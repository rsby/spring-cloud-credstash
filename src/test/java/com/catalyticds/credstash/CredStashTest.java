package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptResult;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.catalyticds.credstash.CredStashStrings.*;
import static com.catalyticds.credstash.CredStashCrypto.INITIALIZATION_VECTOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CredStashTest {

    private AmazonDynamoDB dynamoDBClient = mock(AmazonDynamoDB.class);
    private AWSKMS awskmsClient = mock(AWSKMS.class);
    private CredStash credStash = new CredStash(dynamoDBClient, awskmsClient, new CredStashBouncyCastleCrypto());

    @Test
    public void testGetSecret() {

        String secret = "@#$rttegert$%";
        String secretName = "my_secret_name";
        String table = "my_table";

        MockCredStashData data = new MockCredStashData(secret, secretName, 1);

        when(dynamoDBClient.query(any())).thenReturn(data.queryResult());
        when(awskmsClient.decrypt(any())).thenReturn(data.decryptResult());

        Optional<DecryptedSecret> decryptedSecretOptional =
                credStash.getSecret(new SecretRequest(secretName).withTable(table));

        DecryptedSecret decryptedSecret = decryptedSecretOptional.orElseThrow(RuntimeException::new);

        verify(dynamoDBClient, times(1)).query(any());
        assertEquals(secret, decryptedSecret.getSecret());
        assertEquals(secretName, decryptedSecret.getName());
        assertEquals(table, decryptedSecret.getTable());
        assertEquals(padVersion(1), decryptedSecret.getVersion());
    }

    @Test
    public void testGetSecret_missing() {

        String secretName = "my_secret_name";
        String table = "my_table";

        when(dynamoDBClient.query(any())).thenReturn(new QueryResult().withCount(0));

        assertFalse(credStash.getSecret(new SecretRequest(secretName).withTable(table)).isPresent());
    }

    @Test
    public void testGetSecretWithVersion() {

        String secret = "@#$ppoeert$%";
        String secretName = "my_secret_name";
        String table = "my_table";

        MockCredStashData data = new MockCredStashData(secret, secretName, 1);

        when(dynamoDBClient.getItem(any())).thenReturn(data.getItemResult());
        when(awskmsClient.decrypt(any())).thenReturn(data.decryptResult());

        Optional<DecryptedSecret> decryptedSecretOptional =
                credStash.getSecret(new SecretRequest(secretName)
                        .withTable(table)
                        .withVersion(1));

        DecryptedSecret decryptedSecret = decryptedSecretOptional.orElseThrow(RuntimeException::new);

        verify(dynamoDBClient, times(1)).getItem(any());
        assertEquals(secret, decryptedSecret.getSecret());
        assertEquals(secretName, decryptedSecret.getName());
        assertEquals(table, decryptedSecret.getTable());
        assertEquals(padVersion(1), decryptedSecret.getVersion());

    }

    class MockCredStashData {

        private final byte[] decryptedKey = new byte[64];
        private final Map<String, AttributeValue> item;

        QueryResult queryResult() {
            QueryResult result = new QueryResult();
            result.setItems(Collections.singletonList(item));
            result.setCount(1);
            result.setScannedCount(1);
            return result;
        }

        GetItemResult getItemResult() {
            GetItemResult result = new GetItemResult();
            result.setItem(item);
            return result;
        }

        DecryptResult decryptResult() {
            DecryptResult result = new DecryptResult();
            result.setPlaintext(ByteBuffer.wrap(decryptedKey));
            return result;
        }

        MockCredStashData(String secret, String secretName, int version) {

            // setup fake key values
            byte[] keyBytes = "12121212121212121212121212121212".getBytes();
            byte[] hmacKeyBytes = new byte[32];
            System.arraycopy(keyBytes, 0, decryptedKey, 0, 32);
            for (int i = 0; i < 32; i++) {
                hmacKeyBytes[i] = (byte) i;
                decryptedKey[i + 32] = (byte) i;
            }

            byte[] encryptedKeyBytes = new byte[] {1};

            byte[] secretBytes = secret.getBytes();

            // Credstash uses standard AES
            BlockCipher engine = new AESEngine();

            // Credstash uses CTR mode
            StreamBlockCipher cipher = new SICBlockCipher(engine);

            cipher.init(true, new ParametersWithIV(new KeyParameter(keyBytes), INITIALIZATION_VECTOR));

            byte[] contents = new byte[secretBytes.length];
            int contentsOffset = 0;
            int resultOffset = 0;
            cipher.processBytes(secretBytes, contentsOffset, contents.length, contents, resultOffset);
            byte[] hmac = new CredStashBouncyCastleCrypto().digest(hmacKeyBytes, contents, Digests.SHA256);

            item = new HashMap<>();
            item.put(Keys.NAME, new AttributeValue(secretName));
            item.put(Keys.VERSION, new AttributeValue(padVersion(version)));
            item.put(Keys.KEY, new AttributeValue(new String(Base64.getEncoder().encode(encryptedKeyBytes))));
            item.put(Keys.CONTENTS, new AttributeValue(new String(Base64.getEncoder().encode(contents))));
            item.put(Keys.HMAC, new AttributeValue(new String(Hex.encodeHex(hmac))));
            item.put(Keys.DIGEST, new AttributeValue(Digests.SHA256));
        }
    }
}
