/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.common;

import org.slf4j.*;

import java.lang.invoke.MethodHandles;
import java.security.*;


/**
 * A class helping to create hashes.

 * @author boto
 * Date of creation February 7, 2018
 */
public class HashCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String ALGO_SHA215 = "SHA-256";

    private static final String ALGO_SHA512 = "SHA-512";

    /**
     * Create a SHA 256 hash out of given content.
     *
     * @throws Exception if something goes wrong.
     */
    public static String createSHA256(byte[] content) throws Exception {
        return createHash(content, ALGO_SHA215);
    }

    /**
     * Create a SHA 512 hash out of given content.
     *
     * @throws Exception if something goes wrong.
     */
    public static String createSHA512(byte[] content) throws Exception {
        return createHash(content, ALGO_SHA512);
    }

    private static String createHash(byte[] content, final String algorithmName) throws Exception {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithmName);
            digest.update(content);
            byte data[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                String hex = Integer.toHexString(0xff & data[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Problem occurred while creating a SHA-256 hash, reason: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
    }
}
