/*
 * GenericNamespacedXmlObjectBuilderTest.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.objectbuilder;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericNamespacedXmlObjectBuilderTest {

    @Mock
    private Attributes atts;

    @Mock
    private Attributes atts2;

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleTag() throws SAXException {
        XmlObjectBuilder objectBuilder = new GenericNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(1,((Map<String,String>)objectBuilder.getObject().get("ns_tag")).size());
        assertEquals("TEST",((Map<String,String>)objectBuilder.getObject().get("ns_tag")).get("value"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleTagWithAttributes() throws SAXException {
        
        Mockito.when(atts.getLength()).thenReturn(2);
        Mockito.when(atts.getLocalName(0)).thenReturn("attr1");
        Mockito.when(atts.getQName(0)).thenReturn("ns:attr1");
        Mockito.when(atts.getValue(0)).thenReturn("one");
        Mockito.when(atts.getLocalName(1)).thenReturn("attr2");
        Mockito.when(atts.getQName(1)).thenReturn("attr2");
        Mockito.when(atts.getValue(1)).thenReturn("two");
        
        XmlObjectBuilder objectBuilder = new GenericNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(3,((Map<String,String>)objectBuilder.getObject().get("tag")).size());
        assertEquals("TEST",((Map<String,String>)objectBuilder.getObject().get("tag")).get("value"));
        assertEquals("one",((Map<String,String>)objectBuilder.getObject().get("tag")).get("ns_attr1"));
        assertEquals("two",((Map<String,String>)objectBuilder.getObject().get("tag")).get("attr2"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultipleTag() throws SAXException {
        XmlObjectBuilder objectBuilder = new GenericNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST2".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST3".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST4".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(2,objectBuilder.getObject().size());
        assertEquals(3,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Iterator<Map<String,Object>> tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator();
        Map<String,Object> tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST2",tag.get("value"));
        tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST3",tag.get("value"));
        tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST4",tag.get("value"));
        assertEquals("TEST",((Map<String,String>)objectBuilder.getObject().get("ns_tag")).get("value"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultipleTagAttributes() throws SAXException {
        Mockito.when(atts.getLength()).thenReturn(2);
        Mockito.when(atts.getLocalName(0)).thenReturn("attr1");
        Mockito.when(atts.getQName(0)).thenReturn("ns:attr1");
        Mockito.when(atts.getValue(0)).thenReturn("one");
        Mockito.when(atts.getLocalName(1)).thenReturn("attr2");
        Mockito.when(atts.getQName(1)).thenReturn("attr2");
        Mockito.when(atts.getValue(1)).thenReturn("two");
        
        Mockito.when(atts2.getLength()).thenReturn(2);
        Mockito.when(atts2.getLocalName(0)).thenReturn("attr3");
        Mockito.when(atts2.getQName(0)).thenReturn("attr3");
        Mockito.when(atts2.getValue(0)).thenReturn("three");
        Mockito.when(atts2.getLocalName(1)).thenReturn("attr4");
        Mockito.when(atts2.getQName(1)).thenReturn("attr4");
        Mockito.when(atts2.getValue(1)).thenReturn("four");
        

        XmlObjectBuilder objectBuilder = new GenericNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST2".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts2);
        objectBuilder.characters("TEST3".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(2,objectBuilder.getObject().size());
        assertEquals(2,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Iterator<Map<String,Object>> tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator();
        Map<String,Object> tag = tags.next();
        assertEquals(3,tag.size());
        assertEquals("TEST2",tag.get("value"));
        assertEquals("one",tag.get("ns_attr1"));
        assertEquals("two",tag.get("attr2"));
        tag = tags.next();
        assertEquals(3,tag.size());
        assertEquals("TEST3",tag.get("value"));
        assertEquals("three",tag.get("attr3"));
        assertEquals("four",tag.get("attr4"));
        
        assertEquals("TEST",((Map<String,String>)objectBuilder.getObject().get("ns_tag")).get("value"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTagOverloadedAttributes() throws SAXException {
        
        Mockito.when(atts.getLength()).thenReturn(3);
        Mockito.when(atts.getLocalName(0)).thenReturn("attr1");
        Mockito.when(atts.getQName(0)).thenReturn("attr1");
        Mockito.when(atts.getValue(0)).thenReturn("one");
        Mockito.when(atts.getLocalName(1)).thenReturn("attr1");
        Mockito.when(atts.getQName(1)).thenReturn("ns:attr1");
        Mockito.when(atts.getValue(1)).thenReturn("three");
        Mockito.when(atts.getLocalName(2)).thenReturn("attr2");
        Mockito.when(atts.getQName(2)).thenReturn("attr2");
        Mockito.when(atts.getValue(2)).thenReturn("two");
        
        XmlObjectBuilder objectBuilder = new GenericNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("    ".toCharArray(), 0, 4);
        objectBuilder.startElement("", "attr1", "attr1", atts2);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "attr1", "attr1");
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(4,((Map<String,String>)objectBuilder.getObject().get("tag")).size());
        assertEquals("",((Map<String,String>)objectBuilder.getObject().get("tag")).get("value"));
        assertEquals(2,((Map<String,Collection<Object>>)objectBuilder.getObject().get("tag")).get("attr1").size());
        Iterator<Object> attributeMix = ((Map<String,Collection<Object>>)objectBuilder.getObject().get("tag")).get("attr1").iterator();
        assertEquals("one",attributeMix.next());
        Map<String,Object> child = (Map<String, Object>) attributeMix.next();
        assertEquals(1,child.size());
        assertEquals("TEST",child.get("value"));
        assertEquals("two",((Map<String,String>)objectBuilder.getObject().get("tag")).get("attr2"));
        assertEquals("three",((Map<String,String>)objectBuilder.getObject().get("tag")).get("ns_attr1"));

    }
}
