/*
 * GetVariableSet.java, 22 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.extension.variable.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.mindprogeny.wiremock.extension.freemarker.FreemarkerVariableRepository;

/**
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class GetVariableSet implements AdminTask {
    
    /**
     * Object Mapper to serialize maps
     */
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * @see com.github.tomakehurst.wiremock.admin.AdminTask#execute(com.github.tomakehurst.wiremock.core.Admin, com.github.tomakehurst.wiremock.http.Request, com.github.tomakehurst.wiremock.admin.model.PathParams)
     */
    @Override
    public ResponseDefinition execute(Admin paramAdmin, Request paramRequest, PathParams pathParams) {
        
        try {
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_OK)
                                            .withHeader("content-type", "application/json")
                                            .withBody(jsonMapper.writeValueAsString(FreemarkerVariableRepository.getVariableSet(pathParams.get("set"))))
                                            .build();
        } catch (JsonProcessingException jpe) {
            StringWriter writer = new StringWriter();
            jpe.printStackTrace(new PrintWriter(writer));
            return ResponseDefinitionBuilder.responseDefinition()
                                            .withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
                                            .withStatusMessage("(WireMock) Not able to serialize variable sets.")
                                            .withHeader("content-type", "text/text")
                                            .withBody(writer.toString())
                                            .build();
        }
    }

}
