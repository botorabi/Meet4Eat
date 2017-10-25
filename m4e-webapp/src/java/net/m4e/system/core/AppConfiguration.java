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
import javax.servlet.http.HttpServletRequest;

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
     * Configuration token name for mailer config file.
     */
    public final static String TOKEN_MAILER_CONFIG_FILE = "MailerConfigFile";

    /**
     * Configuration token name for user registration config file.
     * This file is used for providing activation or password reset links in emails sent to users.
     */
    public final static String TOKEN_USER_REGISTRATION_CONFIG_FILE = "UserRegistrationURL";

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
     * Given a HTTP servlet request, return the base URL for HTML resources.
     * 
     * @param request   HTTP request
     * @return          Base URL for html resources
     */
    public String getHTMLBaseURL(HttpServletRequest request) {
        String url = request.getScheme() + "://" + request.getServerName();
        url += (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        url += request.getContextPath();
        return url;
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
