/*
 * XmlObjectBuilderTest.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.objectbuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.mindprogeny.wiremock.extension.freemarker.exception.ActiveObjectBuilderException;
import com.mindprogeny.wiremock.extension.freemarker.exception.UnclosedTagsException;

/**
 * Unit test for {@link XmlObjectBuilder}
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class XmlObjectBuilderTest {
    
    @Mock
    private Attributes atts;

    @Test
    public void testStartDocument() throws SAXException {
        XmlObjectBuilder objectBuilder = new XmlObjectBuilderImpl("value");
        assertNull(objectBuilder.getObject());
        objectBuilder.startDocument();
        assertNotNull(objectBuilder.getObject());
        assertEquals(0,objectBuilder.getObject().size());

        try {
            objectBuilder.startDocument();
            fail("allowing reuse of an active object builder");
        } catch (ActiveObjectBuilderException aobe) {
            
        }
        
        objectBuilder.endDocument();

        try {
            objectBuilder.startDocument();
            fail("allowing reuse of a finished object builder");
        } catch (ActiveObjectBuilderException aobe) {
            
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testStartElement() throws SAXException {
        XmlObjectBuilder objectBuilder = new XmlObjectBuilderImpl("value");
        
        try {
            objectBuilder.startElement("", "tag", "tag", atts);
            fail("Allowing object build without a start of document");
        } catch (Exception e) {
            
        }
        
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        assertNotNull(objectBuilder.getObject());
        assertEquals(1,objectBuilder.getObject().size());
        assertTrue(((Map<String,Map<String,Object>>)objectBuilder.getObject()).get("tag").containsKey("value"));
    }
    
    @Test
    public void testEndDocument() throws SAXException {
        XmlObjectBuilder objectBuilder = new XmlObjectBuilderImpl("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        try {
            objectBuilder.endDocument();
            fail("allowing end of a document with an unclosed tag");
        } catch (UnclosedTagsException ute) {
        }
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
    }
    
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testEndElement() throws SAXException {
        XmlObjectBuilder objectBuilder = new XmlObjectBuilderImpl("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag2", "tag2", atts);
        objectBuilder.endElement("", "tag2", "tag2");
        objectBuilder.endDocument();
        assertNotNull(objectBuilder.getObject());
        assertEquals(2, objectBuilder.getObject().size());
        Iterator entries = objectBuilder.getObject().entrySet().iterator();
        assertEquals("tag", ((Entry<String, String>)entries.next()).getKey());
        assertEquals("tag2", ((Entry<String, String>)entries.next()).getKey());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCharacterReading() {
        try {
            XmlObjectBuilder objectBuilder = new XmlObjectBuilderImpl("value");
            objectBuilder.startDocument();
            objectBuilder.startElement("", "tag", "tag", atts);
            objectBuilder.characters(new char[]{' ',' ',' ',' ','a'}, 0, 4);
            objectBuilder.characters(new char[]{'h','e','l','l','o',' '}, 0, 5);
            objectBuilder.characters(new char[]{' ',' ',' ',' ','a'}, 0, 4);
            objectBuilder.characters(new char[]{' ',' ','y','o','u',' '}, 1, 4);
            assertNotNull(objectBuilder.getObject());
            assertEquals(1, objectBuilder.getObject().size());
            Map<String,Object> tag = (Map<String, Object>) objectBuilder.getObject().get("tag");
            assertEquals(1, tag.size());
            assertEquals("hello you", tag.get("value"));
            objectBuilder.ignorableWhitespace(new char[]{' ','t','w','o',' ',' '}, 1, 4);
            assertEquals(1, objectBuilder.getObject().size());
            tag = (Map<String, Object>) objectBuilder.getObject().get("tag");
            assertEquals(1, tag.size());
            assertEquals("hello you two", tag.get("value"));
            // no changes in the object with these instructions
            objectBuilder.processingInstruction("", "");
            objectBuilder.skippedEntity("");
            objectBuilder.endElement("", "tag", "tag");
            objectBuilder.endDocument();
            assertEquals(1, objectBuilder.getObject().size());
            tag = (Map<String, Object>) objectBuilder.getObject().get("tag");
            assertEquals(1, tag.size());
            assertEquals("hello you two", tag.get("value"));
        } catch (Exception e) {
            fail("no errors should happen");
        }
    }
}

class XmlObjectBuilderImpl extends XmlObjectBuilder {

    /**
     * @param textElementName
     */
    public XmlObjectBuilderImpl(String textElementName) {
        super(textElementName);
    }

    /**
     * @see com.mindprogeny.wiremock.extension.freemarker.objectbuilder.XmlObjectBuilder#addElement(java.util.Map, java.util.Map, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    protected void addElement(Map<String, Object> parent, Map<String, Object> element, String localName, String qName, Attributes atts) {
        parent.put(localName, element);
    }
    
}