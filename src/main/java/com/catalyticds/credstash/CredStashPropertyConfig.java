package com.catalyticds.credstash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author reesbyars on 9/24/17.
 */
public class CredStashPropertyConfig {

    private String name;
    private Boolean enumerable = true;
    private Boolean enabled = true;
    private List<PropertyEntry> matching = new ArrayList<>();
    private String addPrefix;
    private String stripPrefix;
    private String table;
    private String version;
    private Map<String, String> context;
    private List<SecretPropertySourceConfig> sources = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnumerable() {
        return enumerable;
    }

    public void setEnumerable(Boolean enumerable) {
        this.enumerable = enumerable;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<PropertyEntry> getMatching() {
        return matching;
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

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
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

    public List<SecretPropertySourceConfig> getSources() {
        return sources;
    }

    public CredStashPropertyConfig withDefaults(String name, CredStashPropertyConfig defaults) {
        this.name = name;
        addPrefix = Objects.toString(addPrefix, defaults.getAddPrefix());
        stripPrefix = Objects.toString(stripPrefix, defaults.getStripPrefix());
        table = Objects.toString(table, defaults.getTable());
        version = Objects.toString(version, defaults.getVersion());
        context = Optional.ofNullable(context).orElse(defaults.getContext());
        return this;
    }

    @Override
    public String toString() {
        return "CredStashPropertyConfig{" +
                "name='" + name + '\'' +
                ", enumerable=" + enumerable +
                ", enabled=" + enabled +
                ", matching=" + matching +
                ", addPrefix='" + addPrefix + '\'' +
                ", stripPrefix='" + stripPrefix + '\'' +
                ", table='" + table + '\'' +
                ", version='" + version + '\'' +
                ", context=" + context +
                '}';
    }

    public static class PropertyEntry {

        private String pattern;
        private String key;

        public PropertyEntry() { }

        public PropertyEntry(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "PropertyEntry{" +
                    "pattern='" + pattern + '\'' +
                    ", key='" + key + '\'' +
                    '}';
        }
    }
}
