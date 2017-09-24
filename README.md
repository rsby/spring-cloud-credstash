# Spring Cloud CredStash
A read-only Spring Cloud CredStash library for retrieving application
 properties from a CredStash store.
 
# Does it load all credentials from the CredStash store?
No. The CredStashPropertySource is not an EnumerablePropertySource, therefore it
is used exclusively for overrides of properties provided elsewhere. So, 
in order to retrieve the property `my.client.secret`, the property must first be
declared in a property file or environment variable, etc, even if left blank. For instance:

    my.client.secret: 
    
# How to use and configure
As a Spring Cloud bootstrap component, all that is required to begin retrieving
secrets from a CredStash store is to add this library to your Spring Boot app and
configure the following in your `bootstrap.yml` or `application.yml`:

example `bootstrap.yml`:

    credstash:
      enabled: true
      table: "test-store"
      keyPrefix: "dev."
      propertyPatterns: "**.secret,**.password,**.key,some.property"

About each of these:

- _enabled_: set to `false` by default, this must be set to `true` in order to retrieve properties
from your CredStash store
- _table_: the DynamoDB table used for CredStash. Defaults to `credential-store`.
- _keyPrefix_: A prefix that will be appended to the property name for building the key name for the 
secret. The default is empty string `""`. This can be used to fetch different values for the same
property in different environments. Useful when using a single store across multiple environments.
- _propertyPattern_: A comma-separated list of Ant patterns for filtering which properties to fetch. 

# All default settings

    credstash.enabled: false
    credstash.table: "credential-store"
    credstash.keyPrefix: ""
    credstash.propertyPatterns: "**.secret,**.password,**.key"
    credstash.pathSeparator: "."