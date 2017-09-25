package com.catalyticds.credstash;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author reesbyars on 9/24/17.
 */
public class CredStashPropertyConfig {

    private String name;
    private String table;
    private String keyPrefix;
    private String strip;
    private String propertyPattern;
    private String version;
    private Map<String, String> context;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getStrip() {
        return strip;
    }

    public void setStrip(String strip) {
        this.strip = strip;
    }

    public String getPropertyPattern() {
        return propertyPattern;
    }

    public void setPropertyPattern(String propertyPattern) {
        this.propertyPattern = propertyPattern;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public CredStashPropertyConfig withNameAndDefaults(String name, CredStashPropertyConfig defaults) {
        if (propertyPattern == null) {
            throw new IllegalArgumentException("propertyPattern cannot be null for CredStash property " + name);
        }
        setName(name);
        table = Objects.toString(table, defaults.getTable());
        keyPrefix = Objects.toString(keyPrefix, defaults.getKeyPrefix());
        version = Objects.toString(version, defaults.getVersion());
        strip = Objects.toString(strip, defaults.getStrip());
        context = Optional.ofNullable(context).orElse(defaults.getContext());
        return this;
    }

    @Override
    public String toString() {
        return "CredStashPropertyConfig{" +
                "name='" + name + '\'' +
                ", table='" + table + '\'' +
                ", keyPrefix='" + keyPrefix + '\'' +
                ", strip='" + strip + '\'' +
                ", propertyPattern='" + propertyPattern + '\'' +
                ", version='" + version + '\'' +
                ", context=" + context +
                '}';
    }
}
