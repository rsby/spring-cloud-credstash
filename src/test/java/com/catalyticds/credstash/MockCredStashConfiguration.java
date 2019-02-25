package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.util.IOUtils;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;

import java.util.Optional;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author reesbyars on 9/22/17.
 */
@Configuration
public class MockCredStashConfiguration {

    static String credStashValue = "from_credstash";

    @Bean @Primary CredStash credStash() {
        return mock(CredStash.class);
    }

    @Bean AmazonDynamoDB amazonDynamoDB() {
        return mock(AmazonDynamoDB.class);
    }

    @Bean AWSKMS awskms() {
        return mock(AWSKMS.class);
    }

    @PostConstruct
    public void init() {
        when(credStash().getSecret(any())).thenAnswer((Answer<Optional<DecryptedSecret>>) invocation -> {
            Object[] args = invocation.getArguments();
            SecretRequest request = (SecretRequest) args[0];
            String secret = credStashValue;
            if (request.getSecretName().equals("test_source")) {
                secret = IOUtils.toString(new ClassPathResource("from_credstash.yaml").getInputStream());
            } else if (request.getSecretName().toLowerCase().contains("missing")) {
                return Optional.empty();
            }
            return Optional.of(new DecryptedSecret(
                    request.getTable(),
                    request.getSecretName(),
                    request.getVersion().orElse("0"),
                    secret));
        });
    }

}