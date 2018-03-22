## The XML Request Object

The Request Object for XML Requests may be generated using one of four possible strategies

* __Straightforward__

  Namespaces are not taken into consideration, tags are accessed as an array if multiple tags at the same level exist or directly if only one exists.
  
* __Namespaced XML__

  Same as __Straightforward__ but namespace names are prefixed to the tag or attribute names when present.

* __Canonical__

  Namespaces are not taken into consideration. All elements are accessed as arrays, providing a uniform way to access all variables.
* __Namespaced Canonical__
  
  Same as __Canonical__ but namespace names are prefixed to the tag or attribute names when present.
  
## Default XML Request Object

An xml object will be parsed and transformed into a Map of objects, where tag names and attributes are keys in the map, and namespaces are ignored.

Any additional tags contained in a tag (as children) will be also a map (containing its own nested attributes, value and child tags). The text content of a tag will be available under an attribute with the default name of "value".

If a tag has multiple child tags with the same name, then the child tag name will be used as an attribute name to access a list of maps, one per each child tag contained in the parent tag. This will effectively convert multiple sibling namesake tags to an array.

This means that an xml like

```XML
<root xmlns:ns5="http://www.namespace.com" >
  <ns5:parent>
    <name>something</name>
    <child number="1">John</child>
    <child number="2">Mary</child>
    <ns5:child number="3">Cindy</ns5:child>
    <ns5:child number="4">Kyle</ns5:child>
  </ns5:parent>
</root>
```

Will be transformed  into an object like

```
root = { value  = "",
         parent = { value = "",
                    name  = "something",
                    child = [{ value  = "John",
                               number = "1" },
                             { value  = "Mary",
                               number = "2" },
                             { value  = "Cindy",
                               number = "3" },
                             { value  = "Kyle",
                               number = "4" }]
                  }
       }
```
alongside the `request` map variable containing the http request attributes.

Each tag will always have a value attribute, which may be empty (empty, not null) if the tag has no textual value (either being empty or only having child tags).

This object is available as a map in the freemarker template, and the values can be accessed using the freemarker syntax.

To access the name of the second child ("Mary") for example, we can use the following expression:

```
${root.parent.child[1].value}
```

## The canonical request object

The canonical representation of the request object implements a more standard way of representing the xml document. In this case, both attributes and tags  are objects (in the simple request object, attributes are strings) with a "value" attribute, and each attribute of an object, aside from the "value" attribute, are lists of objects.

Taking the previous XML example, the resulting request object would be:

```
root = [{ value  = "",
          parent = [{ value = "",
                      name  = [{ value = "something"
                               }],
                      child = [{ value  = "John",
                                 number = [{ value = "1"
                                           }]
                               },
                               { value  = "Mary",
                                 number = [{ value = "2"
                                           }]
                               },
                               { value  = "Cindy",
                                 number = [{ value = "3"
                                           }]
                               },
                               { value  = "Kyle",
                                 number = [{ value = "4"
                                           }]
                               }]
                    }]
        }]
```

Which would make the name of the second child ("Mary") available through:

```
${root[0].parent[0].child[1].value}
```

This approach allows a more generic template use when dealing with xml documents having tags of 1..n multiplicity (which, with the simple object, would return simple objects for documents that would return just one tag and a list when multiple tags would be present) as well as overloaded attribute names with homonymous child tag names.

For example, imagine the following two xsd compatible xml objects:

1. 
```
<root>
  <child>First</child>
</root>
```
2.
```
<root>
  <child>First</child>
  <child>Second</child>
</root>
```

And we would like to always refer to the `First` child. Using a non-canonical request object, The first child could be accessed with `${root.child.value}` for the first request, and `${root.child[0].value}` in the second request.

By forcing the use of a canonical object, a generic stub can be used that can handle bboth requests in the same way, by simply using the expression `${root[0].child[0].value}`.


To setup a stub using a canonical xml object you may set the extension parameter `xml-object-type` to the value `canonical`:

Configuring the extension in a Junit test:

```
wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                          .withStatus(200)
                                          .withHeader("content-type", "application/xml")
                                          .withBody("<SecondChild>${root[0].parent[0].child[1].value}</SecondChild>")
                                          .withTransformers("freemarker-transformer")
                                          .withTransformerParameter("xml-object-type", "canonical")));
```

Or using the rest api:

```
{
  "request": {
    "url": "/test",
    "method": "POST"
  },
  "response": {
    "status": 200,
    "body": "<SecondChild>${root[0].parent[0].child[1].value}</SecondChild>",
    "headers": { "Content-Type": "application/xml" },
    "transformers": [ "freemarker-transformer" ],
    "transformerParameters": {"xml-object-type":"canonical"}
  }
}
```

