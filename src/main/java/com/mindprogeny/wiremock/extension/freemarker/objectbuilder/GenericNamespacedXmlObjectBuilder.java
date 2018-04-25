/*
 * GenericNamespacedXmlObjectBuilder.java, 14 Mar 2018
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
package com.mindprogeny.wiremock.extension.freemarker.objectbuilder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * Generic XML Map Object builder where collections of homonymous child tags are represented as a collection of maps, a
 * single child tag as a map, attributes as strings and the content of the tag as a sting with the attribute given in
 * the constructor.
 * <br>
 * Attribute names and tag names with namespaces will have the namespace name prefixed to the name with an underscore
 * ('_') separating both and are considered distinct from homonymous tags and attributes having the same name
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class GenericNamespacedXmlObjectBuilder extends XmlObjectBuilder {

    /**
     * 
     */
    public GenericNamespacedXmlObjectBuilder(String textElementName) {
        super(textElementName);
    }

    /**
     * @see com.mindprogeny.wiremock.extension.freemarker.objectbuilder.XmlObjectBuilder#addElement(java.util.Map,
     *      java.util.Map, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void addElement(Map<String, Object> parent, Map<String, Object> element, String localName, String qName, Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            element.put(atts.getQName(i).replace(':', '_'), atts.getValue(i));
        }

        String elementName = qName.replace(':', '_');
        Object existingElement = parent.get(elementName);
        if (existingElement == null) {
            parent.put(elementName, element);
        } else if (existingElement instanceof Collection) {
            ((Collection<Map<String, Object>>) existingElement).add(element);
        } else {
            Collection<Object> siblings = new LinkedList<>();
            siblings.add(existingElement);
            siblings.add(element);
            parent.put(elementName, siblings);
        }
    }

}
