package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.kms.AWSKMS;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
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
        when(credStash().getSecret(argThat(new ArgumentMatcher<SecretRequest>() {
            @Override
            public boolean matches(Object item) {
                return item != null && ((SecretRequest) item).getSecretName().toLowerCase().contains("missing");
            }
            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
            }
            @Override
            public void describeTo(Description description) {
            }
        }))).thenReturn(Optional.empty());

        when(credStash().getSecret(argThat(new ArgumentMatcher<SecretRequest>() {
            @Override
            public boolean matches(Object item) {
                return item != null && !((SecretRequest) item).getSecretName().toLowerCase().contains("missing");
            }
            @Override
            public void describeMismatch(Object item, Description mismatchDescription) {
            }
            @Override
            public void describeTo(Description description) {
            }
        }))).thenReturn(Optional.of(new DecryptedSecret(
                "table",
                "name",
                "version",
                credStashValue)));
    }

}