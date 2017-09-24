package com.catalyticds.credstash;

/**
 * @author reesbyars on 9/23/17.
 */
public class CredStashSignatureException extends RuntimeException {

    private final String secretName;
    private final String version;

    CredStashSignatureException(
            String secretName,
            String version,
            String message) {
        super(message);
        this.secretName = secretName;
        this.version = version;
    }

    public String getSecretName() {
        return secretName;
    }

    public String getVersion() {
        return version;
    }
}
