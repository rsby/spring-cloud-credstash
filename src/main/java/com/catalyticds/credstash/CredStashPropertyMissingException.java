package com.catalyticds.credstash;

/**
 * @author reesbyars on 9/23/17.
 */
public class CredStashPropertyMissingException extends RuntimeException {

    private final String propertyName;
    private final String secretName;

    CredStashPropertyMissingException(
            String propertyName,
            String secretName,
            String message) {
        super(message);
        this.propertyName = propertyName;
        this.secretName = secretName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getSecretName() {
        return secretName;
    }

}
