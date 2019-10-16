/*
 * The MIT License
 * Copyright (c) 2019 CERN/Switzerland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.cern.eos.cdmi;

import org.cern.eos.cdmi.protobuf.ProtobufUtils;
import org.cern.eos.cdmi.util.HttpUtils;
import org.cern.eos.cdmi.util.JsonUtils;
import org.cern.eos.cdmi.util.EOSParseUtils;
import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.BackendCapability;
import org.indigo.cdmi.CdmiObjectStatus;
import org.indigo.cdmi.spi.StorageBackend;
import org.cern.eos.cdmi.util.PluginConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.indigo.cdmi.BackendCapability.CapabilityType.CONTAINER;
import static org.indigo.cdmi.BackendCapability.CapabilityType.DATAOBJECT;

/**
 * Implementation of a StorageBackend compatible with the EOS storage endpoint.
 * <p>
 * Interface of the StorageBackend is defined by the cdmi-spi project:
 * https://github.com/indigo-dc/cdmi-spi
 */
public class EosStorageBackend implements StorageBackend {

  public static final HashMap<String, Object> capabilities = new HashMap<>();
  private static final Logger LOG = LoggerFactory.getLogger(EosStorageBackend.class);
  private static final String cmdPath = "/proc/user/";

  static {
    capabilities.put("cdmi_data_redundancy", "true");
    capabilities.put("cdmi_geographic_placement", "true");
    capabilities.put("cdmi_capabilities_allowed", "true");
    capabilities.put("cdmi_latency", "true");
  }

  private String eosServer;

  public EosStorageBackend() {
    PluginConfig config = new PluginConfig();

    // Fail early if preconditions are not met
    config.throwIfNull("eos.server");
    config.throwIfNull("eos.server.port");
    config.throwIfNull("eos.server.scheme");

    String scheme = config.get("eos.server.scheme");
    eosServer = scheme + "://" + config.get("eos.server") + ":" + config.get("eos.server.port");
  }

  /**
   * Returns a list of all QoS capabilities provided by the backend storage.
   *
   * @return list of provided capabilities
   */
  @Override
  public List<BackendCapability> getCapabilities() throws BackEndException {
    final BackendCapability.CapabilityType[] types = new BackendCapability.CapabilityType[]{CONTAINER, DATAOBJECT};
    List<BackendCapability> backendCapabilities = new ArrayList<>();

    LOG.debug("Fetching CDMI capabilities.");
    String url = "";

    try {
      // Perform "eos qos list" to retrieve all available QoS classes
      url = buildProtoCommandUrl(ProtobufUtils.QoSList());
      JSONObject response = HttpUtils.executeCommand(url);

      // Retrieve capabilities for each QoS class
      for (String capability : JsonUtils.jsonArrayToStringList(response.getJSONArray("name"))) {
        url = buildProtoCommandUrl(ProtobufUtils.QoSListClass(capability));
        response = HttpUtils.executeCommand(url);

        // Use same QoS class for Containers and Dataobjects
        for (BackendCapability.CapabilityType type : types) {
          BackendCapability backendCapability = EOSParseUtils.backendCapabilityFromJson(response, type);
          backendCapabilities.add(backendCapability);
          LOG.info("{} capability: {}", EOSParseUtils.capabilityTypeToString(type), backendCapability);
        }
      }

      // Add Empty capability
      for (BackendCapability.CapabilityType type : types) {
        BackendCapability emptyCapability = emptyBackendCapability(type);
        backendCapabilities.add(emptyCapability);
        LOG.info("{} capability: {}", EOSParseUtils.capabilityTypeToString(type), emptyCapability);
      }

      return backendCapabilities;
    } catch (JSONException | BackEndException | UnsupportedEncodingException e) {
      LOG.error("Error fetching CDMI capabilities -- {}", e.getMessage());
      throw new BackEndException(
        String.format("Failed command %s -- %s", url, e.getMessage()));
    }
  }

