/*
 * ActiveObjectBuilderException.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.exception;

import org.xml.sax.SAXException;

/**
 * Exception raised when trying to re-use an XmlObjectBuilder while it is still being used to parse a document
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public class ActiveObjectBuilderException extends SAXException {

    /**
     * 
     */
    private static final long serialVersionUID = -6302292479734774492L;

    /**
     * 
     */
    public ActiveObjectBuilderException() {
        super("Trying to re-use a closed or active XML ObjectBuilder. Create a new one instead!");
    }

}
