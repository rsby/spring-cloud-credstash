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
    private String table;
    private String addPrefix;
    private String stripPrefix;
    private List<String> matching = new ArrayList<>();
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

    public List<String> getMatching() {
        return matching;
    }

    public void setMatching(List<String> matching) {
        this.matching = matching;
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
        if (matching == null || matching.isEmpty()) {
            throw new IllegalArgumentException("'matching' cannot be null or empty for CredStash config " + name);
        }
        setName(name);
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
                ", addPrefix='" + addPrefix + '\'' +
                ", stripPrefix='" + stripPrefix + '\'' +
                ", matching='" + matching + '\'' +
                ", version='" + version + '\'' +
                ", context=" + context +
                '}';
    }
}
