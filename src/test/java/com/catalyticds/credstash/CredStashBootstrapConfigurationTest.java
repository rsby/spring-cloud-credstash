package com.catalyticds.credstash;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author reesbyars on 9/22/17.
 */
@RunWith(SpringRunner.class)
@PropertySource("classpath:test.properties")
@SpringBootApplication
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
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

    @Autowired
    CredStash credStash;

    @Autowired
    CredStashProperties properties;

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
    }

}