package com.catalyticds.credstash;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @author reesbyars on 9/22/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CredStashBootstrapConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CredStashBootstrapConfigurationTest {

    @Value("${test.secret}")
    String secret;

    @Value("${test.test}")
    String test;

    @Autowired
    CredStash credStash;

    @Autowired
    CredStashProperties properties;

    @Test
    public void test() {
        assertEquals("test", test);
        assertEquals(MockCredStashConfiguration.credStashValue, secret);
    }

}