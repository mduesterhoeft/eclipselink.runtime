/*******************************************************************************
 * Copyright (c) 1998, 2008 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.oxm.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import org.eclipse.persistence.internal.oxm.XMLConversionManager;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.record.XMLRootRecord;
import org.eclipse.persistence.oxm.XMLUnmarshaller;
import org.eclipse.persistence.oxm.record.UnmarshalRecord;
import org.eclipse.persistence.platform.xml.XMLParser;
import org.eclipse.persistence.platform.xml.XMLPlatformFactory;

import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.eclipse.persistence.internal.oxm.record.XMLReader;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide an implementation of PlatformUnmarshaller that makes use of the SAX parser
 * to build Java Objects from SAX Events.
 * <p><b>Responsibilities:</b><ul>
 * <li>Implement the required unmarshal methods from PlatformUnmarshaller</li>
 * <li>Check to see if document preservation is enabled, and if so, always unmarshal from a node</li>
 * </ul>
 * 
 * @author bdoughan
 * @see org.eclipse.persistence.oxm.platform.SAXPlatform
 */
public class SAXUnmarshaller implements PlatformUnmarshaller {
    private static final String VALIDATING = "http://xml.org/sax/features/validation";
    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private int validationMode;
    private Object[] schemas;
    private SAXParser saxParser;
    private XMLReader xmlReader;
    private XMLUnmarshaller xmlUnmarshaller;
    private XMLParser xmlParser;
    private boolean isResultAlwaysXMLRoot;
	private static final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	static {
		try {
			saxParserFactory.setNamespaceAware(true);
			saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);			
		} catch (Exception e) {
            throw XMLMarshalException.errorInstantiatingSchemaPlatform(e);
		}
	}
    
    public SAXUnmarshaller(XMLUnmarshaller xmlUnmarshaller) throws XMLMarshalException {
        super();
        try {
            saxParser = saxParserFactory.newSAXParser();
            xmlReader = new XMLReader(saxParser.getXMLReader());
            xmlParser = XMLPlatformFactory.getInstance().getXMLPlatform().newXMLParser();
            xmlParser.setNamespaceAware(true);
            xmlParser.setValidationMode(XMLParser.NONVALIDATING);
            this.xmlUnmarshaller = xmlUnmarshaller;
        } catch (Exception e) {
            throw XMLMarshalException.errorInstantiatingSchemaPlatform(e);
        }
    }

    public EntityResolver getEntityResolver() {
        return xmlReader.getEntityResolver();
    }
    public void setEntityResolver(EntityResolver entityResolver) {
        xmlReader.setEntityResolver(entityResolver);
        xmlParser.setEntityResolver(entityResolver);
    }

    public ErrorHandler getErrorHandler() {
        return xmlReader.getErrorHandler();
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        xmlReader.setErrorHandler(errorHandler);
        xmlParser.setErrorHandler(errorHandler);
    }

    public int getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(int validationMode) {
        try {
            this.validationMode = validationMode;
            xmlParser.setValidationMode(validationMode);
            switch (validationMode) {
            case XMLParser.NONVALIDATING: {
                xmlReader.setFeature(VALIDATING, false);
                break;
            }
            case XMLParser.DTD_VALIDATION: {
                xmlReader.setFeature(VALIDATING, true);
                break;
            }
            case XMLParser.SCHEMA_VALIDATION: {
                try {
                    xmlReader.setFeature(VALIDATING, true);
                    saxParser.setProperty(SCHEMA_LANGUAGE, XML_SCHEMA);
                    saxParser.setProperty(SCHEMA_SOURCE, schemas);
                } catch (Exception e) {
                    xmlReader.setFeature(VALIDATING, false);
                }
                break;
            }
            }
        } catch (Exception e) {
            // Don't change the validation mode.
        }
    }

    public void setWhitespacePreserving(boolean isWhitespacePreserving) {
        xmlParser.setWhitespacePreserving(isWhitespacePreserving);
    }

    public void setSchemas(Object[] schemas) {
        this.schemas = schemas;
    }

    public Object unmarshal(File file) {
        try {
            if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
                Node domElement = xmlParser.parse(file).getDocumentElement();
                return unmarshal(domElement);
            }
            FileInputStream inputStream = new FileInputStream(file);
            return unmarshal(inputStream);
        } catch (FileNotFoundException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
    }

    public Object unmarshal(File file, Class clazz) {
        try {
            if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
                Node domElement = xmlParser.parse(file).getDocumentElement();
                return unmarshal(domElement, clazz);
            }
            FileInputStream inputStream = new FileInputStream(file);
            return unmarshal(inputStream, clazz);
        } catch (FileNotFoundException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
    }

    public Object unmarshal(InputStream inputStream) {
        if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
            Node domElement = xmlParser.parse(inputStream).getDocumentElement();
            return unmarshal(domElement);
        }
        InputSource inputSource = new InputSource(inputStream);
        return unmarshal(inputSource);
    }

    public Object unmarshal(InputStream inputStream, Class clazz) {
        if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
            Node domElement = xmlParser.parse(inputStream).getDocumentElement();
            return unmarshal(domElement, clazz);
        }
        InputSource inputSource = new InputSource(inputStream);
        return unmarshal(inputSource, clazz);
    }

    public Object unmarshal(InputSource inputSource) {
        try {
        	XMLContext xmlContext = xmlUnmarshaller.getXMLContext();
            if(xmlContext.hasDocumentPreservation()) {
                Node domElement = xmlParser.parse(inputSource).getDocumentElement();
                return unmarshal(domElement);
            }
            SAXUnmarshallerHandler saxUnmarshallerHandler = new SAXUnmarshallerHandler(xmlContext);
            saxUnmarshallerHandler.setXMLReader(xmlReader);
            saxUnmarshallerHandler.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(saxUnmarshallerHandler);
            xmlReader.parse(inputSource);
            xmlReader.setContentHandler(null);
            // resolve any mapping references
            saxUnmarshallerHandler.resolveReferences();
            return saxUnmarshallerHandler.getObject();
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }
    }

    public Object unmarshal(InputSource inputSource, XMLReader xmlReader) {
        try {
            SAXUnmarshallerHandler saxUnmarshallerHandler = new SAXUnmarshallerHandler(xmlUnmarshaller.getXMLContext());
            saxUnmarshallerHandler.setXMLReader(xmlReader);
            saxUnmarshallerHandler.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(saxUnmarshallerHandler);
            xmlReader.parse(inputSource);
            xmlReader.setContentHandler(null);
            // resolve any mapping references
            saxUnmarshallerHandler.resolveReferences();
            return saxUnmarshallerHandler.getObject();
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }
    }
    public Object unmarshal(InputSource inputSource, Class clazz) {
    	XMLContext xmlContext = xmlUnmarshaller.getXMLContext();
    	
    	if(xmlContext.hasDocumentPreservation()) {
            Node domElement = xmlParser.parse(inputSource).getDocumentElement();
            return unmarshal(domElement, clazz);
        }
    	boolean isPrimitiveWrapper = XMLConversionManager.getDefaultXMLManager().getDefaultJavaTypes().get(clazz) != null;
    	UnmarshalRecord unmarshalRecord;
    	XMLDescriptor xmlDescriptor = null;

    	AbstractSession session = null; 

        // check for case where the reference class is a primitive wrapper - in this case, we 
    	// need to use the conversion manager to convert the node's value to the primitive 
    	// wrapper class, then create, populate and return an XMLRoot.  This will be done
    	// via XMLRootRecord.
    	if (isPrimitiveWrapper) {
    		unmarshalRecord = new XMLRootRecord(clazz);
        } else {
            // for XMLObjectReferenceMappings we need a non-shared cache, so
            // try and get a Unit Of Work from the XMLContext
            session = xmlContext.getReadSession(clazz); 
            xmlDescriptor = (XMLDescriptor)session.getDescriptor(clazz);
            unmarshalRecord = (UnmarshalRecord)xmlDescriptor.getObjectBuilder().createRecord();
            unmarshalRecord.setSession(session);
        }
        try {
            unmarshalRecord.setXMLReader(xmlReader);
            unmarshalRecord.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(unmarshalRecord);
            try {
                xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", unmarshalRecord);
            } catch(SAXNotRecognizedException ex) {
            } catch(SAXNotSupportedException ex) {
                //if lexical handling is not supported by this parser, just ignore. 
            }            
            xmlReader.parse(inputSource);
            xmlReader.setContentHandler(null);
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }

        // resolve mapping references
        xmlUnmarshaller.resolveReferences(session);

        if (isPrimitiveWrapper) {
            return unmarshalRecord.getCurrentObject();
        }
        return xmlDescriptor.wrapObjectInXMLRoot(unmarshalRecord, this.isResultAlwaysXMLRoot);

    }

    public Object unmarshal(InputSource inputSource, Class clazz, XMLReader xmlReader) {
    	boolean isPrimitiveWrapper = XMLConversionManager.getDefaultXMLManager().getDefaultJavaTypes().get(clazz) != null;
    	UnmarshalRecord unmarshalRecord;
    	XMLDescriptor xmlDescriptor = null;

        // for XMLObjectReferenceMappings we need a non-shared cache, so
        // try and get a Unit Of Work from the XMLContext
    	AbstractSession session = null; 

        // check for case where the reference class is a primitive wrapper - in this case, we 
    	// need to use the conversion manager to convert the node's value to the primitive 
    	// wrapper class, then create, populate and return an XMLRoot.  This will be done
    	// via XMLRootRecord.
    	if (isPrimitiveWrapper) {
    		unmarshalRecord = new XMLRootRecord(clazz);
    	} else {
            // for XMLObjectReferenceMappings we need a non-shared cache, so
            // try and get a Unit Of Work from the XMLContext
            session = xmlUnmarshaller.getXMLContext().getReadSession(clazz); 
            xmlDescriptor = (XMLDescriptor)session.getDescriptor(clazz);
            unmarshalRecord = (UnmarshalRecord)xmlDescriptor.getObjectBuilder().createRecord();
            unmarshalRecord.setSession(session);
        }

        try {
            unmarshalRecord.setXMLReader(xmlReader);
            unmarshalRecord.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(unmarshalRecord);
            try {
                unmarshalRecord.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", unmarshalRecord);
            } catch(SAXNotRecognizedException ex) {
            } catch(SAXNotSupportedException ex) {
                //if lexical handling is not supported by this parser, just ignore. 
            }            
            xmlReader.parse(inputSource);
            xmlReader.setContentHandler(null);
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }

        // resolve mapping references
        xmlUnmarshaller.resolveReferences(session);

        if (isPrimitiveWrapper) {
            return unmarshalRecord.getCurrentObject();
        }
        return xmlDescriptor.wrapObjectInXMLRoot(unmarshalRecord, this.isResultAlwaysXMLRoot);
    }

    public Object unmarshal(Node node) {
        DOMReader reader = new DOMReader();
        return unmarshal(reader, node);
    }

    public Object unmarshal(DOMReader reader, Node node) {
        try {
            SAXUnmarshallerHandler handler = new SAXUnmarshallerHandler(xmlUnmarshaller.getXMLContext());
            reader.setContentHandler(handler);
            handler.setXMLReader(reader);
            handler.setUnmarshaller(xmlUnmarshaller);
            reader.parse(node);
            reader.setContentHandler(null);
        
            handler.resolveReferences();
            return handler.getObject();
        } catch(SAXException e) {
            throw convertSAXException(e);
        }
        
    }
    
    public Object unmarshal(Node node, Class clazz) {
        DOMReader reader = new DOMReader();
        return unmarshal(reader, node, clazz);
    }
    public Object unmarshal(DOMReader domReader, Node node, Class clazz) {
        boolean isPrimitiveWrapper = XMLConversionManager.getDefaultXMLManager().getDefaultJavaTypes().get(clazz) != null;
        UnmarshalRecord unmarshalRecord;
        XMLDescriptor xmlDescriptor = null;

        AbstractSession session = null; 

        // check for case where the reference class is a primitive wrapper - in this case, we 
        // need to use the conversion manager to convert the node's value to the primitive 
        // wrapper class, then create, populate and return an XMLRoot.  This will be done
        // via XMLRootRecord.
        if (isPrimitiveWrapper) {
            unmarshalRecord = new XMLRootRecord(clazz);
        } else {
            // for XMLObjectReferenceMappings we need a non-shared cache, so
            // try and get a Unit Of Work from the XMLContext
            session = xmlUnmarshaller.getXMLContext().getReadSession(clazz); 
            xmlDescriptor = (XMLDescriptor)session.getDescriptor(clazz);
            unmarshalRecord = (UnmarshalRecord)xmlDescriptor.getObjectBuilder().createRecord();
            unmarshalRecord.setSession(session);
        }
        try {
            unmarshalRecord.setXMLReader(domReader);
            unmarshalRecord.setUnmarshaller(xmlUnmarshaller);
            domReader.setContentHandler(unmarshalRecord);
            domReader.setProperty("http://xml.org/sax/properties/lexical-handler", unmarshalRecord);            
            domReader.parse(node);
            domReader.setContentHandler(null);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }

        // resolve mapping references
        xmlUnmarshaller.resolveReferences(session);

        if (isPrimitiveWrapper) {
            return unmarshalRecord.getCurrentObject();
        }
        return xmlDescriptor.wrapObjectInXMLRoot(unmarshalRecord, this.isResultAlwaysXMLRoot);    
    }

    public Object unmarshal(Reader reader) {
        if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
            Node domElement = xmlParser.parse(reader).getDocumentElement();
            return unmarshal(domElement);
        }
        InputSource inputSource = new InputSource(reader);
        return unmarshal(inputSource);
    }

    public Object unmarshal(Reader reader, Class clazz) {
        if(xmlUnmarshaller.getXMLContext().hasDocumentPreservation()) {
            Node domElement = xmlParser.parse(reader).getDocumentElement();
            return unmarshal(domElement, clazz);
        }
        InputSource inputSource = new InputSource(reader);
        return unmarshal(inputSource, clazz);
    }

    public Object unmarshal(Source source) {
        if (source instanceof SAXSource) {
            SAXSource saxSource = (SAXSource)source;
            XMLReader xmlReader = null;
            if(saxSource.getXMLReader() != null) {
                xmlReader = new XMLReader(saxSource.getXMLReader());
            }
            if (null == xmlReader) {
                return unmarshal(saxSource.getInputSource());
            } else {
                return unmarshal(saxSource.getInputSource(), xmlReader);
            }
        } else if (source instanceof DOMSource) {
            DOMSource domSource = (DOMSource)source;
            return unmarshal(domSource.getNode());
        } else if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource)source;
            if (null != streamSource.getReader()) {
                return unmarshal(streamSource.getReader());
            } else if (null != streamSource.getInputStream()) {
                return unmarshal(streamSource.getInputStream());
            } else {
                return unmarshal(streamSource.getSystemId());
            }
        }
        return null;
    }

    public Object unmarshal(Source source, Class clazz) {
        if (source instanceof SAXSource) {
            SAXSource saxSource = (SAXSource)source;
            XMLReader xmlReader = null;
            if(saxSource.getXMLReader() == null) {
                xmlReader = new XMLReader(saxSource.getXMLReader());
            }
            if (null == saxSource.getXMLReader()) {
                return unmarshal(saxSource.getInputSource(), clazz);
            } else {
                return unmarshal(saxSource.getInputSource(), clazz, xmlReader);
            }
        } else if (source instanceof DOMSource) {
            DOMSource domSource = (DOMSource)source;
            return unmarshal(domSource.getNode(), clazz);
        } else if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource)source;
            if (null != streamSource.getReader()) {
                return unmarshal(streamSource.getReader(), clazz);
            } else if (null != streamSource.getInputStream()) {
                return unmarshal(streamSource.getInputStream(), clazz);
            } else {
                return unmarshal(streamSource.getSystemId(), clazz);
            }
        }
        return null;
    }

    public Object unmarshal(URL url) {
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
        } catch(IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
        
        boolean hasThrownException = false;
        try {
            return unmarshal(inputStream);
        } catch(RuntimeException runtimeException) {
            hasThrownException = true;
            throw runtimeException;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                if(!hasThrownException) {
                    throw XMLMarshalException.unmarshalException(e);
                }
            }
        }
    }

    public Object unmarshal(URL url, Class clazz) {
        try {
            InputStream inputStream = url.openStream();
            Object result = unmarshal(inputStream, clazz);
            inputStream.close();
            return result;
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        }
    }

    public Object unmarshal(String systemId) {
        try {
            SAXUnmarshallerHandler saxUnmarshallerHandler = new SAXUnmarshallerHandler(xmlUnmarshaller.getXMLContext());
            saxUnmarshallerHandler.setXMLReader(xmlReader);
            saxUnmarshallerHandler.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(saxUnmarshallerHandler);
            xmlReader.parse(systemId);
            xmlReader.setContentHandler(null);
            // resolve mapping references
            saxUnmarshallerHandler.resolveReferences();
            return saxUnmarshallerHandler.getObject();
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }
    }
    public Object unmarshal(String systemId, Class clazz) {
    	boolean isPrimitiveWrapper = XMLConversionManager.getDefaultXMLManager().getDefaultJavaTypes().get(clazz) != null;
    	UnmarshalRecord unmarshalRecord;
    	XMLDescriptor xmlDescriptor = null;

    	AbstractSession session = null; 

        // check for case where the reference class is a primitive wrapper - in this case, we 
    	// need to use the conversion manager to convert the node's value to the primitive 
    	// wrapper class, then create, populate and return an XMLRoot.  This will be done
    	// via XMLRootRecord.
    	if (isPrimitiveWrapper) {
    		unmarshalRecord = new XMLRootRecord(clazz);
    	} else {
            // for XMLObjectReferenceMappings we need a non-shared cache, so
            // try and get a Unit Of Work from the XMLContext
            session = xmlUnmarshaller.getXMLContext().getReadSession(clazz); 
            xmlDescriptor = (XMLDescriptor)session.getDescriptor(clazz);
            unmarshalRecord = (UnmarshalRecord)xmlDescriptor.getObjectBuilder().createRecord();
            unmarshalRecord.setSession(session);
        }

        try {
            unmarshalRecord.setXMLReader(xmlReader);
            unmarshalRecord.setUnmarshaller(xmlUnmarshaller);
            xmlReader.setContentHandler(unmarshalRecord);
            try {
                unmarshalRecord.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", unmarshalRecord);
            } catch(SAXNotRecognizedException ex) {
            } catch(SAXNotSupportedException ex) {
                //if lexical handling is not supported by this parser, just ignore. 
            }            xmlReader.parse(systemId);
        } catch (IOException e) {
            throw XMLMarshalException.unmarshalException(e);
        } catch (SAXException e) {
            throw convertSAXException(e);
        }

        // resolve mapping references
        xmlUnmarshaller.resolveReferences(session);

        if (isPrimitiveWrapper) {
            return unmarshalRecord.getCurrentObject();
        }
        return xmlDescriptor.wrapObjectInXMLRoot(unmarshalRecord, this.isResultAlwaysXMLRoot);
    }

    private EclipseLinkException convertSAXException(SAXException saxException) {
        Exception internalException = saxException.getException();
        if (internalException != null) {
            if (EclipseLinkException.class.isAssignableFrom(internalException.getClass())) {
                return (EclipseLinkException)internalException;
            } else {
                return XMLMarshalException.unmarshalException(internalException);
            }
        }
        return XMLMarshalException.unmarshalException(saxException);
    }
    
    public boolean isResultAlwaysXMLRoot() {
        return this.isResultAlwaysXMLRoot;
    }
    
    public void setResultAlwaysXMLRoot(boolean alwaysReturnRoot) {
        this.isResultAlwaysXMLRoot = alwaysReturnRoot;
    }    
}
