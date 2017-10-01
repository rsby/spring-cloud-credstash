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
    private Boolean enumerable = true;
    private Boolean enabled = true;
    private String addPrefix;
    private String stripPrefix;
    private String key;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnumerable() {
        return enumerable;
    }

    public void setEnumerable(Boolean enumerable) {
        this.enumerable = enumerable;
    }

    public String getAddPrefix() {
        return addPrefix;
    }

    public void setAddPrefix(String addPrefix) {
        this.addPrefix = addPrefix;
    }

    public String getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(String stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public CredStashPropertyConfig withDefaults(CredStashPropertyConfig defaults) {
        table = Objects.toString(table, defaults.getTable());
        addPrefix = Objects.toString(addPrefix, defaults.getAddPrefix());
        version = Objects.toString(version, defaults.getVersion());
        stripPrefix = Objects.toString(stripPrefix, defaults.getStripPrefix());
        context = Optional.ofNullable(context).orElse(defaults.getContext());
        return this;
    }

    @Override
    public String toString() {
        return "CredStashPropertyConfig{" +
                "name='" + name + '\'' +
                ", table='" + table + '\'' +
                ", enumerable='" + enumerable + '\'' +
                ", enabled='" + enabled + '\'' +
                ", addPrefix='" + addPrefix + '\'' +
                ", stripPrefix='" + stripPrefix + '\'' +
                ", key='" + key + '\'' +
                ", version='" + version + '\'' +
                ", context=" + context +
                '}';
    }
}
