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

package org.cern.eos.cdmi.util;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to facilitate Json parsing.
 */
public class JsonUtils {

  private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

  /**
   * Return HTTP response as JSON object.
   */
  public static JSONObject responseToJson(HttpResponse response) throws IOException, JSONException {
    String cmdResponse = EntityUtils.toString(response.getEntity());
    String cmdOut = EOSParseUtils.extractCmdOutput(cmdResponse);
    LOG.debug("Converting response output to JSON: {}", cmdOut);

    JSONObject jsonObject;

    try {
      jsonObject = new JSONObject(cmdOut);
    } catch (JSONException objectE) {
      LOG.warn("Failed conversion from out to JSON Object");

      try {
        JSONArray jsonArray = new JSONArray(cmdOut);
        jsonObject = new JSONObject();
        jsonObject.put("name", jsonArray);
      } catch (JSONException arrayE) {
        LOG.warn("Failed conversion from out to JSON Array");
        throw objectE;
      }
    }

    return jsonObject;
  }

  /**
   * Convert a JSON array into a list of strings.
   */
  public static List<String> jsonArrayToStringList(JSONArray array) {
    int len = array.length();
    List<String> list = new ArrayList<String>(len);

    for (int i = 0; i < len; i++) {
      list.add(array.getString(i));
    }

    return list;
  }
}
