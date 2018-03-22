/*
 * FreemarkerVariableRepository.java, 22 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker;

import java.util.HashMap;
import java.util.Map;

/**
 * Central repository for freemarker variables
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class FreemarkerVariableRepository {

    private Map<String, Map<String, Object>> variableSets;
    
    private static final FreemarkerVariableRepository instance = new FreemarkerVariableRepository();
    
    /**
     * 
     */
    private FreemarkerVariableRepository() {
        variableSets = new HashMap<>();
    }
    
    public static final FreemarkerVariableRepository getInstance() {
        return instance;
    }
    
    public static Map<String,Object> getVariableSet(String setName) {
        return instance.variableSets.get(setName);
    }
    
    public static Map<String,Map<String,Object>> getVariableSets() {
        return instance.variableSets;
    }

    public static Map<String,Object> setVariableSet(String setName, Map<String,Object> variableSet) {
        synchronized(instance.variableSets) { // avoid corrupting the map with concurrent calls
            return instance.variableSets.put(setName, variableSet);
        }
    }

    public static Map<String,Object> removeVariableSet(String setName) {
        synchronized(instance.variableSets) { // avoid corrupting the map with concurrent calls
            return instance.variableSets.remove(setName);
        }
    }

    /**
     * 
     */
    public static void reset() {
        synchronized(instance.variableSets) { // avoid corrupting the map with concurrent calls
            instance.variableSets.clear();
        }
    }
}
