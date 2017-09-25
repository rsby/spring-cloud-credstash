# Spring Cloud CredStash
A read-only Spring Cloud CredStash library for retrieving application
 properties from a CredStash store. As opposed to a Docker/container solution, Spring Cloud CredStash
 enables full Spring property, profile and Boot integration, reducing
 the friction for per-environment and local developer configuration. This maintains
  the lightweight _right-click run_ Boot developer experience while reducing the Dev ops
  configuration management effort and boiling the Dev Ops CredStash configuration down
  to a single property or two at most.
 
# Does it load all credentials from the CredStash store?
No. The CredStashPropertySource is not an EnumerablePropertySource, therefore it
is used exclusively for overrides of properties declared elsewhere. So, 
in order to retrieve the property `my.client.secret`, the property must first be
declared in a property file or environment variable, etc, even if left blank. For instance:

    my.client.secret: 
    
# How to use and configure
As a Spring Cloud bootstrap component, all that is required to begin retrieving
secrets from a CredStash store is to add this library to your Spring Boot app and
configure the following in your `bootstrap.yml` or `application.yml`:

example `bootstrap.yml`:

    credstash:
      enabled:      true
      table:        "test-store"
      add_prefix:   "dev_"
      strip_prefix: "credstash__"
      matching:     "credstash__*"
      context:
        my-app: "my-context"
      
This config will trigger loading of Spring properties such as `credstash__user.svc.db.pass` and load them
from CredStash using the key `dev_user.svc.db.pass`. The context is optional.

More about the settings:

- _enabled_: set to `false` by default, this must be set to `true` in order to retrieve properties
from your CredStash store
- _table_: the DynamoDB table used for CredStash. Defaults to `credential-store`.
- _addPrefix_: A prefix that will be appended to the property name for building the key name for the 
secret. The default is empty string `""`. This can be used to fetch different values for the same
property in different environments. Useful when using a single store across multiple environments.
- _stripPrefix_: A string to strip from the original Spring property name when converting to a CredStash key.
- _matching_: An Ant pattern for filtering which properties to fetch. 

# Fine tuning
The yaml config also supports setting "child" configs that will inherit from the main settings. For instance:

    credstash:
      enabled: true
      addPrefix: "dev_"
      stripPrefix: "credstash__"
      matching: "credstash__*"
      more:
        user_service_pass:
          matching: "credstash__user.svc.db.pass"
          version: "2"

In this config, most properties would be satisfied with the latest secret version in CredStash, but
the user service DB password would still be on version 2. These "child" configs must all be part of the `more` field.
Any field can be overridden in the child configs, except for `enabled` and `pathSeparator`, which can only be set in 
the default config.

# All default settings

    credstash.enabled:          false
    credstash.table:            "credential-store"
    credstash.add_prefix:       ""
    credstash.strip_prefix:     ""
    credstash.matching:         ""
    credstash.version:          null
    credstash.context:          null
    credstash.pathSeparator:    "."