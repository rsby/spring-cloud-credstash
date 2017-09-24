package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;

/**
 * Represents a row in a credstash table. The encrypted key and encrypted contents are both stored base64 encoded.
 * The hmac digest is stored hex encoded.
 */
class StoredSecret {

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
