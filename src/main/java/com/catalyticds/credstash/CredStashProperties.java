package com.catalyticds.credstash;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author reesbyars on 9/22/17.
 */
@ConfigurationProperties(prefix = "credstash")
class CredStashProperties {

    private Boolean enabled = false;
    private String table = "credential-store";
    private String keyPrefix = "";
    private String propertyPattern = "*";
    private String pathSeparator = ".";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getPropertyPattern() {
        return propertyPattern;
    }

    public void setPropertyPattern(String propertyPattern) {
        this.propertyPattern = propertyPattern;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

}
