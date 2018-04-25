/*
 * UnclosedTagsException.java, 14 Mar 2018
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
