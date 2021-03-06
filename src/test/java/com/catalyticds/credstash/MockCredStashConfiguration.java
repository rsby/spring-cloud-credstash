package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.kms.AWSKMS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
        when(credStash().getSecret(any()))
                .thenReturn(Optional.of(new DecryptedSecret(
                        "table",
                        "name",
                        "version",
                        credStashValue)));
    }

}