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

    private Map<String, AttributeValue> item;

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
        byte[] attr;

        try {
            if (b != null && b.remaining() > 0) {
                // support for current versions of credstash
                attr = value.getB().array();
            } else {
                // support for backwards compatibility
                attr = value.getS().getBytes("UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        Hex hexDecoder = new Hex("UTF-8");
        try {
            return hexDecoder.decode(attr);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

}
