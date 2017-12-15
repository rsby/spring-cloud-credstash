package com.catalyticds.credstash;

import org.junit.Test;

import java.util.Optional;

import static com.catalyticds.credstash.CredStashStrings.padVersion;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author reesbyars on 9/30/17.
 */
public class CredStashTextEncryptorTest {

    private SecretRequest mainRequest = new SecretRequest("PBE_password").withTable("test_table");
    private SecretRequest v3Request = new SecretRequest("PBE_password").withTable("test_table").withVersion(padVersion(3));
    private CredStash credStash = mock(CredStash.class);

    @Test
    public void encrypt() {

        when(credStash.getSecret(mainRequest)).thenReturn(Optional.of(
                new DecryptedSecret(
                        mainRequest.getTable(),
                        mainRequest.getSecretName(),
                        padVersion(4),
                        "secret_pass_4")));

        when(credStash.getSecret(v3Request)).thenReturn(Optional.of(
                new DecryptedSecret(
                        mainRequest.getTable(),
                        mainRequest.getSecretName(),
                        padVersion(3),
                        "secret_pass_3")));

        CredStashTextEncryptor oldEncryptor = new CredStashTextEncryptor(credStash, v3Request);
        String v3Text = "v3_text";
        String encryptedV3Text = oldEncryptor.encrypt(v3Text);

        CredStashTextEncryptor encryptor = new CredStashTextEncryptor(credStash, mainRequest);
        String decryptedV3Text = encryptor.decrypt(encryptedV3Text);
        assertEquals(v3Text, decryptedV3Text);
        String v4Text = "v4_text";
        String encryptedV4Text = encryptor.encrypt(v4Text);
        String decryptedV4Text = encryptor.decrypt(encryptedV4Text);
        assertEquals(v4Text, decryptedV4Text);
    }

}