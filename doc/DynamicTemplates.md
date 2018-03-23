# Dynamic Templates with Variables

It is possible to define sets of variables and make them available as part of the Request Object (under the variable name `var`, by default), so that they can be used in generating dynamic stub content.

To achieve this, the freemarker variable repository extension (already part of the wiremock-freemarker-extension library) needs to be activated so that additional `__admin` endpoints are made available to manage the variables (creating and updating variable sets, for example).

To activate the variable repository:

* **For Unit Tests**

  ```java
             .
             .  
  import org.junit.Rule;
  import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
  import com.github.tomakehurst.wiremock.junit.WireMockRule;
  import com.mindprogeny.wiremock.extension.freemarker.FreemarkerResponseTransformer;
  import com.mindprogeny.wiremock.extension.freemarker.FreemarkerVariableRepositoryManager;
             .
             .

  @Rule
  public WireMockRule wiremock = 
         new WireMockRule(WireMockConfiguration.wireMockConfig()
                                               .port(55080)
                                               .extensions(new FreemarkerResponseTransformer())
                                               .extensions(new FreemarkerVariableRepositoryManager()));
  ```
  Where 55080 would be the port where the wiremock server would be listening.

* **For Standalone Wiremock**

  Run wiremock using the following minimal command (feel free to add any additional wiremock specific flags you need):
  ```sh
  java -cp "wiremock-standalone-2.14.0.jar:wiremock-freemarker-extension-0.0.2.jar" \
       com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
       --port 8080 --https-port 8443 \
       --extensions "com.mindprogeny.wiremock.extension.freemarker.FreemarkerResponseTransformer,com.mindprogeny.wiremock.extension.freemarker.FreemarkerVariableRepositoryManager"
  ```

This will activate the variable repository management extension, providing the following operations to interact with the variable repository:

* Create/Update Variables
* Create/Update a specific Variable Set
* Retrieve Variable Sets
* Retrieve a specific Variable Set
* Delete Variable Sets
* Delete a specific Variable Set

## Variable Repository Structure

The variables are stored in the repository as a map using a hierarchical structure of two levels, made up of the variable set name and for each variable set the actual set of variables to be used (which in turn can also be maps and so on). This allows to have multiple variable sets that aggregates the actual variables of the set independently of other variable sets, each one used in their own stubs. The variables contained in a set are an actual json object, so it can also contain a deeper hierarchy of variable names and values.

The structure is such that:

```
{
  "<set_0>" : {
    "<variable_0_0>": <value>
               .
               .
   ,"<variable_0_y>": <value>
  }
  .
  .
 ,"<set_n>": {
    "variable_n_0>": <value>
              .
              .
   ,"variable_n_z>": <value>
}
```

Where `<value>` may be an array, a literal or another map.

## Create or Update Variables

Variable sets can be created/updated sending a post request to the endpoint "/__admin/variables". For example, assuming a running instance in the localhost on port 8080:

```sh
curl -d '\
{ \
  "set1" : { \
    "uno" : { \
      "name" : "miguel", \
      "profession" : "matador" \
    }, \
    "dos" : { \
      "name" : "manuel", \
      "profession" : "toro" \
     } \
   }, \
   "set2" : { \
     "tres" : { \
       "name" : "juan", \
       "profession" : "luchador" \
     }, \
     "cuatro" : { \
       "name" : "jose", \
       "profession" : "batido" \
     } \
  } \
}' http://localhost:8080/__admin/variables
```

Will create two variable sets, set1 and set2, each with its own set of variable hierarchies. Sets don't have to follow the same the structure as they can be used for completely different purposes. A stub can be bound to a specific variable set, having only the variables under that set available for processing (see later examples for setting up stubs with specific variable sets).

This method does not clean any existing variable sets which exist under another name, but will overwrite any existing ones. For example, if the repository already had a set1 and set3 variable sets, calling the method with the previous sets would overwrite set1 with the new values, add set2 and keep set3 as it was.

The structure of the creation message is compliant with the struvture of the variable repository described above, which is:

```
{
  "<set_0>" : {
    "<variable_0_0>": <value>
               .
               .
   ,"<variable_0_y>": <value>
  }
  .
  .
 ,"<set_n>": {
    "variable_n_0>": <value>
              .
              .
   ,"variable_n_z>": <value>
}
```

Allowing for n sets, each one with an arbitrary number of independent variables.

## Create or Update Single Variable Sets

As a convenience, it is also possible to set individual variable sets using the endpoint `/__admin/variables/{setName}`. So, to set or update a specific variable set we only need to send the actual variable set content. 

For example. to create or update the second variable set from the previous example (`set2`), we would post the following object to `/__admin/variables/set2`:

```json
{
  "tres" : {
    "name" : "juan",
    "profession" : "luchador"
  },
  "cuatro" : {
    "name" : "jose",
    "profession" : "batido"
  }
}
```

## Check the current variable sets

To check the current status of the repository, and effectively retrieve all current variable sets and their respective variables, the endpoint `/__admin/variables` may be called with a `GET` request.

For example:

```sh
curl -s http://localhost:8080/__admin/variables
```
  
result:

```json
{"set2":{"tres":{"name":"juan","profession":"luchador"},"cuatro":{"name":"jose","profession":"batido"}},"set1":{"uno":{"name":"miguel","profession":"matador"},"dos":{"name":"manuel","profession":"toro"}}}
```

Notice that the variable sets, as well as their variables, are not presented in any guaranteed order.

## Retrieve a specific variable set

