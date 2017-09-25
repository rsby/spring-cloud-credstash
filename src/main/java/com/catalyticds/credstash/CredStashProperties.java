package com.catalyticds.credstash;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author reesbyars on 9/22/17.
 */
@ConfigurationProperties(prefix = "credstash")
class CredStashProperties extends CredStashPropertyConfig {

    private Boolean enabled = false;
    private String pathSeparator = null;
    private Map<String, CredStashPropertyConfig> properties = new LinkedHashMap<>();

    public CredStashProperties() {
        setName("defaults");
        setTable("credential-store");
        setKeyPrefix("");
        setPropertyPattern("");
        setVersion(null);
        setStrip(null);
        setContext(new LinkedHashMap<>());
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public Map<String, CredStashPropertyConfig> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, CredStashPropertyConfig> properties) {
        this.properties = properties;
    }

    List<CredStashPropertyConfig> compileToOrderedList() {
        List<CredStashPropertyConfig> configs = properties
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().withNameAndDefaults(entry.getKey(), this))
                .collect(Collectors.toList());
        configs.add(this);
        return configs;
    }

    @Override
    public String toString() {
        return "CredStashProperties{" +
                "enabled=" + enabled +
                ", pathSeparator='" + pathSeparator + '\'' +
                ", defaults=" + super.toString() +
                ", properties=" + properties +
                '}';
    }
}
