/*******************************************************************************
 * Copyright (c) 1998, 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     rbarkhouse - 2.4 - initial implementation
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.xmlelement;

import java.util.ArrayList;

import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class XmlElementDefaultValueTestCases extends JAXBWithJSONTestCases {

    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlelement/employee_defaultvalue.xml";
    private final static String JSON_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlelement/employee_defaultvalue.json";

    private final static String XML_RESOURCE_WRITE = "org/eclipse/persistence/testing/jaxb/xmlelement/employee_defaultvalue_w.xml";
    private final static String JSON_RESOURCE_WRITE = "org/eclipse/persistence/testing/jaxb/xmlelement/employee_defaultvalue_w.json";

    public XmlElementDefaultValueTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);
        setControlJSON(JSON_RESOURCE);
        setWriteControlDocument(XML_RESOURCE_WRITE);
        setWriteControlJSON(JSON_RESOURCE_WRITE);
        Class[] classes = new Class[1];
        classes[0] = EmployeeDefaultValue.class;
        setClasses(classes);
    }

    protected Object getControlObject() {
        EmployeeDefaultValue employee = new EmployeeDefaultValue();
        employee.name = EmployeeDefaultValue.DEFAULT_NAME;

        employee.ints = new ArrayList<Integer>();
        employee.ints.add(123);
        employee.ints.add(123);
        employee.ints.add(10);
        employee.ints.add(null);
        return employee;
    }

}
