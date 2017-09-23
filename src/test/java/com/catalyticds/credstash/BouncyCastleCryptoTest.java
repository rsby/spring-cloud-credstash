package com.catalyticds.credstash;

/**
 * @author jcoyle on 2/1/16.
 */
public class BouncyCastleCryptoTest extends CredStashCryptoTest {
    public BouncyCastleCryptoTest(String key, String digestKey, String decrypted, String encrypted, String digest) {
        super(key, digestKey, decrypted, encrypted, digest);
    }

    @Override
    protected CredStashCrypto getCryptoImplementation() {
        return new CredStashBouncyCastleCrypto();
    }
}
