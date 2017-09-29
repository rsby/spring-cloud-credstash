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
    private final Map<String, Optional<String>> cache = new LinkedHashMap<>();
    private final List<String> auditLog = new ArrayList<>();
    private final String[] enumerableProperties;

    CredStashPropertySource(
            CredStash credStash,
            CredStashProperties credStashProperties,
            PathMatcher propertyMatcher) {
        super("credstash", credStash);
        this.propertyConfigs = credStashProperties.compileToOrderedList();
        this.propertyMatcher = propertyMatcher;
        this.mode = credStashProperties.getMode();
        this.enumerableProperties = getEnumerableProperties(credStashProperties);
    }

    @Override
    public Object getProperty(String propertyName) {
        propertyName = propertyName.split(":")[0];
        for (CredStashPropertyConfig config : propertyConfigs) {
            Optional<String> optionalSecretKey = getSecretKey(propertyName, config);
            if (optionalSecretKey.isPresent()) {
                String secretKey = optionalSecretKey.get();
                logger.debug("Using key [" + secretKey + "] for retrieval of property [" + propertyName + "]");
                return getSecret(propertyName, secretKey, config);
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
        String secretKey = config.getOneToOne().get(propertyName);
        if (secretKey != null) {
            return Optional.of(secretKey);
        }
        for (String matching : config.getMatching()) {
            if (propertyMatcher.match(matching, propertyName)) {
                secretKey = propertyName;
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
        Optional<String> secret = cache.get(secretKey);
        if (secret != null) {
            return secret.orElse(null);
        }
        secret = source.getSecret(
                config.getTable(),
                secretKey,
                config.getContext(),
                config.getVersion());
        cache.put(propertyName, secret);
        if (secret.isPresent()) {
            audit("Found " + propertyName + " mapped to secret key " + secretKey + " using " + config);
            return secret.get();
        }
        audit("Missing " + propertyName + " mapped to secret key " + secretKey + " using " + config);
        if (mode == CredStashProperties.Mode.PROD) {
            throw new CredStashPropertyMissingException(
                    propertyName,
                    config,
                    String.format("Property [%s] not found using config [%s]",
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

    private static String[] getEnumerableProperties(CredStashProperties credStashProperties) {
        if (!credStashProperties.getEnumerable()) {
            return new String[]{};
        }
        Set<String> enumeratedProperties = new LinkedHashSet<>();
        for (CredStashPropertyConfig config : credStashProperties.compileToOrderedList()) {
            for (String matching : config.getMatching()) {
                if (!matching.contains("*")) {
                    enumeratedProperties.add(matching);
                }
            }
            for (String oneToOne : config.getOneToOne().keySet()) {
                enumeratedProperties.add(oneToOne);
            }
        }
        return enumeratedProperties.toArray(new String[enumeratedProperties.size()]);
    }
}
