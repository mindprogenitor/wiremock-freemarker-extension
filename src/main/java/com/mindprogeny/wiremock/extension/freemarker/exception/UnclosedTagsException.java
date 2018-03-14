/*
 * UnclosedTagsException.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.freemarker.exception;

import org.xml.sax.SAXException;

/**
 * Exception Raised when finishing a parsed object and not all of the tags have been closed
 *
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public class UnclosedTagsException extends SAXException {

    /**
     * 
     */
    private static final long serialVersionUID = -5551503344554948076L;

    public UnclosedTagsException() {
        super("XML Document finished with unclosed tags!");
    }
}