  /**
   * Starts a CDMI QoS transition of the object at the given path towards the capability
   * described at the specified URI.
   *
   * @param path                the object path, as queried via the CDMI interface
   * @param targetCapabilityUri the target capabilities URI
   */
  @Override
  public void updateCdmiObject(String path, String targetCapabilityUri) throws BackEndException {
    String qosClass = EOSParseUtils.qosClassFromCapUri(targetCapabilityUri);
    String url = "";

    LOG.debug("Updating CDMI capabilities of: {} [target={}]", path, qosClass);

    try {
      url = buildProtoCommandUrl(ProtobufUtils.QoSSet(path, qosClass));
      JSONObject response = HttpUtils.executeCommand(url);
      LOG.info("QoS update of {} [target={}]: {}", path, qosClass, response);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error updating CDMI capabilities of {} -- {}", path, e.getMessage());
      throw new BackEndException(
          String.format("Failed updating CDMI capabilities of %s [target=%s] [url=%s] -- %s",
              path, qosClass, url, e.getMessage()));
    }
  }

  /**
   * Get QoS information about the object at the given path.
   *
   * @param path the object path, as queried via the CDMI interface
   * @return CDMI object status enriched with QoS information
   */
  @Override
  public CdmiObjectStatus getCurrentStatus(String path) throws BackEndException {
    LOG.debug("Get current CDMI capabilities of: {}", path);
    String url = "";

    try {
      // Perform fileinfo on path
      url = buildFileinfoCommandUrl(path);
      JSONObject fileinfo = HttpUtils.executeCommand(url);

      if (EOSParseUtils.fileinfoIsDirectory(fileinfo)) {
        throw new BackEndException("Is directory");
      }

      // Perform "qos get" on path
      url = buildProtoCommandUrl(ProtobufUtils.QoSGet(path));
      JSONObject qosGet = HttpUtils.executeCommand(url);

      // Extract current_qos, target_qos and monitored metadata
      final Map<String, Object> monitored = EOSParseUtils.metadataFromQoSJson(qosGet);
      String currentClass = qosGet.getString("current_qos");
      String currentCapUri, targetCapUri = null;

      if (currentClass.equals("null")) {
        currentClass = "empty";
      }

      currentCapUri = "/cdmi_capabilities/dataobject/" + currentClass;

      if (qosGet.has("target_qos")) {
        targetCapUri = "/cdmi_capabilities/dataobject/" + qosGet.getString("target_qos");
      }

      CdmiObjectStatus status = new CdmiObjectStatus(monitored, currentCapUri, targetCapUri);
      LOG.info("CDMI Capability of {}: {} {} -- {}", path, currentCapUri,
          ((targetCapUri == null || targetCapUri.isEmpty()) ?
              "[no transition]" :
              "[transition to " + targetCapUri + "]"),
          status);

      return status;
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error retrieving CDMI capabilities of {} -- {}", path, e.getMessage());
      throw new BackEndException(
          String.format("Failed retrieving CDMI capabilities of %s [url=%s] -- %s",
              path, url, e.getMessage()));
    }
  }

  /**
   * Return the EOS protobuf specific command URL containing the given opaque info.
   */
  private String buildProtoCommandUrl(String opaqueInfo) throws UnsupportedEncodingException {
    String encodedOpaque = URLEncoder.encode(opaqueInfo, StandardCharsets.UTF_8.toString());
    return eosServer + cmdPath + "?mgm.cmd.proto=" + encodedOpaque;
  }

  /**
   * Return the EOS fileinfo specific command URL containing the given path.
   */
  private String buildFileinfoCommandUrl(String path) throws UnsupportedEncodingException {
    String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8.toString());
    return eosServer + cmdPath + "?mgm.cmd=fileinfo&mgm.path=" + encodedPath + "&mgm.format=json";
  }

  /**
   * Return an empty BackendCapability corresponding to a file with no QoS class.
   */
  private BackendCapability emptyBackendCapability(BackendCapability.CapabilityType type) {
    BackendCapability emptyCapability = new BackendCapability("empty", type);
    emptyCapability.setMetadata(new HashMap<>());
    emptyCapability.setCapabilities(EosStorageBackend.capabilities);

    return emptyCapability;
  }
}
