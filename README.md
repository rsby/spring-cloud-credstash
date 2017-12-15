# Spring Cloud CredStash
A read-only Spring Cloud library for retrieving application
 properties from a CredStash store. As opposed to a Docker/container solution, Spring Cloud CredStash
 enables full Spring property, profile and Boot integration, reducing
 the friction for per-environment and local developer configuration.
 
### Does it load all credentials from the CredStash store?
No. Loading all properties requires table scanning permission in DynamoDB, and we elected not to require
that permission. However, some properties, such as `spring.datasource.password`, may need to be enumerated in order
to be set. In those cases, explicitly listing the property in the yaml config (shown below) will suffice. 
 
### Basic use and configuration
As a Spring Cloud bootstrap component, all that is required to begin retrieving
secrets from a CredStash store is to add this library to your Spring Boot app and
configure the following in your `bootstrap.yml` or `application.yml`:

    credstash:
      enabled: true # the default is 'false' - it must be explicitly enabled
      table: test-store # the default is 'credential-store'

This would then set the `my.secret` property to the value of the `MY_SECRET` credstash secret:
    
      my.secret=${credstash__MY_SECRET}

### Simple use and configuration - single credential store with keys for environment or VPC

    credstash:
      enabled: true
      add_prefix:   "dev_" # this would be added to the crestash key names as a key prefix
            
This would then set the `my.secret` property to the value of the `dev_MY_SECRET` credstash secret:
    
      my.secret=${credstash__MY_SECRET}

### Adding context

    credstash:
      enabled:      true
      context:
        my-app: "my-context"
               
This would then match:
    
      my.secret=${credstash__MY_SECRET} # this then includes the declared context
     
### Sub-configs

    credstash:
      enabled: true
      properties:
        - name: "credstash_my_app__*.**"
          stripPrefix: "credstash_my_app__"
          context:
            my-app: key
            
This would then match:
    
      my.secret=${credstash__MY_SECRET}
      my.app.secret=${credstash_my_app__MY_APP_SECRET}

### More about the settings

- _enabled_: set to `false` by default, this must be set to `true` in order to retrieve properties
from your CredStash store
- _table_: the DynamoDB table used for CredStash. Defaults to `credential-store`.
- _addPrefix_: A prefix that will be appended to the property name for building the key name for the 
secret. The default is empty string `""`. This can be used to fetch different values for the same
property in different environments. Useful when using a single store across multiple environments.
- _stripPrefix_: A string to strip from the original Spring property name when converting to a CredStash key.
- _matching_: An Ant pattern for filtering which properties to fetch. 

### All default settings

    credstash.enabled:          false                   # not enabled by default (except for "child" configs)
    credstash.table:            "credential-store"
    credstash.add_prefix:       ""
    credstash.strip_prefix:     "credstash__"
    credstash.matching:         "credstash__*.**"
    credstash.version:          null                    # null resolves to "latest"
    credstash.context:          null
    credstash.pathSeparator:    "."                     # separator for Ant matching
    credstash.mode:             PROD                    # can be set to AUDIT to log CredStash retrieval details

### Debugging

To debug issues when first configuring CredStash in an environment, `credstash.mode: AUDIT` can be set. This causes 
two things:

   1) Failure to locate a property in CredStash does not break deployment (in the default PROD mode, it does break deployment)
   1) A list of attempted property retrievals is logged, including both successful and unsuccessful retrievals
