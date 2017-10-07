package com.catalyticds.credstash;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
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
    private Mode mode = Mode.PROD;
    private List<CredStashPropertyConfig> properties = new ArrayList<>();

    public CredStashProperties() {
        setName("defaults");
        setTable("credential-store");
        setEnabled(false);
        setAddPrefix("");
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

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public List<CredStashPropertyConfig> getProperties() {
        return properties;
    }

    List<CredStashPropertyConfig> compileToOrderedList() {
        CredStashPropertyConfig defaultConfig = new CredStashPropertyConfig();
        defaultConfig.setName("credstash__*.**");
        defaultConfig.setStripPrefix("credstash__");
        properties.add(defaultConfig);
        properties.forEach(config -> config.withDefaults(this));
        return properties;
    }

    @Override
    public String toString() {
        return "CredStashProperties{" +
                "pathSeparator='" + pathSeparator + '\'' +
                ", defaults=" + super.toString() +
                '}';
    }

    public enum Mode {
        PROD, AUDIT
    }
}
