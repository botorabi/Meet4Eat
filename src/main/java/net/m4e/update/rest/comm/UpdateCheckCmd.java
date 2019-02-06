/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.update.rest.comm;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author boto
 * Date of creation March 12, 2018
 */
public class UpdateCheckCmd {

    private String name;
    private String os;
    private String flavor;
    private String clientVersion;

    public UpdateCheckCmd() {
    }

    public UpdateCheckCmd(final String name,
                          final String os,
                          final String flavor,
                          final String clientVersion) {
        this.name = name;
        this.os = os;
        this.flavor = flavor;
        this.clientVersion = clientVersion;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    @JsonbProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonbProperty("os")
    public void setOs(String os) {
        this.os = os;
    }

    @JsonbProperty("flavor")
    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    @JsonbProperty("clientVersion")
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }
}
