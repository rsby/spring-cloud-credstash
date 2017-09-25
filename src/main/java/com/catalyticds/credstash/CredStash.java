package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;

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

    /**
     * Gets a secret from credstash.
     *
     * @param tableName the dynamo table name (likely "credential-store")
     * @param secretName the name of the secret to get
     * @return unencrypted secret
     */
    public Optional<String> getSecret(String tableName, String secretName)  {
        return getSecret(tableName, secretName, null, null);
    }

    /**
     * Gets a secret from credstash.
     *
     * @param tableName the dynamo table name (likely "credential-store")
     * @param secretName the name of the secret to get
     * @param context encryption context key/value pairs associated with the credential in the form of "key=value"
     * @return unencrypted secret
     */
    public Optional<String> getSecret(String tableName, String secretName, Map<String, String> context)  {
        return getSecret(tableName, secretName, context, null);
    }

    /**
     * Gets a secret from credstash with a specified version
     *
     * @param tableName the dynamo table name (likely "credential-store")
     * @param secretName the name of the secret to get
     * @param context encryption context key/value pairs associated with the credential in the form of "key=value"
     * @param version a particular version string to lookup (null for latest version)
     * @return unencrypted secret
     */
    public Optional<String> getSecret(
            String tableName, String secretName, Map<String, String> context, String version)  {
        // First find the relevant rows from the credstash table
        StoredSecret encrypted = version == null ?
                readHighestVersionDynamoItem(tableName, secretName) :
                readVersionedDynamoItem(tableName, secretName, version);
        if (encrypted == null) {
            return Optional.empty();
        }
        return getStoredSecret(encrypted, context);
    }

    Optional<String> getStoredSecret(StoredSecret encrypted, Map<String, String> context)  {

        // The secret was encrypted using AES, then the key for that encryption was encrypted with AWS KMS
        // Then both the encrypted secret and the encrypted key are stored in dynamo

        // First obtain that original key again using KMS
        ByteBuffer plainText = decryptKeyWithKMS(encrypted.getKey(), context);

        // The key is just the first 32 bits, the remaining are for HMAC signature checking
        byte[] keyBytes = new byte[32];
        plainText.get(keyBytes);

        byte[] hmacKeyBytes = new byte[plainText.remaining()];
        plainText.get(hmacKeyBytes);
        byte[] encryptedContents = encrypted.getContents();
        byte[] digest = credStashCrypto.digest(hmacKeyBytes, encryptedContents);
        if (!Arrays.equals(digest, encrypted.getHmac())) {
            throw new CredStashSignatureException(
                    encrypted.getName(),
                    encrypted.getVersion(),
                    "HMAC integrity check failed");
        }

        // now use AES to finally decrypt the actual secret
        byte[] decryptedBytes = credStashCrypto.decrypt(keyBytes, encryptedContents);
        return Optional.of(new String(decryptedBytes));
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

    private ByteBuffer decryptKeyWithKMS(byte[] encryptedKeyBytes, Map<String, String> context) {
        ByteBuffer blob = ByteBuffer.wrap(encryptedKeyBytes);
        DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(blob);
        if (context != null) {
            decryptRequest.withEncryptionContext(context);
        }
        DecryptResult decryptResult = awsKmsClient.decrypt(decryptRequest);
        return decryptResult.getPlaintext();
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

}