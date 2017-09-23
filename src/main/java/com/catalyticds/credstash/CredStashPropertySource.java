package com.catalyticds.credstash;

import org.springframework.core.env.PropertySource;
import org.springframework.util.PathMatcher;

/**
 * @author reesbyars on 9/21/17.
 */
class CredStashPropertySource extends PropertySource<CredStash> {

    private final CredStashProperties properties;
    private final PathMatcher propertyMatcher;

    CredStashPropertySource(
            CredStash credStash,
            CredStashProperties properties,
            PathMatcher propertyMatcher) {
        super("credstash", credStash);
        this.properties = properties;
        this.propertyMatcher = propertyMatcher;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (properties.getEnabled() && propertyMatcher.match(
                properties.getPropertyPattern(), propertyName)) {
            return source
                    .getSecret(properties.getKeyPrefix() + propertyName)
                    .orElse(null);
        }
        return null;
    }

}
