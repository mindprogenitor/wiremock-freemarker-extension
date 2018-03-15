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
import com.jayway.restassured.response.ValidatableResponse;

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
                                                                         .extensions(new FreemarkerResponseTransformer()));

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

}
