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

package org.cern.eos.cdmi.protobuf;

import org.cern.eos.cdmi.protobuf.generated.Request.*;
import org.cern.eos.cdmi.protobuf.generated.QoSCmd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Utility class to handle Protobuf command building and serialization.
 */
public class ProtobufUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ProtobufUtils.class);

  /**
   * Returns the base64 encoded string of the Protobuf "qos ls" command.
   */
  public static String QoSList() {
    QoSProto.ListProto qosList = QoSProto.ListProto.newBuilder().build();
    QoSProto qos = QoSProto.newBuilder()
        .setList(qosList)
        .build();

    return Base64Encode(PackageQoSRequest(qos));
  }

  /**
   * Returns the base64 encoded string of the Protobuf "qos ls classname" command.
   */
  public static String QoSListClass(String qosClassName) {
    QoSProto.ListProto qosList = QoSProto.ListProto.newBuilder()
        .setClassname(qosClassName)
        .build();
    QoSProto qos = QoSProto.newBuilder()
        .setList(qosList)
        .build();

    return Base64Encode(PackageQoSRequest(qos));
  }

  /**
   * Returns the base64 encoded string of the Protobuf "qos get path" command.
   */
  public static String QoSGet(String path) {
    QoSProto.IdentifierProto identifier = QoSProto.IdentifierProto.newBuilder()
        .setPath(path)
        .build();
    QoSProto.GetProto qosGet = QoSProto.GetProto.newBuilder()
        .setIdentifier(identifier)
        .build();
    QoSProto qos = QoSProto.newBuilder()
        .setGet(qosGet)
        .build();

    return Base64Encode(PackageQoSRequest(qos));
  }

  /**
   * Returns the base64 encoded string of the Protobuf "qos set path class" command.
   */
  public static String QoSSet(String path, String qosClass) {
    QoSProto.IdentifierProto identifier = QoSProto.IdentifierProto.newBuilder()
        .setPath(path)
        .build();
    QoSProto.SetProto qosSet = QoSProto.SetProto.newBuilder()
        .setIdentifier(identifier)
        .setClassname(qosClass)
        .build();
    QoSProto qos = QoSProto.newBuilder()
        .setSet(qosSet)
        .build();

    return Base64Encode(PackageQoSRequest(qos));
  }

  /**
   * Package a QoS command proto object into a Request protobuf object.
   *
   * @param qos the QoS command to package
   * @return the request protobuf object
   */
  private static RequestProto PackageQoSRequest(QoSProto qos) {
    return RequestProto.newBuilder()
        .setFormat(RequestProto.FormatType.JSON)
        .setDontColor(true)
        .setQos(qos)
        .build();
  }

  /**
   * Returns a base64 string encoding of a protobuf object.
   */
  private static String Base64Encode(RequestProto request) {
    String base64 = Base64.getEncoder().encodeToString(request.toByteArray());

    LOG.debug("Base64 encoding:\n{}--> {}", request.toString(), base64);
    return base64;
  }
}
