/*
 * CanonicalNamespacedXmlObjectBuilder.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.objectbuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * Canonical XML Map Object builder where the tag content is a string with the attribute given in the constructor and
 * both attributes and child tags are placed in namesake collections (even if with just one element) of maps where
 * attribute values can be accessed the same way as tag content. The names of attributes and tags are prefixed with
 * their namespace names, if present, and considered different of other homonymous tags and attributes of different
 * namespaces.
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 22 Mar 2018
 *
 */
public class CanonicalNamespacedXmlObjectBuilder extends XmlObjectBuilder {

    private String textElementName;

    /**
     * 
     */
    public CanonicalNamespacedXmlObjectBuilder(String textElementName) {
        super(textElementName);
        this.textElementName = textElementName;
    }

    /**
     * @see com.mindprogeny.wiremock.extension.freemarker.objectbuilder.XmlObjectBuilder#addElement(java.util.Map,
     *      java.util.Map, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void addElement(Map<String, Object> parent, Map<String, Object> element, String localName, String qName, Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            Collection<Map<String, Object>> siblings = new LinkedList<>();
            Map<String, Object> attrElement = new LinkedHashMap<>();
            attrElement.put(textElementName, atts.getValue(i));
            siblings.add(attrElement);
            element.put(atts.getQName(i).replace(':', '_'), siblings);
        }

        String elementName = qName.replace(':', '_');
        Object existingElement = parent.get(elementName);
        if (existingElement == null) {
            Collection<Map<String, Object>> siblings = new LinkedList<>();
            siblings.add(element);
            parent.put(elementName, siblings);
        } else {
            ((Collection<Map<String, Object>>) existingElement).add(element);
        }
    }

}
