/*
 * FreemarkerVariableRepository.java, 22 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