## Namespaces

By default the request object is created using the attribute and tag local names, effectively ignoring the namespaces.

In the xml example provided previously:

```
<root xmlns:ns5="http://www.namespace.com" >
  <ns5:parent>
    <name>something</name>
    <child number="1">John</child>
    <child number="2">Mary</child>
    <ns5:child number="3">Cindy</ns5:child>
    <ns5:child number="4">Kyle</ns5:child>
  </ns5:parent>
</root>
```

There are two homonymous tags ("child") which can potentially have different structures and the template may want to access their specific attributes. Without the namespaces, as seen before, all of those tags are kept in the same collection which can cause reference errors.

When using namespaces, the namespace is prefixed to the attribute and tag name that has them, and homonymous attributes and tags are considered distinct and kept in different lists. The resulting simple request object would be:

```
root = { value  = "",
         ns5_parent = { value = "",
                        name  = "something",
                        child = [{ value  = "John",
                                   number = "1" },
                                 { value  = "Mary",
                                   number = "2" }],
                        ns5_child = [{ value  = "Cindy",
                                       number = "3" },
                                     { value  = "Kyle",
                                       number = "4" }]
                      }
       }
```

In this case, to access the name of the second child we could use:

```
${root.ns5_parent.child[1].value}
```

while accessing the name of the third child would require:

```
${root.ns5_parent.ns5_child[0].value}
```

These examples are using the simple xml request object but the same applies to the canonical xml object while using its own usage method.

The generated namespaced canonical object would be:

```
root = [{ value  = "",
          ns5_parent = [{ value = "",
                          name  = [{ value = "something"
                                   }],
                          child = [{ value  = "John",
                                     number = [{ value = "1"
                                               }]
                                   },
                                   { value  = "Mary",
                                     number = [{ value = "2"
                                               }]
                                   }],
                          ns5_child = [{ value  = "Cindy",
                                         number = [{ value = "3"
                                                   }]
                                       },
                                       { value  = "Kyle",
                                         number = [{ value = "4"
                                                   }]
                                       }]
                        }]
        }]
```

And to access the second and third child values you can use `${root[0].ns5_parent[0].child[1].value}` and `${root[0].ns5_parent[0].ns5_child[0].value}` respectively.

Activating the use of namespaces in the request object requires setting the extension parameter `include-namespaces` to `true`.

An example stub in junit would be:

```
wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                          .withStatus(200)
                                          .withHeader("content-type", "application/xml")
                                          .withBody("<ThirdChild>${root.ns5_parent.ns5_child[0].value}</ThirdChild>")
                                          .withTransformers("freemarker-transformer")
                                          .withTransformerParameter("include-namespaces", "true")));
```

And using the rest api:

```
{
  "request": {
    "url": "/test",
    "method": "POST"
  },
  "response": {
    "status": 200,
    "body": "<ThirdChild>${root.ns5_parent.ns5_child[0].value}</ThirdChild>",
    "headers": { "Content-Type": "application/xml" },
    "transformers": [ "freemarker-transformer" ],
    "transformerParameters": {"include-namespaces":"true"}
  }
}
```

To use it with the canonical object, you must also set the `xml-object-type` extension parameter to the value `canonical`.

## Overriding the XML default Tag Content attribute name

The tag content is by default accessible through the attribute "value".

However, if required (for example, in case the document has tags with name "value") this can be changed to any other xml compatible value. This will apply to all the request object (both tags as well as attributes, in case of the canonical object).

Changing the name of the value variable name is achieved through the extension parameter `xml-text-element-name`.

To change the tag content attribute name in a junit test:

```
wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                          .withStatus(200)
                                          .withHeader("content-type", "application/xml")
                                          .withBody("<ThirdChild>${root.ns5_parent.ns5_child[0].content}</ThirdChild>")
                                          .withTransformers("freemarke r-transformer")
                                          .withTransformerParameter("xml-text-element-name", "content")));
```

And using the rest api:

```
{
  "request": {
    "url": "/test",
    "method": "POST"
  },
  "response": {
    "status": 200,
    "body": "<ThirdChild>${root.ns5_parent.ns5_child[0].content}</ThirdChild>",
    "headers": { "Content-Type": "application/xml" },
    "transformers": [ "freemarker-transformer" ],
    "transformerParameters": {"xml-text-element-name":"content"}
  }
}
```