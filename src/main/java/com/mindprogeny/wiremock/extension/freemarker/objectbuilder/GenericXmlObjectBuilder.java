/**
 * GenericXmlObjectBuilder.java, 18 aug. 2016
 * Created by Joao Viegas (Joao.Nascimento@rabobank.nl)
 *
 * Copyright (c)2016, Rabobank Nederland & affiliates.
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
 * 
 * @author Jo&atilde;o Viegas
 * @since 18 aug. 2016
 */
public class GenericXmlObjectBuilder extends XmlObjectBuilder {

    /**
     * 
     */
    public GenericXmlObjectBuilder(String textElementName) {
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
            element.put(atts.getLocalName(i), atts.getValue(i));
        }

        Object existingElement = parent.get(localName);
        if (existingElement == null) {
            parent.put(localName, element);
        } else if (existingElement instanceof Collection) {
            ((Collection<Object>) existingElement).add(element);
        } else {
            Collection<Object> siblings = new LinkedList<>();
            siblings.add(existingElement);
            siblings.add(element);
            parent.put(localName, siblings);
        }
    }

}
