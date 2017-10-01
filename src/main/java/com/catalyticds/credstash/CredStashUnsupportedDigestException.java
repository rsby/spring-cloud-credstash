package com.catalyticds.credstash;

/**
 * @author reesbyars on 9/30/17.
 */
public class CredStashUnsupportedDigestException extends RuntimeException {

    private final String digest;

    CredStashUnsupportedDigestException(String message, String digest) {
        super(message);
        this.digest = digest;
    }

    public String getDigest() {
        return digest;
    }
}
