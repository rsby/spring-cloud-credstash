package com.catalyticds.credstash;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author reesbyars on 2/25/19.
 */
public class SecretPropertySource extends EnumerablePropertySource<DecryptedSecret> {

    private final Map<String, Object> source;
    private final String[] names;

    SecretPropertySource(
            CredStash credStash,
            CredStashPropertyConfig propertyConfig,
            SecretPropertySourceConfig sourceConfig) {
        super(sourceConfig.getSecretName());
        String secretName = sourceConfig.getSecretName();
        SecretRequest request = new SecretRequest(secretName)
                .withContext(propertyConfig.getContext())
                .withTable(propertyConfig.getTable())
                .withVersion(propertyConfig.getVersion());
        Optional<DecryptedSecret> optionalSecret = credStash.getSecret(request);
        if (optionalSecret.isPresent()) {
            byte[] secretBytes = optionalSecret.get().getSecret().getBytes(StandardCharsets.UTF_8);
            Resource resource = new ByteArrayResource(secretBytes, secretName);
            source = new Processor(resource).process();
            names = source.keySet().toArray(new String[0]);
            logger.info("Loaded " + names.length + " properties from CredStash source '" + secretName + "'");
        } else if (sourceConfig.isRequired()) {
            throw new CredStashMissingPropertySourceException(sourceConfig);
        } else {
            logger.info("Failed to load CredStash property source: " + sourceConfig);
            source = Collections.emptyMap();
            names = new String[0];
        }
    }

    @Override
    public String[] getPropertyNames() {
        return names;
    }

    @Override
    public Object getProperty(String name) {
        return source.get(name);
    }

    static class Processor extends YamlProcessor {

        Processor(Resource resource) {
            setResources(resource);
        }

        Map<String, Object> process() {
            final Map<String, Object> result = new LinkedHashMap<>();
            process((properties, map) -> result.putAll(getFlattenedMap(map)));
            return result;
        }

    }

}