If the goal is to check a specific variable set, the same request, suffixed by the variable set name `/__admin/variables/{setName}` may be called with a `GET` request:

```sh
curl -s http://localhost:8080/__admin/variables/set2
```
  
result:

```json
{"uno":{"name":"miguel","profession":"matador"},"dos":{"name":"manuel","profession":"toro"}}
```

The variable set name itself is not part of the response.

## Clear repository

To clear the variable repository, a `DELETE` request can be sent to the endpoint `/__admin/variables/remove`:

```sh
curl -X DELETE http://localhost:8080/__admin/variables/remove
```

## Remove a variable set

If only a specific variable set is meant to be removed, that can be achieved by also sending the variable set in the `DELETE` request to `/__admin/variables/remove/{setName}`:

```sh
curl -X DELETE http://localhost:8080/__admin/variables/remove/set1
```

## Using Variables with Freemarker Stubs

After setting up the variables, they can be used in a stub by referring which variable set the stub will use. This is achieved with the extension parameter `variable-set`. All variables in the chosen variable set will be available in the template context under the variable name `var`, keeping the variable structure defined in the variable set. To get the value of a variable:

```
${var.<variable_name>}
```

To access the name of "miguel" from the first set in the example variables:

```
${var.uno.name}
```

To create a simple stub that returns a name and a profession based on the example variable sets, using the rest api stub creation:

```json
{
  "request":{"urlPath": "/test", "method": "GET"},
  "response": {
    "status": 200,
    "body": "{ \"name\" : \"${var.uno.name}\",\r\n \"profession\" : \"${var.uno.profession}\"}",
    "headers":{"content-type": "application/json"},
    "transformers" : [ "freemarker-transformer" ],
    "transformerParameters" : { "variable-set" : "set1" }
  }
}
```

This example would create a stub that refers variables from set `set1`.

Making a call to the configured stub (assuming the variable sets were uploaded with the given example variable sets):

```sh
curl http://localhost:8080/test
```

Would return:

```json
{ "name" : "miguel",
 "profession" : "matador"}
 ```

## Dynamically choosing a sub-set of variables to apply

It is also possible to define a variable set whose first level of variables are used as a key to dynamically choose a sub-set of variables, within a variable set. In order to do so, the variable set message must be of the form:

```
  "<set_0>" : {
    "key_0_0" : {
      "<variable_0_0_0>": <value>
                 .
                 .
     ,"<variable_0_0_y>": <value>
    }
    .
    .
   ,"key_0_k" : {
      "<variable_0_k_0>": <value>
                 .
                 .
     ,"<variable_0_k_y>": <value>
    }
  }
  .
  .
 ,"<set_n>" : {
    "key_n_0" : {
      "<variable_n_0_0>": <value>
                 .
                 .
     ,"<variable_n_0_x>": <value>
    }
    .
    .
   ,"key_n_k" : {
      "<variable_n_k_0>": <value>
                 .
                 .
     ,"<variable_n_k_x>": <value>
    }
  }
}
```

The used example already follows this structure:

```json
{
  "set1": {
    "uno": {
      "name": "miguel",
      "profession": "matador"
    },
    "dos": {
      "name": "manuel",
      "profession": "toro"
    }
  },
  "set2": {
    "tres": {
      "name": "juan",
      "profession": "luchador"
    },
    "cuatro": {
      "name": "jose",
      "profession": "batido"
    }
  }
}
```

Each set has two keys ("uno", "dos" for "set1", and "tres", "cuatro" for "set2"), and under each key, for each set, the same structure and number of variables exist. In the example, both sets have the same variable structure but that is not required. Each set can have their own variable structure.

__The Repository manager DOES NOT validate if the variable structure of key is the same. In fact, for all purposes, the keys are just normal independent variable containers. It's up to the user to guarantee that the structure under each key is the same, if it is meant to be used with dynamic variable sets, as otherwise it can have situations where stubs will break due to lack of variables. Lack of variables can be coped with by having the templates testing for the variable existence.__

## Creating a stub to chose a variable sub-set

In order to choose a specific sub-set of the variable set, the stub needs to have the key configured, so that some part of the request can be used as the key to identify the sub-set. This is achieved by setting the variable `variable-key-source` to define the source of the request where a key should be searched for (possible variables are `url`, `body` and `headers`) and a regular expression that is used to find matches for the key, with the variable `variable-key-matches`.

For example, we can configure a stub to retrieve the key value from the request url:

```json
{
    "request":{"urlPathPattern": "/test/.*", "method": "GET"},
    "response": {
      "status": 200,
      "body": "{ \"name\" : \"${var.name}\",\r\n \"profession\" : \"${var.profession}\"}",
      "headers":{"content-type": "application/json"},
      "transformers" : [ "freemarker-transformer" ],
      "transformerParameters" : {
        "variable-key-matches" : "/test/(.*)",
        "variable-set" : "set1",
        "variable-key-source" : "url"
      }
    }
}
```

In this example, the stub is configured to accept any request under the endpoint "/test/...", to use the first set from our variable repository (`set1`), to search for the key in the request url (using `"variable-key-source" : "url"`), and to match any key with the pattern `/test/(.*)` (Refer to regular expression syntax for matching expressions) which will effectively match any value we send to the test stub after the leading `/test/`.

To call the stub with the values under the first key (`uno`), a single `GET` to `/test/uno` is used.

```sh
curl -s http://localhost:8080/test/uno
```

returning:

```json
{ "name" : "miguel",
 "profession" : "matador"}
```
