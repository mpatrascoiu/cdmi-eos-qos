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

import org.indigo.cdmi.BackEndException;
import org.indigo.cdmi.BackendCapability;
import org.indigo.cdmi.CdmiObjectStatus;
import org.indigo.cdmi.spi.StorageBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of a StorageBackend compatible with the EOS storage endpoint.
 *
 * Interface of the StorageBackend is defined by the cdmi-spi project:
 * https://github.com/indigo-dc/cdmi-spi
 */
public class EosStorageBackend implements StorageBackend {

 public static final String cmdPath = "/proc/user/";
 public static final HashMap<String, Object> capabilities = new HashMap<>();
 private static final Logger LOG = LoggerFactory.getLogger(EosStorageBackend.class);

 static {
  capabilities.put("cdmi_data_redundancy", "true");
  capabilities.put("cdmi_geographic_placement", "true");
  capabilities.put("cdmi_capabilities_allowed", "true");
  capabilities.put("cdmi_latency", "true");
 }

 private ServerConfig config;
 private String eosServer;

 public EosStorageBackend() {

 }

 /**
  * Returns a list of all QoS capabilities provided by the backend storage.
  *
  * @return list of provided capabilities
  */
 @Override
 public List<BackendCapability> getCapabilities() throws BackEndException {
  return null;
 }

 /**
  * Starts a CDMI QoS transition of the object at the given path towards the capability
  * described at the specified URI.
  *
  * @param path the object path, as queried via the CDMI interface
  * @param targetCapabilityUri the target capabilities URI
  */
 @Override
 public void updateCdmiObject(String path, String targetCapabilityUri) throws BackEndException {

 }

 /**
  * Get QoS information about the object at the given path.
  *
  * @param path the object path, as queried via the CDMI interface
  * @return CDMI object status enriched with QoS information
  */
 @Override
 public CdmiObjectStatus getCurrentStatus(String path) throws BackEndException {
  return null;
 }
}
