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
package org.eclipse.persistence.internal.jpa.metadata.accessors;

import org.eclipse.persistence.internal.jpa.metadata.accessors.MetadataAccessor;

/**
 * An transient accessor ... which does nothing ... just a clever way to
 * make sure we don't process the accessible object for annotations.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class TransientAccessor extends MetadataAccessor {
	/**
     * INTERNAL:
     */
    public TransientAccessor() {}
    
    /**
     * INTERNAL:
     */
     public void process() {
    	 // Does nothing ...
     }
}
