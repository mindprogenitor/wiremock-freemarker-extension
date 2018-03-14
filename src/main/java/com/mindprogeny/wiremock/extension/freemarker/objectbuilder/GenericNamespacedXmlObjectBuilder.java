/**
 * GenericNamespacedXmlObjectBuilder.java, 22 aug. 2016
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
 * Attribute names and tag names with namespaces will have the namespace name prefixed to the name with an underscore
 * ('_') separating both and are considered distinct from homonymous tags and attributes having the same name
 * 
 * @author Jo&atilde;o Viegas
 * @since 22 aug. 2016
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
