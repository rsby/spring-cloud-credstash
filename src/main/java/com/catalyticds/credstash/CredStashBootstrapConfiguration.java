package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.AntPathMatcher;

/**
 * @author reesbyars on 9/22/17.
 */
@Configuration
@EnableConfigurationProperties(CredStashProperties.class)
@ConditionalOnProperty(prefix = "credstash", name = "enabled", havingValue = "true")
public class CredStashBootstrapConfiguration implements InitializingBean {

    private final ConfigurableEnvironment env;
    private final CredStashProperties credStashProperties;

    CredStashBootstrapConfiguration(
            ConfigurableEnvironment env,
            CredStashProperties credStashProperties) {
        this.env = env;
        this.credStashProperties = credStashProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        env.getPropertySources().addFirst(credStashPropertySource());
    }

    @Bean
    PropertySource credStashPropertySource() {
        return new CredStashPropertySource(
                credStash(),
                credStashProperties,
                new AntPathMatcher(credStashProperties.getPathSeparator()));
    }

    @Bean
    @ConditionalOnMissingBean
    CredStash credStash() {
        return new CredStash(
                amazonDynamoDB(),
                awskms(),
                credStashCrypto(),
                credStashProperties.getTable());
    }

    @Bean
    @ConditionalOnMissingBean
    AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }

    @Bean
    @ConditionalOnMissingBean
    AWSKMS awskms() {
        return AWSKMSClientBuilder.defaultClient();
    }

    @Bean
    @ConditionalOnMissingBean
    CredStashCrypto credStashCrypto() {
        return new CredStashBouncyCastleCrypto();
    }

}
