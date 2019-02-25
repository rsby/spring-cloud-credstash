package com.catalyticds.credstash;

/**
 * Thrown when a secret-as-a-property-source could not be retrieved from CredStash
 *
 * @author reesbyars on 2/25/19.
 */
public class CredStashMissingPropertySourceException extends RuntimeException {

    private final SecretPropertySourceConfig config;

    CredStashMissingPropertySourceException(SecretPropertySourceConfig config) {
        super("Failed to locate property source in CredStash for " + config.getSecretName());
        this.config = config;
    }

    public SecretPropertySourceConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "CredStashMissingPropertySourceException{" +
                "config=" + config +
                '}';
    }
}
