package com.catalyticds.credstash;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.kms.AWSKMS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author reesbyars on 9/22/17.
 */
@RunWith(SpringRunner.class)
@PropertySource("classpath:test.properties")
@SpringBootApplication
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class CredStashBootstrapConfigurationTest {

    @Value("${test.secret}")
    String secret;

    @Value("${test.pass}")
    String pass;

    @Value("${test.key}")
    String key;

    @Value("${test.test}")
    String test;

    @Value("${my.exact.match}")
    String exactMatch;

    @Value("${test.fromFile}")
    String fromPropertySourceFile;

    @Value("${test.fromValue:from_value}")
    String fromValueAnnotation;

    @Value("${test.fromValueNoDefault}")
    String fromValueAnnotationNoDefault;

    @Value("${test.oneToOne}")
    String oneToOne;

    @Value("${credstash__SOME_KEY}")
    String defaultPrefixMatching1;

    @Value("${credstash__some.key}")
    String defaultPrefixMatching2;

    @Value("${try.default}")
    String defaultVal;

    @Value("${test.test.test}")
    String fromPropertySource;

    @Autowired
    CredStash credStash;

    @Autowired
    CredStashProperties properties;

    @Autowired
    CredStashTextEncryptor encryptor;

    @Test
    public void test() {
        assertEquals("test", test);
        assertEquals(MockCredStashConfiguration.credStashValue, secret);
        assertEquals(MockCredStashConfiguration.credStashValue, pass);
        assertEquals(MockCredStashConfiguration.credStashValue, key);
        assertEquals(MockCredStashConfiguration.credStashValue, exactMatch);
        assertEquals(MockCredStashConfiguration.credStashValue, fromPropertySourceFile);
        assertEquals(MockCredStashConfiguration.credStashValue, fromValueAnnotation);
        assertEquals(MockCredStashConfiguration.credStashValue, fromValueAnnotationNoDefault);
        assertEquals(MockCredStashConfiguration.credStashValue, oneToOne);
        assertEquals(MockCredStashConfiguration.credStashValue, defaultPrefixMatching1);
        assertEquals(MockCredStashConfiguration.credStashValue, defaultPrefixMatching2);
        assertEquals("from_property_source", fromPropertySource);
        assertEquals("default", defaultVal);
        assertEquals(
                "test",
                encryptor.decrypt("0::b4ab1bff72df5a3d::547294a955fa5550cb6684afda85112be57c69f9a5d6d8009db622a3dcc6ca6d35b7e274"));
    }

}