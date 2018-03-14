/*
 * LocalThreadTemplateLoader.java, 14 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
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
     * @param template
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
