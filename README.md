# Spring Cloud CredStash
A read-only Spring Cloud library for retrieving application
 properties from a CredStash store. As opposed to a Docker/container solution, Spring Cloud CredStash
 enables full Spring property, profile and Boot integration, reducing
 the friction for per-environment and local developer configuration. This maintains
  the lightweight _right-click run_ Boot developer experience while reducing the Dev ops
  configuration management effort and boiling the Dev Ops CredStash configuration down
  to a single property or two at most.
 
### Does it load all credentials from the CredStash store?
No. The CredStashPropertySource is not an EnumerablePropertySource, therefore it
is used exclusively for overrides of properties declared or requested elsewhere. 
 
### Simple use and configuration - credential store per environment or VPC
As a Spring Cloud bootstrap component, all that is required to begin retrieving
secrets from a CredStash store is to add this library to your Spring Boot app and
configure the following in your `bootstrap.yml` or `application.yml`:

    credstash:
      enabled: true
      table: "qa-credential-store"
      matching:
       - "auth.idp.client.secret"
       - "spring.datasource.password"

### Simple use and configuration - single credential store with keys for environment or VPC

    credstash:
      enabled: true
      add_prefix:   "dev_"
      matching:
       - "auth.idp.client.secret"
       - "spring.datasource.password"


### Further use and configuration

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

### Fine tuning
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
    credstash.add_prefix:       ""
    credstash.strip_prefix:     ""
    credstash.matching:         ""                      # matches none
    credstash.version:          null                    # null resolves to "latest"
    credstash.context:          null
    credstash.pathSeparator:    "."                     # separator for Ant matching
    credstash.mode:             PROD                    # can be set to AUDIT to log CredStash retrieval details

### Debugging

To debug issues when first configuring CredStash in an environment, `credstash.mode: AUDIT` can be set. This causes 
two things:

   1) Failure to locate a property in CredStash does not break deployment (in the default PROD mode, it does break deployment)
   1) A list of attempted property retrievals is logged, including both successful and unsuccessful retrievals
