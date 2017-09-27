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

    private String pathSeparator = ".";
    private Map<String, CredStashPropertyConfig> more = new LinkedHashMap<>();

    public CredStashProperties() {
        setName("defaults");
        setTable("credential-store");
        setEnabled(false);
        setAddPrefix("");
        getMatching().add("");
        setVersion(null);
        setStripPrefix(null);
        setContext(new LinkedHashMap<>());
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public void setPathSeparator(String pathSeparator) {
        this.pathSeparator = pathSeparator;
    }

    public Map<String, CredStashPropertyConfig> getMore() {
        return more;
    }

    public void setMore(Map<String, CredStashPropertyConfig> more) {
        this.more = more;
    }

    List<CredStashPropertyConfig> compileToOrderedList() {
        List<CredStashPropertyConfig> configs = more
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
                ", pathSeparator='" + pathSeparator + '\'' +
                ", defaults=" + super.toString() +
                ", more=" + more +
                '}';
    }
}
