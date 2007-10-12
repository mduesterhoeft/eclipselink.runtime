/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.testing.oxm.mappings.compositeobject.nillable;

import org.eclipse.persistence.oxm.NamespaceResolver;
import org.eclipse.persistence.oxm.XMLConstants;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.mappings.nullpolicy.AbstractNullPolicy;
import org.eclipse.persistence.oxm.mappings.nullpolicy.IsSetNullPolicy;
import org.eclipse.persistence.oxm.mappings.nullpolicy.XMLNullRepresentationType;

import org.eclipse.persistence.oxm.mappings.XMLCompositeObjectMapping;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

/**
 * UC 8-9 and 11.3
 * Invalid
 */
public class CompositeObjectIsSetNullPolicyAbsentIsSetAbsentTrueTestCases extends XMLMappingTestCases {
    private final static String XML_RESOURCE = //
    	"org/eclipse/persistence/testing/oxm/mappings/compositeobject/nillable/CompositeObjectIsSetNullPolicyAbsentIsSetAbsentTrue.xml";

    public CompositeObjectIsSetNullPolicyAbsentIsSetAbsentTrueTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);

        AbstractNullPolicy aNullPolicy = new IsSetNullPolicy();
    	// Alter unmarshal policy state
    	aNullPolicy.setNullRepresentedByEmptyNode(false); // No effect
    	aNullPolicy.setNullRepresentedByXsiNil(false); // No effect
    	// Alter marshal policy state
    	aNullPolicy.setMarshalNullRepresentation(XMLNullRepresentationType.ABSENT_NODE);

    	((IsSetNullPolicy)aNullPolicy).setIsSetMethodName("isSetManager");
        Project aProject = new CompositeObjectNodeNullPolicyProject(true);
        XMLDescriptor teamDescriptor = (XMLDescriptor) aProject.getDescriptor(Team.class);
        //NamespaceResolver namespaceResolver = new NamespaceResolver();
        //namespaceResolver.put(XMLConstants.SCHEMA_INSTANCE_PREFIX, XMLConstants.SCHEMA_INSTANCE_URL);
        //teamDescriptor.setNamespaceResolver(namespaceResolver);
        XMLCompositeObjectMapping aMapping = (XMLCompositeObjectMapping) teamDescriptor.getMappingForAttributeName("manager");
        aMapping.setNullPolicy(aNullPolicy);
        setProject(aProject);
    }

    protected Object getControlObject() {
    	Team aTeam = new Team();
    	aTeam.setId(123);
    	aTeam.setName("Eng");
    	//aTeam.setManager(null); // isSet state has no effect        
        return aTeam;
    }
}
