package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

/**
 * @author reesbyars on 9/23/17.
 */
public class CredStashAttributeEncodingException extends RuntimeException {

    private final AttributeValue attributeValue;

    CredStashAttributeEncodingException(
            AttributeValue attributeValue, String message, Throwable cause) {
        super(message, cause);
        this.attributeValue = attributeValue;
    }

    public AttributeValue getAttributeValue() {
        return attributeValue;
    }
}
