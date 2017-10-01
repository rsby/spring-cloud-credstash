package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author jcoyle created on 2/1/16.
 * @author reesbyars refactored on 9/20/17.
 */
public class CredStash {
    
    private final AmazonDynamoDB amazonDynamoDBClient;
    private final AWSKMS awsKmsClient;
    private final CredStashCrypto credStashCrypto;

    public CredStash() {
        this(
                AmazonDynamoDBClientBuilder.defaultClient(),
                AWSKMSClientBuilder.defaultClient(),
                new CredStashBouncyCastleCrypto());
    }

    /**
     * @param amazonDynamoDBClient AWS SDK client for DynamoDB
     * @param awsKmsClient AWS SDK client for KMS
     * @param credStashCrypto the crypto used for decryption and digests
     */
    public CredStash(
            AmazonDynamoDB amazonDynamoDBClient,
            AWSKMS awsKmsClient,
            CredStashCrypto credStashCrypto) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.awsKmsClient = awsKmsClient;
        this.credStashCrypto = credStashCrypto;
    }

    public Optional<DecryptedSecret> getSecret(SecretRequest request)  {

        String tableName = request.getTable();
        Optional<String> optionalVersion = request.getVersion();
        String secretName = request.getSecretName();

        // First find the relevant rows from the credstash table
        StoredSecret encrypted = optionalVersion.isPresent() ?
                readVersionedDynamoItem(tableName, secretName, optionalVersion.get()) :
                readHighestVersionDynamoItem(tableName, secretName);

        if (encrypted == null) {
            return Optional.empty();
        }

        // The secret was encrypted using AES, then the key for that encryption was encrypted with AWS KMS
        // Then both the encrypted secret and the encrypted key are stored in dynamo

        // First obtain that original key again using KMS
        ByteBuffer encryptedBuffer = ByteBuffer.wrap(encrypted.getKey());
        DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(encryptedBuffer);
        request.getContext().ifPresent(decryptRequest::withEncryptionContext);
        DecryptResult decryptResult = awsKmsClient.decrypt(decryptRequest);
        ByteBuffer plainText = decryptResult.getPlaintext();

        // The key is just the first 32 bits, the remaining are for HMAC signature checking
        byte[] keyBytes = new byte[32];
        plainText.get(keyBytes);

        byte[] hmacKeyBytes = new byte[plainText.remaining()];
        plainText.get(hmacKeyBytes);
        byte[] encryptedContents = encrypted.getContents();
        byte[] digest = credStashCrypto.digest(hmacKeyBytes, encryptedContents, encrypted.getDigest());
        if (!Arrays.equals(digest, encrypted.getHmac())) {
            throw new CredStashSignatureException(
                    encrypted.getName(),
                    encrypted.getVersion(),
                    "HMAC integrity check failed");
        }

        // now use AES to finally decrypt the actual secret
        byte[] decryptedBytes = credStashCrypto.decrypt(keyBytes, encryptedContents);
        return Optional.of(new DecryptedSecret(
                tableName,
                secretName,
                encrypted.getVersion(),
                new String(decryptedBytes)));
    }

    private StoredSecret readVersionedDynamoItem(String tableName, String secretName, String version) {
        HashMap<String, AttributeValue> key = new HashMap<>();
        key.put("name", new AttributeValue(secretName));
        key.put("version", new AttributeValue(version));
        GetItemResult getItemResult = amazonDynamoDBClient.getItem(new GetItemRequest(tableName, key, true));
        if (getItemResult == null) {
            return null;
        }
        Map<String, AttributeValue> item = getItemResult.getItem();
        return new StoredSecret(item);
    }

    private StoredSecret readHighestVersionDynamoItem(String tableName, String secretName) {
        QueryResult queryResult = amazonDynamoDBClient.query(
                basicQueryRequest(tableName, secretName)
        );
        if (queryResult.getCount() == 0) {
            return null;
        }
        Map<String, AttributeValue> item = queryResult.getItems().get(0);
        return new StoredSecret(item);
    }

    private QueryRequest basicQueryRequest(String tableName, String secretName) {
        return new QueryRequest(tableName)
                .withLimit(1)
                .withScanIndexForward(false)
                .withConsistentRead(true)
                .addKeyConditionsEntry("name", new Condition()
                        .withComparisonOperator(ComparisonOperator.EQ)
                        .withAttributeValueList(new AttributeValue(secretName)));
    }

    /**
     * Represents a row in a credstash table. The encrypted key and encrypted contents are both stored base64 encoded.
     * The hmac digest is stored hex encoded.
     */
    private static class StoredSecret {

        private final Map<String, AttributeValue> item;

        StoredSecret(Map<String, AttributeValue> item) {
            this.item = item;
        }

        byte[] getKey() {
            return base64AttributeValueToBytes(item.get("key"));
        }

        byte[] getContents() {
            return base64AttributeValueToBytes(item.get("contents"));
        }

        byte[] getHmac() {
            return hexAttributeValueToBytes(item.get("hmac"));
        }

        String getVersion() {
            return item.get("version").getS();
        }

        String getName() {
            return item.get("name").getS();
        }

        String getDigest() {
            return item.get("digest").getS();
        }

        private static byte[] base64AttributeValueToBytes(AttributeValue value) {
            return Base64.getDecoder().decode(value.getS());
        }

        private static byte[] hexAttributeValueToBytes(AttributeValue value) {
            ByteBuffer b = value.getB();
            try {
                if (b != null && b.remaining() > 0) {
                    // support for current versions of credstash
                    return new Hex("UTF-8").decode(value.getB().array());
                } else {
                    // support for backwards compatibility
                    return new Hex("UTF-8").decode(value.getS().getBytes("UTF-8"));
                }
            } catch (UnsupportedEncodingException | DecoderException e) {
                throw new CredStashAttributeEncodingException(value, "Attribute encoding exception", e);
            }
        }

    }

    public static String padVersion(Integer version) {
        return String.format("%019d", version);
    }

}