/*
 * LocalThreadTemplateLoader.java, 14 Mar 2018
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
package com.mindprogeny.wiremock.extension.freemarker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import freemarker.cache.TemplateLoader;

/**
 * Freemarker Template loader to support caching from runtime templates
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 14 Mar 2018
 *
 */
public class LocalThreadTemplateLoader implements TemplateLoader {
    
	/**
	 * The stub template stored at the thread scope
	 */
    private ThreadLocal<String> localTemplate = new ThreadLocal<>();

    /**
     * @see freemarker.cache.TemplateLoader#findTemplateSource(java.lang.String)
     */
    @Override
    public Object findTemplateSource(String name) throws IOException {
        return localTemplate.get();
    }

    /**
     * @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object)
     */
    @Override
    public long getLastModified(Object templateSource) {
        // The name is a digest, if the loader is being called for a specific digest it won't be changed if it was loaded before
        return 0;
    }

    /**
     * @see freemarker.cache.TemplateLoader#getReader(java.lang.Object, java.lang.String)
     */
    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return new StringReader((String)templateSource);
    }

    /**
     * @see freemarker.cache.TemplateLoader#closeTemplateSource(java.lang.Object)
     */
    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
        // do nothing
    }

    /**
     * Set the local thread template to load if required
     * @param template the template to store in the thread
     */
    public void setLocalTemplate(String template) {
        localTemplate.set(template);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append(" (Current template in local thread is \"");
        sb.append(localTemplate.get());
        sb.append("\")");
        return sb.toString();
    }

}
