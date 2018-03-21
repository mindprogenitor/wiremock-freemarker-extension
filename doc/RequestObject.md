## The Request Object

The request is processed by the extension and transformed into a map which is made available to the template.

The Request Object has the following format:

```
.-> request
|     |-> body                     - The original request body. May be null if request has no content.
|     |-> url                      - The endpoint, with parameters, used to access the stub
|     |-> cookies                  - attribute containing all cookies received. May be null if no cookies.
|     |     |-> <cookie name_1>    - Each cookie value can be directly accessed by name
|     |     |-> <cookie name_2>
|     |     |       ..
|     |     \-> <cookie name_n>
|     \-> parameters               - Query parameters received. May be null if no query parameters present.
|           |-> <parameter name_1> - Each query parameter value can be accessed directly by name
|           |-> <parameter name_2>
|           |       ..
|           \-> <parameter name_n>
|
|   If the request body is either an xml or a json object, all other variable names are inferred by the
|   original tag or attribute names, and sub attributes and tags may be accessed through a hierarchical
|   dot notation according the original request structure. Variables may be arrays if they refer to arrays or
|   lists in the original request.
|
|-> <request attribute/tag>
|     |-> <request attribute/tag>
|     |      ..
|     \-> <request attribute/tag>
|-> <request attribute/tag>
|       ..
\-> <request attribute/tag>
```
The way the Map is constructed varies depending if it's for a json or an xml object.

## Request Object for JSON Requests

For any request parsed as a Json object, the request object will be a map containing an exact representation of the original json request, each attribute accessed hierarchically through a dot notation.

As an example, if the `/test?name=Joe` endpoint had been called with a POST request having the following json body:
```json
{ "name" : "Joe",
  "children" : [
    { "name"   : "John",
      "gender" : "male" },
    { "name"   : "Mary",
      "gender" : "female"}],
  "parents" : ["Joe","Sandra"],
  "car" : { "brand" : "Porsche" }
}
```
The following variables would be available:

Variable | Value
-------- | -----
request.body | _The original Json Object_
request.url | '/test?name=Joe'
request.parameters | _map of query parameters_
request.parameters.name | 'Joe'
request.cookies | _null_
name | 'Joe'
children | _array with children objects_
children[0] | _First child object_
children[0].name | 'John'
children[0].gender | 'male'
children[1] | _Second child object_
children[1].name | 'Mary'
children[1].gender | 'female'
parents | _Array of Parent names_
parents[0] | 'Joe'
parents[1] | 'Sandra'
car | _car object_
car.brand | 'Porsche'

## Configuring the Request Object Type

With no special configuration, if the request is made with a body, the extension will first try to parse it as an XML object, in case of failure it will try to parse it as a JSON object, and if that fails it will consider the request as text and it will just make the `request.body` variable available to the template.

This detection approach allows the use of stubs out of the box, but it will always spend processing time trying to parse an XML object, even if the template is meant to be used with a json object. Forcing the expected request type provides some optimization as well as always raising a parsing error when the request doesn't comply with the expected type.

The extension can be setup with a parameter ("input") to define what is the expected request type so that it will immediately parse that format, failing with an HTTP 400 (Bad Request) error if the request doesn't comply. 

The four input types are "xml", "json", "text" and "detect" (the default value which will cause the extension to try all formats in the mentioned order).

To setup the wiremock stub with a json request type:
 
```java
wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                          .withStatus(200)
                                          .withHeader("content-type", "application/xml")
                                          .withBody("<Parent>${parent.name}</Parent>")
                                          .withTransformers("freemarker-transformer")
                                          .withTransformerParameter("input", "json")));
```

Or using the rest api:

```json
{ 
  "request": {
    "url": "/test",
    "method": "POST"
  },
  "response": {
    "status": 200,
    "body": "<Parent>${parent.name}</Parent>",
    "headers": {
      "Content-Type": "application/xml"
    },
    "transformers": [ "freemarker-transformer" ],
    "transformerParameters": {"input":"json"}
  }
}
```

If the input type is set to `"text"` the request object will never be parsed even if it's in xml or json. It will always just be available as a body text value.

## Template errors 

If the stubbed response is a template with a syntax error, the stub will still be created successfully (extensions are not called at the moment of a stub creation, and hence, the stub cannot be validated by the extension at the creation time). Only when the mock is invoked for that stub will the template error be raised. In this case, the extension will always return a 500 (Internal Error) HTTP response, having "Wiremock" in the text message (to allow differentiation from a configured stubbed fault, for example).

If the template refers to variables or structures expected to be in the request but not available (for example missing tags or attributes), it will also cause a generation error and a 500 response will be returned.

Apart from a small performance gain when configuring the input type using the service with text or json requests, as mentioned before, an incorrect request type will cause wiremock to reply with a 400 HTTP response (Bad request).

If a template is defined for xml requests, for example, and the request is sent with an invalid xml, through the detection process it will be processed as if it were a Text request, effectively causing the request object available in the template to not have any parsed values and consequentially none of the attributes of the request document will be directly accessible. Since the errors will be caused during the response generation, it will return a 500 HTTP response, complaining about a bad stub definition when in fact  the problem is with the request which is sending a bad request.

Using specific input types will allow discerning between a bad or unexpected request type and problems with the template itself.

## Overriding the request variable name

If working with json or xml objects with conflicting root element names (first level tags or attributes called "request") the request can be made available to the template with another request variable name to avoid conflicts and having the request object overwriting the original request variables.

To change the tag content attribute name:

```
wiremock.stubFor(get(urlEqualTo("/test")).willReturn(aResponse()
                                         .withStatus(200)
                                         .withHeader("content-type", "text/text")
                                         .withBody("${input.body}")
                                         .withTransformers("freemarker-transformer")
                                         .withTransformerParameter("request-element-name", "input")));
```
And using the REST API:

```json
{
  "request": {
    "url": "/test",
    "method": "GET"
  },
  "response": {
    "status": 200,
    "body": "${input.body}",
    "headers": { "Content-Type": "application/xml" },
    "transformers": [ "freemarker-transformer" ],
    "transformerParameters": {"request-element-name": "input"}
  }
}
```

As seen in the example stub where the variable name is renamed to `input` instead of the default `request`), the original request body would be accessed through `input.body` instead of `request.body`.