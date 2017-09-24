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
    private String propertyPatterns = "**.secret,**.password,**.key";
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

    public String getPropertyPatterns() {
        return propertyPatterns;
    }

    public void setPropertyPatterns(String propertyPatterns) {
        this.propertyPatterns = propertyPatterns;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    @Override
    public String toString() {
        return "CredStashProperties{" +
                "enabled=" + enabled +
                ", table='" + table + '\'' +
                ", keyPrefix='" + keyPrefix + '\'' +
                ", propertyPatterns='" + propertyPatterns + '\'' +
                ", pathSeparator='" + pathSeparator + '\'' +
                '}';
    }
}
