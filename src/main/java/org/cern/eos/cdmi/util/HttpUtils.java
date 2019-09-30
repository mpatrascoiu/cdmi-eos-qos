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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.indigo.cdmi.BackEndException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class to facilitate handling of HTTP requests and responses.
 */
public class HttpUtils {

  private static final HttpClient client = HttpClientBuilder.create().build();
  private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

  /**
   * Performs an HTTP request at the given URL and returns the response as JSON object.
   *
   * @param url the URL to query
   * @return json response object
   */
  public static JSONObject executeCommand(String url) throws BackEndException {
    return execute(new HttpGet(url));
  }

  /**
   * Performs an HTTP request and returns the response as JSON object.
   *
   * @param request the HTTP Request to perform
   * @return json response object
   */
  private static JSONObject execute(HttpUriRequest request) throws BackEndException {
    try {
      LOG.debug("HTTP Request: {}", request);
      HttpResponse response = client.execute(request);

      if (statusOk(response)) {
        return JsonUtils.responseToJson(response);
      } else {
        LOG.warn("{} {} {}: {}", request.getMethod(), request.getURI(),
          response.getStatusLine().getStatusCode(), httpResponseToString(response));

        if (statusError(response)) {
          throw new BackEndException(
            JsonUtils.responseToJson(response).getString("error"));
        }
      }
    } catch (IOException | JSONException | BackEndException e) {
      String message =
        String.format("Failed %s %s -- %s", request.getMethod(), request.getURI(), e.getMessage());
      throw new BackEndException(message);
    }

    return null;
  }

  /**
   * Returns true if HTTP response status code is in the HTTP OK range.
   */
  private static boolean statusOk(HttpResponse response) {
    return (response.getStatusLine().getStatusCode() >= 200 &&
            response.getStatusLine().getStatusCode() <= 202);
  }

  /**
   * Returns true if HTTP response status code is in the HTTP Error range.
   */
  private static boolean statusError(HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();

    return (statusCode == 400 || statusCode == 401 || statusCode == 404 ||
            statusCode == 500 || statusCode == 501);
  }

  /**
    * Return a string representation of an HTTP response object.
    */
  private static String httpResponseToString(HttpResponse response) throws IOException {
      return EntityUtils.toString(response.getEntity(), "UTF-8");
  }
}
