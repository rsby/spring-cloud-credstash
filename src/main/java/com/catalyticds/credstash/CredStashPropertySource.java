package com.catalyticds.credstash;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author reesbyars on 9/21/17.
 */
class CredStashPropertySource extends EnumerablePropertySource<CredStash> {

    private final List<CredStashPropertyConfig> propertyConfigs;
    private final PathMatcher propertyMatcher;
    private final CredStashProperties.Mode mode;
    private final Map<String, Optional<String>> cache = new LinkedHashMap<>();
    private final List<String> auditLog = new ArrayList<>();

    CredStashPropertySource(
            CredStash credStash,
            List<CredStashPropertyConfig> propertyConfigs,
            PathMatcher propertyMatcher,
            CredStashProperties.Mode mode) {
        super("credstash", credStash);
        this.propertyConfigs = propertyConfigs;
        this.propertyMatcher = propertyMatcher;
        this.mode = mode;
    }

    @Override
    public Object getProperty(String propertyName) {
        propertyName = propertyName.split(":")[0];
        for (CredStashPropertyConfig config : propertyConfigs) {
            if (config.getEnabled()) {
                for (String matching : config.getMatching()) {
                    if (propertyMatcher.match(matching, propertyName)) {
                        String secretName = propertyName;
                        if (!StringUtils.isEmpty(config.getStripPrefix())) {
                            secretName = secretName.replace(config.getStripPrefix(), "");
                        }
                        secretName = config.getAddPrefix() + secretName;
                        Optional<String> secret = cache.get(secretName);
                        if (secret != null) {
                            return secret.orElse(null);
                        }
                        secret = source.getSecret(
                                config.getTable(),
                                secretName,
                                config.getContext(),
                                config.getVersion());
                        cache.put(propertyName, secret);
                        if (secret.isPresent()) {
                            auditLog.add("\n   Found " + propertyName + " mapped to secret key " +
                                    secretName + " using " + config);
                            return secret.get();
                        }
                        auditLog.add("\n   Missing " + propertyName + " mapped to secret key " +
                                secretName + " using " + config);
                        if (mode == CredStashProperties.Mode.PROD) {
                            throw new CredStashPropertyMissingException(
                                    propertyName,
                                    config,
                                    String.format("Property [%s] not found using config [%s]",
                                            propertyName,
                                            config));
                        }
                    }
                }
            }
        }
        return null;
    }

    @EventListener(ContextRefreshedEvent.class)
    void onContextRefreshed() {
        if (mode == CredStashProperties.Mode.AUDIT) {
            logger.info("CredStash audit log ==> " + auditLog);
        }
        auditLog.clear();
        cache.clear();
    }

    @Override
    public String[] getPropertyNames() {
        return new String[0];
    }
}
