/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
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
public class UpdateCheckResult {

    private String updateVersion;
    private String os;
    private String url;
    private Long releaseDate;

    public UpdateCheckResult() {
    }

    public UpdateCheckResult(final String updateVersion,
                             final String os,
                             final String url,
                             final Long releaseDate) {

        this.os = os;
        this.url = url;
        this.updateVersion = updateVersion;
        this.releaseDate = releaseDate;
    }

    public String getUpdateVersion() {
        return updateVersion;
    }

    public String getOs() {
        return os;
    }

    public String getUrl() {
        return url;
    }

    public Long getReleaseDate() {
        return releaseDate;
    }

    @JsonbProperty("updateVersion")
    public void setUpdateVersion(String updateVersion) {
        this.updateVersion = updateVersion;
    }

    @JsonbProperty("os")
    public void setOs(String os) {
        this.os = os;
    }

    @JsonbProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonbProperty("releaseDate")
    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }
}
