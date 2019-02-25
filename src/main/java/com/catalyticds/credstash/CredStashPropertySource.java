package com.catalyticds.credstash;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author reesbyars on 9/21/17.
 */
class CredStashPropertySource extends EnumerablePropertySource<CredStash> {

    private final List<CredStashPropertyConfig> propertyConfigs;
    private final PathMatcher propertyMatcher;
    private final CredStashProperties.Mode mode;
    private final Map<String, Optional<DecryptedSecret>> cache = new LinkedHashMap<>();
    private final List<String> auditLog = new ArrayList<>();
    private final String[] enumerableProperties;

    CredStashPropertySource(
            CredStash credStash,
            List<CredStashPropertyConfig> propertyConfigs,
            CredStashProperties.Mode mode,
            PathMatcher propertyMatcher) {
        super("credstash", credStash);
        this.propertyConfigs = propertyConfigs;
        this.mode = mode;
        this.propertyMatcher = propertyMatcher;
        this.enumerableProperties = getEnumerableProperties();
    }

    @Override
    public Object getProperty(String propertyName) {
        propertyName = propertyName.split(":")[0];
        for (CredStashPropertyConfig config : propertyConfigs) {
            Optional<String> optionalSecretKey = getSecretKey(propertyName, config);
            if (optionalSecretKey.isPresent()) {
                return getSecret(propertyName, optionalSecretKey.get(), config);
            }
        }
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        return enumerableProperties;
    }

    @EventListener({ContextRefreshedEvent.class, ContextClosedEvent.class})
    void onContextRefreshed() {
        if (mode == CredStashProperties.Mode.AUDIT) {
            logger.info("CredStash audit log ==> " + auditLog);
        }
        auditLog.clear();
        cache.clear();
    }

    private Optional<String> getSecretKey(String propertyName, CredStashPropertyConfig config) {
        if (!config.getEnabled()) {
            return Optional.empty();
        }
        for (CredStashPropertyConfig.PropertyEntry entry : config.getMatching()) {
            if (propertyMatcher.match(entry.getPattern(), propertyName)) {
                String secretKey = propertyName;
                if (!StringUtils.isEmpty(entry.getKey())) {
                    secretKey = entry.getKey();
                }
                if (!StringUtils.isEmpty(config.getStripPrefix())) {
                    secretKey = secretKey.replace(config.getStripPrefix(), "");
                }
                secretKey = config.getAddPrefix() + secretKey;
                return Optional.of(secretKey);
            }
        }
        return Optional.empty();
    }

    private String getSecret(String propertyName, String secretKey, CredStashPropertyConfig config) {
        Optional<DecryptedSecret> secret = cache.get(secretKey);
        if (secret != null) {
            return secret.isPresent() ? secret.get().getSecret() : null;
        }
        SecretRequest request = new SecretRequest(secretKey)
                .withTable(config.getTable())
                .withVersion(config.getVersion())
                .withContext(config.getContext());
        logger.debug("Retrieving property [" + propertyName + "] with request [" + request + "]");
        try {
            secret = source.getSecret(request);
        } catch (Exception e) {
            logger.error("Failed retrieving secret " + request, e);
            secret = Optional.empty();
        }
        cache.put(propertyName, secret);
        if (secret.isPresent()) {
            audit("Found " + propertyName + " using " + request + " built from " + config);
            return secret.get().getSecret();
        }
        audit("Missing " + propertyName + " using " + request + " built from " + config);
        if (mode == CredStashProperties.Mode.PROD) {
            logger.debug(String.format("Property [%s] not found using config [%s]",
                    propertyName,
                    config));
        }
        return null;
    }

    private void audit(String entry) {
        if (logger.isDebugEnabled()) {
            logger.debug(entry);
        }
        auditLog.add("\n    " + entry);
    }

    private String[] getEnumerableProperties() {
        Set<String> enumeratedProperties = new LinkedHashSet<>();
        for (CredStashPropertyConfig config : propertyConfigs) {
            if (config.getEnumerable()) {
                for (CredStashPropertyConfig.PropertyEntry entry : config.getMatching()) {
                    if (!entry.getPattern().contains("*")) {
                        enumeratedProperties.add(entry.getPattern());
                    }
                }
            }
        }
        return enumeratedProperties.toArray(new String[enumeratedProperties.size()]);
    }
}
