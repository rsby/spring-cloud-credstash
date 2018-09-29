# Spring Cloud CredStash
A read-only Spring Cloud library for retrieving application
 properties from a CredStash store. This keeps the container size smaller 
 (as opposed to installing Python just to read CredStash) and enables straight-forward secret and property 
 access via spring property management in the form:
 
    my.secret=${credstash__MY_SECRET}
    my.other.secret=${credstash__MY_OTHER_SECRET:default if missing}

### Basic use and configuration - credential store per environment or VPC
As a Spring Cloud bootstrap component, all that is required to begin retrieving
secrets from a CredStash store is to add this library to your Spring Boot app and
configure the following in your `bootstrap.yml` or `application.yml`:

    credstash:
      enabled: true
      table: "qa-credential-store"

### More complex use cases
The yaml config also supports setting "child" configs that will inherit from the main settings. For instance:

    credstash:
      enabled: true
      properties:
        user_service_pass:
          matching: "credstash__user.svc.db.pass"
          version: "2"
          context:
            my-app: "my-context"
        external_file_store_pass:
          enabled: false
          matching: "external.file.store.pass"

In this config, most properties would be satisfied with the latest secret version in CredStash, but
the user service DB password would still be on version 2. These "child" configs must all be part of the `more` field.
Any field can be overridden in the child configs, except for `pathSeparator`, which can only be set in 
the default config. For child configs, `enabled` defaults to true, but it can be set to false. This is useful
if using a centralized `bootstrap.yml` across multiple micro services - child configs can be toggled on and
off via environment variables, enabling ease of defining the `spring.datasource.password` per service in
a centralized config and then only turning on the child config for a given service via its env args.

### All default settings

    credstash.enabled:          false                   # not enabled by default (except for "child" configs)
    credstash.table:            "credential-store"
    credstash.add_prefix:       ""                      # if to, e.g., "qa_", the qa_ would be prepended to the secret name
    credstash.strip_prefix:     "credstash__"           # strips credstash__ from the property name to obtain the secret name
    credstash.matching:         "credstash__*.**"       # matches properties beginning with credstash__
    credstash.version:          null                    # null resolves to "latest"
    credstash.context:          null
    credstash.mode:             PROD                    # can be set to AUDIT to log CredStash retrieval details

# Text Encryptor

The CredStashTextEncryptor enables versioned text encryption built on top of Spring's HexEncodingTextEncryptor.
This can be used to implement the second layer of 2-layer encryption at rest (first layer being DB encryption) 
or in transit (first layer being SSL/TLS). The encrypted values contain the version of the secret used to encrypt them, 
such that the secret can be updated on a schedule, and the encryptor will use the latest version for encrypting yet 
will still decrypt using the version used to previously encrypt values. It is also possible to specify a default 
secret version other than latest. An example configuration in your `bootstrap.yml`:

    credstash:
      encryptor:
        secret:
          name: "encryptor_secret"
          
# Beans

When enabled, the CredStash and CredStashTextEncryptor beans are available in the application context to be wired
into your classes and used directly.
