/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.system.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Base URL for all RESTful services
     */
    public final static String REST_BASE_URL = "webresources";

    /**
     * URL for accessing the WebSocket endpoint
     */
    public final static String WEBSOCKET_URL = "/ws";

    /**
     * Configuration token name for app version.
     */
    public final static String TOKEN_APP_VERSION = "AppVersion";

    /**
     * Configuration token name for mailer config file.
     */
    public final static String TOKEN_MAILER_CONFIG_FILE = "MailerConfigFile";

    /**
     * Configuration token name for account registration config file.
     * This file is used for providing activation or password reset links in emails sent to users.
     */
    public final static String TOKEN_ACC_REGISTRATION_CONFIG_FILE = "AccountRegistrationConfigFile";

    /**
     * All settings found in account registration file, if one exists.
     */
    private Properties accountRegistrationConfig;

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
     * Get the account registration configuration if a config file exists. The config file name
     * is defined by the value of app parameter with name given by 'TOKEN_ACC_REGISTRATION_CONFIG_FILE'.
     * The file is expected to be in WEB-INF directory. If no such file exists the null is returned.
     * 
     * @return Account registration configuration
     */
    public Properties getAccountRegistrationConfig() {
        return accountRegistrationConfig;
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
     * Setup the account registration configuration. The config is read from given
     * stream. This method is used during application start.
     * 
     * @param configContent Configuration file's content
     */
    protected void setupAccountRegistrationConfig(InputStream configContent) {
        if (configContent == null) {
            accountRegistrationConfig = null;    
        }
        else {
            try {
                Properties props = new Properties();
                props.load(configContent);
                accountRegistrationConfig = props;
                LOGGER.info("Successfully loaded account registration configuration");
            }
            catch (IOException ex) {
                LOGGER.warn("Could not load account registration configuration, reason: " + ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Singleton holder class
     */
    private static class AppConfigurationHolder {

        private static final AppConfiguration INSTANCE = new AppConfiguration();
    }
}
