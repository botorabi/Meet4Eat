/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Central place for application configuration. This is a singleton class.
 * It is filled with context-params of web.xml on startup of application.
 * It can also hold other useful configuration information.
 * All configuration values are of type String.
 * 
 * Usage example:
 * 
 *   String config = AppConfiguration.getInstance().getConfigValue("MyToken");
 * 
 * @author boto
 * Date of creation Aug 18, 2017
 */
public class AppConfiguration {

    /**
     * Base URL for all RESTful services
     */
    public final static String REST_BASE_URL = "webresources";

    /**
     * The unit name for persistence as configured in persistence.xml
     */
    public final static String PERSITENCE_UNIT_NAME = "Meet4EatPU";

    /**
     * Configuration token name for app version.
     */
    public final static String TOKEN_APP_VERSION = "AppVersion";

    /**
     * App configuration map holding environment parameters.
     */
    private final Map<String, String> configs = new HashMap<>();

    /**
     * Get a configuration value given a token.
     * 
     * @param token Token
     * @return  Token value
     */
    public String getConfigValue(String token) {
        return configs.get(token);
    }

    /**
     * Set the configuration value for given token.
     * 
     * @param token Token
     * @param value Token value
     */
    public void setConfigValue(String token, String value) {
        configs.put(token, value);
    }

    /**
     * Private constructor of singleton.
     */
    private AppConfiguration() {
    }

    /**
     * Singleton access
     * 
     * @return Single instance of this class.
     */
    public static AppConfiguration getInstance() {
        return AppConfigurationHolder.INSTANCE;
    }

    /**
     * Singleton holder class
     */
    private static class AppConfigurationHolder {

        private static final AppConfiguration INSTANCE = new AppConfiguration();
    }
}
