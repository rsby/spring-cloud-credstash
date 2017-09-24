package com.catalyticds.credstash;

import org.springframework.core.env.PropertySource;
import org.springframework.util.PathMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author reesbyars on 9/21/17.
 */
class CredStashPropertySource extends PropertySource<CredStash> {

    private final CredStashProperties properties;
    private final PathMatcher propertyMatcher;
    private final List<String> propertyPatterns = new ArrayList<>();

    CredStashPropertySource(
            CredStash credStash,
            CredStashProperties properties,
            PathMatcher propertyMatcher) {
        super("credstash", credStash);
        this.properties = properties;
        this.propertyMatcher = propertyMatcher;
        Collections.addAll(propertyPatterns, properties.getPropertyPatterns().split(","));
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing CredStash property source with ==> " + properties);
        }
    }

    @Override
    public Object getProperty(String propertyName) {
        if (properties.getEnabled()) {
            for (String propertyPattern : propertyPatterns) {
                if (propertyMatcher.match(propertyPattern, propertyName)) {
                    String secretName = properties.getKeyPrefix() + propertyName;
                    return source.getSecret(secretName)
                            .orElseThrow(() ->
                                    new CredStashPropertyMissingException(
                                            propertyName,
                                            secretName,
                                            String.format("Property [%s] not found using key [%s]",
                                                    propertyName,
                                                    secretName)));
                }
            }
        }
        return null;
    }

}
