/*
 * FreemarkerResponseTransformerTest.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.response.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Integration Test for {@link FreemarkerResponseTransformer}
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public class FreemarkerResponseTransformerIntegrationTest {

    /**
     * WireMock server
     */
    @Rule
    public WireMockRule wiremock = new WireMockRule(WireMockConfiguration.wireMockConfig()
                                                                         .port(55080)
                                                                         .extensions(new FreemarkerResponseTransformer())
                                                                         .extensions(new FreemarkerVariableRepositoryManager()));

    /**
     * Test simple xml request parser and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSimpleXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));
        
    }
    
    /**
     * Test default detection through unknown type using an xml request
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testDefaultDetectionTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("input", "unknown")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));
        
    }

    /**
     * Test template using Query Parameters
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSimpleParameterTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(get(urlPathEqualTo("/test")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("content-type", "application/xml")
                .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-query-parameters.xml").toURI())),StandardCharsets.UTF_8))
                .withTransformers("freemarker-transformer")));

        given().port(55080)
               .when()
               .get("/test?test1=what&test2=")
               .then()
               .body(hasXPath("/root/test1", equalTo("what")))
               .body(hasXPath("/root/test2", equalTo("")))
               .body(hasXPath("/root/test3", equalTo("null")));

        given().port(55080)
               .when()
               .get("/test?test1=what&test2=&test3")
               .then()
               .body(hasXPath("/root/test1", equalTo("what")))
               .body(hasXPath("/root/test2", equalTo("")))
               .body(hasXPath("/root/test3", equalTo("")));
    }


    /**
     * Test Missing Parameters not breaking the template parameters object
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testMissingParameterTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(get(urlPathEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-allways-present-parameters-object.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")));
        
        given().port(55080)
               .when()
               .get("/test")
               .then()
               .body(hasXPath("/root", equalTo("nothing")));

        given().port(55080)
               .when()
               .get("/test?test1=what&test2=")
               .then()
               .body(hasXPath("/root", equalTo("something")));
    }

    /**
     * Test xml request parser while producing a canonical xml object and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testCanonicalXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-canonical-xml-request-processor.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("xml-object-type", "canonical")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));
        
    }

    /**
     * Test simple xml request parser with namespaces and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSimpleNamespacedXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-namespaced-xml-request.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("include-namespaces", "true")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));
        
    }

    /**
     * Test xml request parser producing a canonical object with namespaces and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testCanonicalNamespacedXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-canonical-namespaced-xml-request.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("xml-object-type", "canonical")
                                                  .withTransformerParameter("include-namespaces", "true")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));
        
    }

    /**
     * Test simple xml request parser with multiple sibling tags and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testMultipleTagXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test-multiple")).willReturn(aResponse()
                                                           .withStatus(200)
                                                           .withHeader("content-type", "application/xml")
                                                           .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-for-request-with-sibiling-twin-tags.xml").toURI())),StandardCharsets.UTF_8))
                                                           .withTransformers("freemarker-transformer")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request-with-multiple-sibling-tags.xml").toURI())))
               .when()
               .post("/test-multiple")
               .then()
               .body(hasXPath("/root/children/name", equalTo("John")));
    }

    /**
     * Test changing the name of the variable used to access the xml tag value
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTagTextElementNameXmlTransformation() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test-text-tag")).willReturn(aResponse()
                                                           .withStatus(200)
                                                           .withHeader("content-type", "application/xml")
                                                           .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-with-custom-xml-tag-value-name.xml").toURI())),StandardCharsets.UTF_8))
                                                           .withTransformers("freemarker-transformer")
                                                           .withTransformerParameter("xml-text-element-name", "content")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request-with-multiple-sibling-tags.xml").toURI())))
               .when()
               .post("/test-text-tag")
               .then()
               .body(hasXPath("/root/children/name", equalTo("John")));
    }

    /**
     * Test json request parser and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSingleRootJson() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test-json")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "application/xml")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-to-json-request.xml").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test-json")
               .then()
               .body(hasXPath("/root/name", equalTo("Joe")))
               .body(hasXPath("/root/children/child[1]", equalTo("John")))
               .body(hasXPath("/root/children/child[2]", equalTo("Mary")))
               .body(hasXPath("/root/parents/parent[1]", equalTo("Joe")))
               .body(hasXPath("/root/parents/parent[2]", equalTo("Sandra")))
               .body(hasXPath("/root/car", equalTo("Porsche")));
    }


    /**
     * Test detected text request and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testDetectedTextRequest() throws IOException, URISyntaxException {
        // Array as root element is not supported
        wiremock.stubFor(post(urlEqualTo("/test-text")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "text/text")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/text-response-stub").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/invalid-json-request.json").toURI())))
               .when()
               .post("/test-text")
               .then()
               .body(equalTo(new String(Files.readAllBytes(Paths.get(getClass().getResource("/request/invalid-json-request.json").toURI())),StandardCharsets.UTF_8)));
    }

    /**
     * Test detected xml request and usage as text
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testRequestBodyInResponse() throws IOException, URISyntaxException {
        // Array as root element is not supported
        wiremock.stubFor(post(urlEqualTo("/test-text")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "text/text")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/text-response-stub").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test-text")
               .then()
               .body(equalTo(new String(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())),StandardCharsets.UTF_8)));
    }

    /**
     * Test forced xml input request and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testXmlInput() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("input", "xml")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/Action", equalTo("Doing Something")))
               .body(hasXPath("/Envelope/Body/Operation/Response/Report/LogEntries[4]/Comments/Comment[3]", equalTo("Doing Something")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test")
               .then()
               .statusCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    
    /**
     * Test forced json input request and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testJsonInput() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub-to-json-request.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")
                                                  .withTransformerParameter("input", "json")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test")
               .then()
               .body(hasXPath("/root/name", equalTo("Joe")))
               .body(hasXPath("/root/children/child[1]", equalTo("John")))
               .body(hasXPath("/root/children/child[2]", equalTo("Mary")))
               .body(hasXPath("/root/parents/parent[1]", equalTo("Joe")))
               .body(hasXPath("/root/parents/parent[2]", equalTo("Sandra")))
               .body(hasXPath("/root/car", equalTo("Porsche")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test")
               .then()
               .statusCode(HttpURLConnection.HTTP_BAD_REQUEST);

    }

    /**
     * Test forced text input request and usage in response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTextRequest() throws IOException, URISyntaxException {
        // Array as root element is not supported
        wiremock.stubFor(post(urlEqualTo("/test-text")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "text/text")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/text-response-stub-with-no-values").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")
                                                       .withTransformerParameter("input", "text")));

        given().port(55080)
               .contentType("application/xml")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test-text")
               .then()
               .body(equalTo("Text Request"));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test-text")
               .then()
               .body(equalTo("Text Request"));
    }

    /**
     * Test usage of request as text in response with a different attribute name
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testTextRequestWithCustomAttribute() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test-text")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "text/text")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/text-response-stub-with-custom-parameter-name").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")
                                                       .withTransformerParameter("request-element-name", "input")));

        given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())))
               .when()
               .post("/test-text")
               .then()
               .body(equalTo(new String(Files.readAllBytes(Paths.get(getClass().getResource("/request/xml-request.xml").toURI())),StandardCharsets.UTF_8)));
    }


    /**
     * Test faulty stub definition and usage
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testFaultyStub() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test")).willReturn(aResponse()
                                                  .withStatus(200)
                                                  .withHeader("content-type", "application/xml")
                                                  .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/xml-response-stub.xml").toURI())),StandardCharsets.UTF_8))
                                                  .withTransformers("freemarker-transformer")));

        Response response = given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test");
        response.then()
               .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);

    }

    /**
     * Test json request parser and usage in json response
     * 
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    public void testSimpleJson() throws IOException, URISyntaxException {
        wiremock.stubFor(post(urlEqualTo("/test-json")).willReturn(aResponse()
                                                       .withStatus(200)
                                                       .withHeader("content-type", "application/json")
                                                       .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/json-response-stub.json").toURI())),StandardCharsets.UTF_8))
                                                       .withTransformers("freemarker-transformer")));

        Response response = given().port(55080)
               .contentType("text/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/request/json-request.json").toURI())))
               .when()
               .post("/test-json");
        response.then()
                .body("name", equalTo("Joe"))
                .body("spouse", equalTo("Sandra"))
                .body("children[0].name", equalTo("Joe"))
                .body("children[0].gender", equalTo("Unknown"));
    }
    
    /**
     * Test url request variable
     * 
     * @throws Exception
     */
    @Test
    public void testUrl() throws Exception {
        wiremock.stubFor(get(urlPathMatching("/test-url/.*")).willReturn(aResponse()
                                                             .withStatus(200)
                                                             .withHeader("content-type", "application/json")
                                                             .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/json-response-stub-with-url.json").toURI())),StandardCharsets.UTF_8))
                                                             .withTransformers("freemarker-transformer")));
        
        Response response = given().port(55080)
                                   .when()
                                   .get("/test-url/something");
        response.then()
                .body("url", equalTo("/test-url/something"));
        
        response = given().port(55080)
                          .when()
                          .get("/test-url/something?else");
        
        response.then()
                .body("url", equalTo("/test-url/something?else"));
    }
    
    /**
     * Test cookies request variable
     * 
     * @throws Exception
     */
    @Test
    public void testCookies() throws Exception {
        wiremock.stubFor(get(urlEqualTo("/test-cookies")).willReturn(aResponse()
                                                         .withStatus(200)
                                                         .withHeader("content-type", "application/json")
                                                         .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/json-response-stub-with-cookies.json").toURI())),StandardCharsets.UTF_8))
                                                         .withTransformers("freemarker-transformer")));
        
        Response response = given().port(55080)
                                   .header("Cookie", "ONE=1; TWO=2; THREE-FOUR=34")
                                   .when()
                                   .get("/test-cookies");
        response.then()
                .body("a", equalTo("1"))
                .body("b", equalTo("2"))
                .body("c", equalTo("34"));
    }

    @Test
    public void testVariables() throws Exception {
        wiremock.stubFor(get(urlPathMatching("/test-var/.*")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("content-type", "application/json")
                .withBody(new String(Files.readAllBytes(Paths.get(getClass().getResource("/stub/json-response-stub-with-variables.json").toURI())),StandardCharsets.UTF_8))
                .withTransformers("freemarker-transformer")
                .withTransformerParameter("variable-set", "test")
                .withTransformerParameter("variable-key-source", "url")
                .withTransformerParameter("variable-key-matches", "/test-var/(.*)")));

        given().port(55080)
               .contentType("application/json")
               .body(Files.readAllBytes(Paths.get(getClass().getResource("/variables.json").toURI())))
               .when()
               .post("/__admin/variables");
        
        Response response = given().port(55080)
                .contentType("application/json")
                .when()
                .get("/test-var/uno");
        response.then().body("name", equalTo("miguel"))
                       .body("profession", equalTo("matador"));

        given().port(55080)
               .contentType("application/json")
               .when()
               .get("/test-var/dos")
               .then()
               .body("name", equalTo("manuel"))
               .body("profession", equalTo("matado"));
    }

}
