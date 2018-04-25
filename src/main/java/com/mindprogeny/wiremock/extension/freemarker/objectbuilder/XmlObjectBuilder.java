/*
 * XmlObjectBuilder.java, 14 Mar 2018
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.mindprogeny.wiremock.extension.freemarker.exception.ActiveObjectBuilderException;
import com.mindprogeny.wiremock.extension.freemarker.exception.UnclosedTagsException;

/**
 * Common SAX ContentHandler object to handle all the events which are common to all 4 object building options (Generic,
 * Generic with Namespaces, Canonical and Canonical with Namespaces)
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public abstract class XmlObjectBuilder implements ContentHandler {

    /**
     * The attribute name to be used for tag content
     */
    private String textElementName;

    /**
     * The final object representing the XML document
     */
    private Map<String, Object> object;

    /**
     * Pointer to travel through the XML tree
     */
    private Map<String, Object> pointer;

    /**
     * Stack holding the branch of the XML tree when travelling the document
     */
    private LinkedList<Map<String, Object>> stack = new LinkedList<>();

    public XmlObjectBuilder(String textElementName) {
        this.textElementName = textElementName;
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        // Ignore it
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        if (object != null) {
            throw new ActiveObjectBuilderException();
        }
        pointer = object = new LinkedHashMap<>();
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        if (pointer != object) { //NOSONAR
            throw new UnclosedTagsException();
        }
        pointer = null;
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // Ignore it
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // Ignore it
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        Map<String, Object> element = new LinkedHashMap<>();
        element.put(textElementName, null);

        addElement(pointer, element, localName, qName, atts);

        stack.addFirst(pointer);
        pointer = element;
    }

    /**
     * Populate the current node in the xml object with the current xml tag element.
     * 
     * @param parent the current node parent
     * @param element the current elemtn being read
     * @param localName the local tag name
     * @param qName the qualified tag name
     * @param atts the tag's attributes
     */
    protected abstract void addElement(Map<String, Object> parent, Map<String, Object> element, String localName, String qName, Attributes atts);

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        pointer = stack.removeFirst();
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length).trim();
        String existingValue = (String) pointer.get(textElementName);
        if (existingValue == null || existingValue.length() == 0) {
            existingValue = value;
        } else if (value.length() == 0) {
            return;
        } else {
            existingValue += " " + value;
        }

        pointer.put(textElementName, existingValue);
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        // Ignore it
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        // Ignore it
    }

    /**
     * @return
     */
    public Map<String, ?> getObject() {
        return object;
    }

}
