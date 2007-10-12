/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.oxm;

import org.eclipse.persistence.internal.oxm.XPathFragment;

public class XMLRoot {
    protected Object rootObject;
    protected XPathFragment rootFragment;
    protected String encoding;
    protected String xmlVersion;
    protected String schemaLocation;
    protected String noNamespaceSchemaLocation;

    public XMLRoot() {
        rootFragment = new XPathFragment();
    }

    public Object getObject() {
        return rootObject;
    }

    public String getLocalName() {
        return rootFragment.getLocalName();
    }

    public String getNamespaceURI() {
        return rootFragment.getNamespaceURI();
    }

    public void setObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    /**
     * Set the element name.  This method will parse the qualified
     * name in an attempt to set the prefix and localName fields.  If
     * there is no prefix, the prefix field is set to null.
     *
     * @param qualifiedName a fully qualified element name
     */
    public void setLocalName(String name) {
        rootFragment.setXPath(name);
    }

    public void setNamespaceURI(String rootElementUri) {
        rootFragment.setNamespaceURI(rootElementUri);
    }

    /**
     * INTERNAL:     
     */
    public XPathFragment getRootFragment() {
        return rootFragment;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public String getXMLVersion() {
        return xmlVersion;
    }
    
    public void setVersion(String version) {
        this.xmlVersion = version;
    }
    
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }
    
    public String getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }
    
    public void setNoNamespaceSchemaLocation(String noNamespaceSchemaLocation) {
        this.noNamespaceSchemaLocation = noNamespaceSchemaLocation;
    }
    
}