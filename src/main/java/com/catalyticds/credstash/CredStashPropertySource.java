package com.catalyticds.credstash;

import org.springframework.core.env.PropertySource;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author reesbyars on 9/21/17.
 */
class CredStashPropertySource extends PropertySource<CredStash> {

    private final List<CredStashPropertyConfig> propertyConfigs;
    private final PathMatcher propertyMatcher;

    CredStashPropertySource(
            CredStash credStash,
            List<CredStashPropertyConfig> propertyConfigs,
            PathMatcher propertyMatcher) {
        super("credstash", credStash);
        this.propertyConfigs = propertyConfigs;
        this.propertyMatcher = propertyMatcher;
    }

    @Override
    public Object getProperty(String propertyName) {
        for (CredStashPropertyConfig config : propertyConfigs) {
            if (propertyMatcher.match(config.getMatching(), propertyName)) {
                String secretName = propertyName;
                if (!StringUtils.isEmpty(config.getStripPrefix())) {
                    secretName = secretName.replace(config.getStripPrefix(), "");
                }
                secretName = config.getAddPrefix() + secretName;
                return source.getSecret(config.getTable(), secretName, config.getContext(), config.getVersion())
                        .orElseThrow(() ->
                                new CredStashPropertyMissingException(
                                        propertyName,
                                        config,
                                        String.format("Property [%s] not found using config [%s]",
                                                propertyName,
                                                config)));
            }

        }
        return null;
    }

}
