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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Plugin config encapsulation class.
 * The config options are loaded into a memory-stored properties map.
 */
public class PluginConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PluginConfig.class);
    private Properties properties = new Properties();

    public PluginConfig() {
        String configName = "eos.config";

        // Load config from config directory
        String configFile = "config/" + configName;

        try (InputStream configFileIS = new FileInputStream(configFile)) {
            properties.load(configFileIS);
        } catch (FileNotFoundException e) {
            LOG.error("Could not find configuration file: {}", configFile);
        } catch (IOException e) {
            LOG.error("Failed to parse configuration file: {}", configFile);
            throw new RuntimeException("Failed to parse configuration file: " + configFile, e);
        }
    }

    /**
     * Get property from loaded config properties.
     */
    public String get(String parameter) {
        return properties.getProperty(parameter);
    }

    /**
     * Throw a NullPointerException if the requested property is null.
     */
    public void throwIfNull(String parameter) throws NullPointerException {
        if (properties.getProperty(parameter) == null) {
            throw new NullPointerException("Null property: " + parameter);
        }
    }
}
