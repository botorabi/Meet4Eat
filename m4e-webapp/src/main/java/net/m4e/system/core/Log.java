/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.system.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application logger. Use this logger for all application related outputs.
 * 
 * @author boto
 * Date of creation Aug 22, 2017
 */
abstract public class Log {

    private static final String LOG_NAME = "(net.m4e) ";
    private static       Logger logger   = null;

    /**
     * Get the single instance of logger.
     * @return 
     */
    private static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(LOG_NAME);
        }
        return logger;
    }

    /**
     * Format and output the message.
     * 
     * @param level Log level string auch as D, V, I etc.
     * @param tag   Module name the log came from
     * @param msg   Message to output
     */
    private static void outputMsg(String level, String tag, String msg) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MMM.yyyy HH:mm:ss");
        LocalDateTime date = LocalDateTime.now();
        getLogger().log(Level.INFO, "[{0}] ({1}) {2}: {3}", new Object[]{level, date.format(formatter), tag, msg});
    }

    /**
     * Output a verbose message.
     * 
     * @param tag   Module name the message came from
     * @param msg   Message which is being logged
     */
    public static void verbose(String tag, String msg) {
        outputMsg("V", tag, msg);
    }

    /**
     * Output an info message.
     * 
     * @param tag   Module name the message came from
     * @param msg   Message which is being logged
     */
    public static void info(String tag, String msg) {
        outputMsg("I", tag, msg);
    }

    /**
     * Output an error message.
     * 
     * @param tag   Module name the message came from
     * @param msg   Message which is being logged
     */
    public static void error(String tag, String msg) {
        outputMsg("E", tag, msg);
    }

    /**
     * Output a warning message.
     * 
     * @param tag   Module name the message came from
     * @param msg   Message which is being logged
     */

    public static void warning(String tag, String msg) {
        outputMsg("W", tag, msg);
    }

    /**
     * Output a debug message.
     * 
     * @param tag   Module name the message came from
     * @param msg   Message which is being logged
     */
    public static void debug(String tag, String msg) {
        outputMsg("D", tag, msg);
    }
}
