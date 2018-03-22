/*
 * SetVariables.java, 22 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.extension.variable.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.freemarker.FreemarkerVariableRepository;;

/**
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class SetVariableSets  implements AdminTask {
    
    /**
     * Object Mapper to serialize maps
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseDefinition execute(Admin paramAdmin, Request request, PathParams paramPathParams) {
        try {
            Map<String, Object> variableSetRequest = jsonMapper.readValue(request.getBodyAsString(), Map.class);
            variableSetRequest.forEach((key,value) -> FreemarkerVariableRepository.setVariableSet(key, (Map<String, Object>) value));
        } catch (IOException ioe) {
            StringWriter writer = new StringWriter();
            ioe.printStackTrace(new PrintWriter(writer));
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_BAD_REQUEST)
                                            .withStatusMessage("Variable sets message in incorrect format.")
                                            .withHeader("content-type", "text/text")
                                            .withBody("Expected format is:\n{\n  \"set\": {\n    \"<key>\": {\"<var>\":\"<value>\"}\n  }\n}")
                                            .build();
        }
        
        return ResponseDefinitionBuilder.responseDefinition()
                .withStatus(HttpURLConnection.HTTP_OK)
                .build();
    }

}
