/*
 * FreemarkerResponseTransformer.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.freemarker.objectbuilder.CanonicalNamespacedXmlObjectBuilder;
import com.mindprogeny.wiremock.extension.freemarker.objectbuilder.CanonicalXmlObjectBuilder;
import com.mindprogeny.wiremock.extension.freemarker.objectbuilder.GenericNamespacedXmlObjectBuilder;
import com.mindprogeny.wiremock.extension.freemarker.objectbuilder.GenericXmlObjectBuilder;
import com.mindprogeny.wiremock.extension.freemarker.objectbuilder.XmlObjectBuilder;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Wiremock Extension to transform request body into a map object and make it available to use in the response as a freemarker template
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public class FreemarkerResponseTransformer extends ResponseDefinitionTransformer {

    /**
     * Transformer parameter to explicitly convert an input request as a defined type
     */
    private static final String INPUT_TYPE_PARAMETER = "input";

    /**
     * Input type value for an xml request
     */
    private static final String XML_INPUT_TYPE = "xml";

    /**
     * Input type value for a json request
     */
    private static final String JSON_INPUT_TYPE = "json";

    /**
     * Input type value to use the request as a text value
     */
    private static final String TEXT_INPUT_TYPE = "text";

    /**
     * Default input type where it will try to detect the input type, finally converging to text if xml and json is not parsable.
     */
    private static final String DETECT_INPUT_TYPE = "detect";

    /**
     * Attribute name to be used to access an xml tag content 
     */
    private static final String XML_TEXT_ELEMENT_NAME = "xml-text-element-name";

    /**
     * Default attribute name for the xml tag content
     */
    private static final String DEFAULT_XML_TEXT_ELEMENT_NAME = "value";

    /**
     * Attribute name to be used to access the request body and parameters
     */
    private static final String REQUEST_ELEMENT_NAME = "request-element-name";

    /**
     * Default attribute name for the request content
     */
    private static final String DEFAULT_REQUEST_ELEMENT_NAME = "request";

    /**
     * Type of xml object to be generated when parsing an xml object
     */
    private static final String XML_OBJECT_TYPE = "xml-object-type";

    /**
     * Value to indicate the transformer to produce a generic XML map object (default value)
     */
    private static final String XML_GENERIC_OBJECT_TYPE = "generic";

    /**
     * Value to indicate the transformer to produce a canonical XML map object
     */
    private static final String XML_CANONICAL_OBJECT_TYPE = "canonical";

    /**
     * Parameter to indicate if the namespaces should be prefixed to the tag names (true or false values)
     */
    private static final String INCLUDE_NAMESPACES = "include-namespaces";

    /**
     * Request content attribute name
     */
    private static final String REQUEST_CONTENT_NAME = "body";

    /**
     * Request parameters attribute name
     */
    private static final String REQUEST_PARAMETERS_NAME = "parameters";

    /**
     * Request url attribute name
     */
    private static final String REQUEST_URL_NAME = "url";

    /**
     * Request cookies attribute name
     */
    private static final String REQUEST_COOKIES_NAME = "cookies";
    
    /**
     * Object Mapper to parse json requests
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * The SAX Parser factory to parse xml objects
     */
    private SAXParserFactory saxParserFactory;
    
    /**
     * Thread Local Xml Reader
     */
    private ThreadLocal<XMLReader> xmlReader;

    /**
     * Freemarker configuration object
     */
    private Configuration configuration;
    
    /**
     * Template loader to get the template from the local thread
     */
    private LocalThreadTemplateLoader templateLoader = new LocalThreadTemplateLoader();

    /**
     * Initialize configuration and sax parser factory
     */
    public FreemarkerResponseTransformer() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTemplateLoader(templateLoader);
        configuration.setTemplateUpdateDelayMilliseconds(Long.MAX_VALUE);
        saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware(true);
        xmlReader = new ThreadLocal<XMLReader>(){
            /**
             * @see java.lang.ThreadLocal#initialValue()
             */
            @Override
            public XMLReader initialValue() {
                try {
                    return saxParserFactory.newSAXParser().getXMLReader();
                } catch (Exception e) {
                    // Any of these exceptions are critical. Raise a Runtime Exception to show in wiremock response
                    throw new RuntimeException("SAX Parser with configuration issues!!", e);
                }
            }};
    }

    /**
     * @see com.github.tomakehurst.wiremock.extension.Extension#getName()
     */
    @Override
    public String getName() {
        return "freemarker-transformer";
    }

    /**
     * @see com.github.tomakehurst.wiremock.extension.AbstractTransformer#applyGlobally()
     */
    @Override
    public boolean applyGlobally() {
        return false;
    }

    /**
     * @see com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer#transform(com.github.tomakehurst.wiremock.http.Request,
     *      com.github.tomakehurst.wiremock.http.ResponseDefinition, com.github.tomakehurst.wiremock.common.FileSource,
     *      com.github.tomakehurst.wiremock.extension.Parameters)
     */
    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {

        // Let's first see if the stub has any kind of body defined. if not let's just return the configured response as is
        if (responseDefinition.getBody() == null && responseDefinition.getBodyFileName() == null) {
            return responseDefinition;
        }

        String inputType = parameters == null ? DETECT_INPUT_TYPE : (String) parameters.getOrDefault(INPUT_TYPE_PARAMETER, DETECT_INPUT_TYPE);
        @SuppressWarnings("rawtypes")
        Map requestObject;
        try {
            requestObject = getRequestObject(request, inputType, parameters);
        } catch (IOException | SAXException e) {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
                                            .withStatusMessage("(WireMock) Unexpected or incorrect request format. Expecting " + inputType)
                                            .build();
        } catch (RuntimeException re) {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                                            .withStatusMessage("(WireMock) Internal ERROR. (Probably) Bad XML Parser configuration.")
                                            .build();
        }
        
        String template = getStubTemplate(responseDefinition, files);

        try {
            return ResponseDefinitionBuilder.like(responseDefinition)
                                            .but()
                                            .withBodyFile(null) // if the template was defined in a file, we clean it up
                                            .withBody(transformResponse(template, requestObject))
                                            .build();
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            // Done on purpose as we want to see the error in the response
            e.printStackTrace(new PrintWriter(writer)); // NOSONAR
            writer.append("\n Given Request: \n");
            writer.append(request.getBodyAsString());
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                                            .withStatusMessage("(WireMock) Bad Stub Definition.")
                                            .withBody(writer.toString())
                                            .build();
        }
    }

    /**
     * Retrieve the request object from the request body
     * 
     * @param request the request body of the request as a string
     * @param inputType the type of input to be parsed
     * @param parameters transformer parameters
     * @return the Map Object
     * @throws IOException if any error has occured when trying to parse the input object in a specfic format
     * @throws SAXException if any error has occured when trying to parse the input object as an XML
     * @throws ParserConfigurationException Should not happen, may be raised due to parser unavailability
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map getRequestObject(Request request, String inputType, Parameters parameters) throws SAXException, IOException {

        String requestBody = request.getBodyAsString();
        Map requestObject = null;
        
        String xmlTextElementName = DEFAULT_XML_TEXT_ELEMENT_NAME;
        String requestElementName = DEFAULT_REQUEST_ELEMENT_NAME;
        boolean canonical = false;
        boolean namespaces = false;
        if (parameters != null) {
            xmlTextElementName = (String) parameters.getOrDefault(XML_TEXT_ELEMENT_NAME, DEFAULT_XML_TEXT_ELEMENT_NAME);
            requestElementName = (String) parameters.getOrDefault(REQUEST_ELEMENT_NAME, DEFAULT_REQUEST_ELEMENT_NAME);
            canonical = XML_CANONICAL_OBJECT_TYPE.equals(parameters.getOrDefault(XML_OBJECT_TYPE, XML_GENERIC_OBJECT_TYPE));
            namespaces = Boolean.parseBoolean((String) parameters.getOrDefault(INCLUDE_NAMESPACES, Boolean.FALSE.toString()));
        }
        
        switch (inputType) {
        case XML_INPUT_TYPE:
            requestObject = parseXml(requestBody, xmlTextElementName, canonical, namespaces);
            break;
        case JSON_INPUT_TYPE:
            requestObject = parseJson(requestBody);
            break;
        case TEXT_INPUT_TYPE:
            requestObject = new LinkedHashMap<>();
            break;
        case DETECT_INPUT_TYPE:
        default:
            try {
                requestObject = parseXml(requestBody, xmlTextElementName, canonical, namespaces);
            } catch (Exception e) {
                try {
                    requestObject = parseJson(requestBody);
                } catch (IOException ioe) {
                    requestObject = new LinkedHashMap<>();
                }
            }
        }
        
        Map<String, Object> requestContent = new LinkedHashMap<>();
        requestObject.put(requestElementName, requestContent);
        requestContent.put(REQUEST_CONTENT_NAME, requestBody);
        requestContent.put(REQUEST_URL_NAME, request.getUrl());
        requestContent.put(REQUEST_COOKIES_NAME, request.getCookies());
        requestContent.put(REQUEST_PARAMETERS_NAME, parseParameters(request.getAbsoluteUrl()));
        return requestObject;
    }

    /**
     * Parse the query parameters of a url
     * 
     * @param url the url to pass the parameters
     * @return a map of parameter (name,value) tuples
     */
    private Map<String, String> parseParameters(String url) {
        Map<String, String> result = new LinkedHashMap<>();
        
        int queryStartIndex = url.indexOf('?') + 1;
        // URL spec doesn't allow a url to end with a '?'
        if (queryStartIndex > 0) {
            String query = url.substring(queryStartIndex);
            String[] parameters = query.split("&");
            for (String parameter : parameters) {
                int assignmentIndex = parameter.indexOf('=');
                if (assignmentIndex > 0) {
                    result.put(parameter.substring(0, assignmentIndex), parameter.substring(assignmentIndex + 1));
                } else {
                    result.put(parameter, "");
                }
            }
        }
        
        return result;
    }

    /**
     * Apply a parsed request object to a freemarker template
     * 
     * @param template the template to apply the object to
     * @param requestObject the parsed request object
     * @return the transformed template
     * @throws IOException If errors occur in reading the template or writing the response
     * @throws TemplateException If errors occur in the template definition
     * @throws NoSuchAlgorithmException 
     */
    @SuppressWarnings("rawtypes")
    private String transformResponse(String template, Map requestObject) throws IOException, TemplateException, NoSuchAlgorithmException {

        StringWriter writer = new StringWriter();
        String templateName = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(template.getBytes()));
        templateLoader.setLocalTemplate(template);
        configuration.getTemplate(templateName).process(requestObject, writer);
        return writer.toString();
    }

    /**
     * Retrieve the template from the stubbed response (either from a file or from the embedded response)
     * 
     * @param responseDefinition the original stubbed response definition
     * @param files Any files associated to the stub
     * @return the freemarker template
     */
    private String getStubTemplate(ResponseDefinition responseDefinition, FileSource files) {
        String template = responseDefinition.getBody();
        if (template == null) {
            BinaryFile file = files.getBinaryFileNamed(responseDefinition.getBodyFileName());

            // If the defined file is not a template, freemarker will complain later.
            template = new String(file.readContents(), StandardCharsets.UTF_8);
        }
        return template;
    }

    /**
     * Parse the request body as an xml document and produce an xml object according to the defined parameters
     * 
     * @param request the request body
     * @param textElementName the attribute name to be given to the xml tag content
     * @param canonical whether if a canonical object should be produced or not
     * @param namespaces whether if namespaces should be prefixed to tag names or not
     * @return the request object
     * @throws SAXException If parsing errors occur
     * @throws IOException if IO errors occur
     */
    @SuppressWarnings("rawtypes")
    private Map parseXml(String request, String textElementName, boolean canonical, boolean namespaces) throws SAXException, IOException {

        XmlObjectBuilder contentHandler = null;
        if (canonical) {
            if (namespaces) {
                contentHandler = new CanonicalNamespacedXmlObjectBuilder(textElementName);
            } else {
                contentHandler = new CanonicalXmlObjectBuilder(textElementName);
            }
        } else if (namespaces) {
            contentHandler = new GenericNamespacedXmlObjectBuilder(textElementName);
        } else {
            contentHandler = new GenericXmlObjectBuilder(textElementName);
        }
        
        XMLReader reader = xmlReader.get();
        reader.setContentHandler(contentHandler);

        reader.parse(new InputSource(new StringReader(request)));
        return contentHandler.getObject();
    }

    /**
     * Parse a request body as a map object
     * 
     * @param request the request body
     * @return the parsed request
     * @throws IOException if errors occur reading and parsing the json file
     */
    @SuppressWarnings("rawtypes")
    private Map parseJson(String request) throws IOException {
        return jsonMapper.readValue(request, Map.class);
    }

}
