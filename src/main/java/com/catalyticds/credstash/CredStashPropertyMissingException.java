package com.catalyticds.credstash;

/**
 * @author reesbyars on 9/23/17.
 */
public class CredStashPropertyMissingException extends RuntimeException {

    private final String propertyName;
    private final CredStashPropertyConfig propertyConfig;

    CredStashPropertyMissingException(
            String propertyName,
            CredStashPropertyConfig propertyConfig,
            String message) {
        super(message);
        this.propertyName = propertyName;
        this.propertyConfig = propertyConfig;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public CredStashPropertyConfig getPropertyConfig() {
        return propertyConfig;
    }
}
