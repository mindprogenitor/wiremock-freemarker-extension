/*
 * CanonicalNamespacedXmlObjectBuilderTest.java,, 14 Mar 2018
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
public class CanonicalNamespacedXmlObjectBuilderTest {

    @Mock
    private Attributes atts;

    @Mock
    private Attributes atts2;

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleTag() throws SAXException {
        XmlObjectBuilder objectBuilder = new CanonicalNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "ns:tag");
        objectBuilder.endDocument();
        
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(1,((Collection<Map<String,String>>)objectBuilder.getObject().get("ns_tag")).size());
        assertEquals("TEST",((Collection<Map<String,String>>)objectBuilder.getObject().get("ns_tag")).iterator().next().get("value"));

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
        
        XmlObjectBuilder objectBuilder = new CanonicalNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(1,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Map<String,Object> tag = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator().next();
        assertEquals(3,tag.size());
        assertEquals("TEST",tag.get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().size());
        assertEquals("one",((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().size());
        assertEquals("two",((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().get("value"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultipleTag() throws SAXException {
        XmlObjectBuilder objectBuilder = new CanonicalNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST2".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST3".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST4".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "ns:tag");
        objectBuilder.endDocument();
        
        assertEquals(2,objectBuilder.getObject().size());
        assertEquals(3,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Iterator<Map<String,Object>> tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator();
        Map<String,Object> tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST",tag.get("value"));
        tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST2",tag.get("value"));
        tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST3",tag.get("value"));

        assertEquals(1,((Collection<Map<String,Object>>)objectBuilder.getObject().get("ns_tag")).size());
        tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("ns_tag")).iterator();
        tag = tags.next();
        assertEquals(1,tag.size());
        assertEquals("TEST4",tag.get("value"));

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
        

        XmlObjectBuilder objectBuilder = new CanonicalNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "tag", atts2);
        objectBuilder.characters("TEST2".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.startElement("", "tag", "ns:tag", atts);
        objectBuilder.characters("TEST3".toCharArray(), 0, 5);
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(2,objectBuilder.getObject().size());
        assertEquals(2,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Iterator<Map<String,Object>> tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator();
        Map<String,Object> tag = tags.next();
        assertEquals(3,tag.size());
        assertEquals("TEST",tag.get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().size());
        assertEquals("one",((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().size());
        assertEquals("two",((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().get("value"));
        tag = tags.next();
        assertEquals(3,tag.size());
        assertEquals("TEST2",tag.get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr3")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr3")).iterator().next().size());
        assertEquals("three",((Collection<Map<String,Object>>)tag.get("attr3")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr4")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr4")).iterator().next().size());
        assertEquals("four",((Collection<Map<String,Object>>)tag.get("attr4")).iterator().next().get("value"));

        tags = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("ns_tag")).iterator();
        tag = tags.next();
        assertEquals(3,tag.size());
        assertEquals("TEST3",tag.get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().size());
        assertEquals("one",((Collection<Map<String,Object>>)tag.get("ns_attr1")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).size());
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().size());
        assertEquals("two",((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().get("value"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTagOverloadedAttributes() throws SAXException {
        
        Mockito.when(atts.getLength()).thenReturn(3);
        Mockito.when(atts.getLocalName(0)).thenReturn("attr1");
        Mockito.when(atts.getQName(0)).thenReturn("attr1");
        Mockito.when(atts.getValue(0)).thenReturn("one");
        Mockito.when(atts.getLocalName(1)).thenReturn("attr2");
        Mockito.when(atts.getQName(1)).thenReturn("attr2");
        Mockito.when(atts.getValue(1)).thenReturn("two");
        Mockito.when(atts.getLocalName(2)).thenReturn("attr3");
        Mockito.when(atts.getQName(2)).thenReturn("ns:attr3");
        Mockito.when(atts.getValue(2)).thenReturn("three");
        
        XmlObjectBuilder objectBuilder = new CanonicalNamespacedXmlObjectBuilder("value");
        objectBuilder.startDocument();
        objectBuilder.startElement("", "tag", "tag", atts);
        objectBuilder.characters("    ".toCharArray(), 0, 4);
        objectBuilder.startElement("", "attr1", "attr1", atts2);
        objectBuilder.characters("TEST".toCharArray(), 0, 4);
        objectBuilder.endElement("", "attr1", "attr1");
        objectBuilder.startElement("", "attr2", "ns:attr2", atts2);
        objectBuilder.characters("TEST2".toCharArray(), 0, 5);
        objectBuilder.endElement("", "attr2", "ns:attr2");
        objectBuilder.startElement("", "attr3", "attr3", atts2);
        objectBuilder.characters("TEST3".toCharArray(), 0, 5);
        objectBuilder.endElement("", "attr3", "attr3");
        objectBuilder.endElement("", "tag", "tag");
        objectBuilder.endDocument();
        
        assertEquals(1,objectBuilder.getObject().size());
        assertEquals(1,((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).size());
        Map<String,Object> tag = ((Collection<Map<String,Object>>)objectBuilder.getObject().get("tag")).iterator().next();
        assertEquals(6,tag.size());
        assertEquals("",tag.get("value"));
        assertEquals(2,((Collection<Map<String,Object>>)tag.get("attr1")).size());
        Iterator<Map<String,Object>> attributeMix = ((Collection<Map<String,Object>>)tag.get("attr1")).iterator();
        Map<String,Object> element = attributeMix.next();
        assertEquals(1,element.size());
        assertEquals("one",element.get("value"));
        element = attributeMix.next();
        assertEquals(1,element.size());
        assertEquals("TEST",element.get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().size());
        assertEquals("two",((Collection<Map<String,Object>>)tag.get("attr2")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr2")).iterator().next().size());
        assertEquals("TEST2",((Collection<Map<String,Object>>)tag.get("ns_attr2")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("ns_attr3")).iterator().next().size());
        assertEquals("three",((Collection<Map<String,Object>>)tag.get("ns_attr3")).iterator().next().get("value"));
        assertEquals(1,((Collection<Map<String,Object>>)tag.get("attr3")).iterator().next().size());
        assertEquals("TEST3",((Collection<Map<String,Object>>)tag.get("attr3")).iterator().next().get("value"));

    }
}
