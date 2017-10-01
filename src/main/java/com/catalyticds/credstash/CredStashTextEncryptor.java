package com.catalyticds.credstash;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * A password-based encryptor using CredStash for secure key management and the Spring
 * {@link Encryptors#delux(CharSequence, CharSequence)} facility for cryptographically strong encryption.
 *
 * Versions of secrets are cached. This caching plus using the Spring Encryptors rather than leveraging
 * AWS KMS directly minimizes AWS costs, improves performance, and still maintains cryptographic strength and
 * secure key management (via CredStash).
 *
 * Any CredStash secret used as the password must use integer-based versioning. Left zero-padding
 * is okay.
 *
 * @author reesbyars on 9/30/17.
 */
public class CredStashTextEncryptor implements TextEncryptor {

    private static final String separator = "::";

    private final CredStash credStash;
    private final DecryptedSecret encryptionSecret;
    private final SecretRequest secretRequest;
    private final ConcurrentMap<Integer, DecryptedSecret> cachedVersions = new ConcurrentHashMap<>();

    public CredStashTextEncryptor(
            CredStash credStash,
            SecretRequest encryptionSecretRequest) {
        this.credStash = credStash;
        this.secretRequest = encryptionSecretRequest;
        Optional<DecryptedSecret> optionalEncryptionSecret =
                credStash.getSecret(encryptionSecretRequest);
        if (!optionalEncryptionSecret.isPresent()) {
            throw new IllegalStateException("Could not locate secret ==> " + encryptionSecretRequest);
        }
        encryptionSecret = optionalEncryptionSecret.get();
        cachedVersions.put(Integer.valueOf(encryptionSecret.getVersion()), encryptionSecret);
    }

    @Override
    public String encrypt(String text) {
        String salt = KeyGenerators.string().generateKey();
        return Integer.valueOf(encryptionSecret.getVersion()) +
                separator +
                salt +
                separator +
                Encryptors.delux(encryptionSecret.getSecret(), salt).encrypt(text);
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null) {
            return null;
        }
        String[] parts = encryptedText.split(separator);
        if (parts.length < 3) {
            return encryptedText;
        }
        Integer version = Integer.valueOf(parts[0]);
        String salt = parts[1];
        String encrypted = parts[2];
        DecryptedSecret secret = cachedVersions.computeIfAbsent(version, v ->
                credStash.getSecret(new SecretRequest(encryptionSecret.getName())
                        .withTable(encryptionSecret.getTable())
                        .withContext(secretRequest.getContext().orElse(null))
                        .withVersion(v))
                        .orElseThrow(() ->
                                new IllegalArgumentException("Version not found ==> " + version)));
        return Encryptors.delux(secret.getSecret(), salt).decrypt(encrypted);
    }

}
