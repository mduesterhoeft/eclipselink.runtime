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


package org.eclipse.persistence.testing.tests.jpa.advanced;

import java.util.Vector;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.DeleteAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;
import org.eclipse.persistence.queries.UpdateAllQuery;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.UnitOfWork;

import org.eclipse.persistence.testing.models.jpa.advanced.*;
import org.eclipse.persistence.testing.framework.junit.JUnitTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ReportQueryAdvancedJUnitTest  extends JUnitTestCase {

    static protected Class[] classes = {Employee.class, Address.class, PhoneNumber.class, Project.class};
    static protected Vector[] objectVectors = {null, null, null, null};
    
    static protected EmployeePopulator populator = new EmployeePopulator();

    public ReportQueryAdvancedJUnitTest() {
        super();
    }
    
    public ReportQueryAdvancedJUnitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
        if(!compare()) {
            clear();
            populate();
        }
    }
    
    protected static DatabaseSession getDbSession() {
        return getServerSession();   
    }
    
    protected static UnitOfWork acquireUnitOfWork() {
        return getDbSession().acquireUnitOfWork();   
    }
    
    protected static void clear() {
        UnitOfWork uow = acquireUnitOfWork();

        UpdateAllQuery updateEmployees = new UpdateAllQuery(Employee.class);
        updateEmployees.addUpdate("manager", null);
        updateEmployees.addUpdate("address", null);
        uow.executeQuery(updateEmployees);
    
        UpdateAllQuery updateProjects = new UpdateAllQuery(Project.class);
        updateProjects.addUpdate("teamLeader", null);
        uow.executeQuery(updateProjects);
    
        uow.executeQuery(new DeleteAllQuery(PhoneNumber.class));
        uow.executeQuery(new DeleteAllQuery(Address.class));
        uow.executeQuery(new DeleteAllQuery(Employee.class));
        uow.executeQuery(new DeleteAllQuery(Project.class));

        uow.commit();
        clearCache();
    }
    
    protected static void populate() {
        populator.buildExamples();
        populator.persistExample(getDbSession());
        clearCache();
        for(int i=0; i < classes.length; i++) {
            objectVectors[i] = getDbSession().readAllObjects(classes[i]);
        }
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ReportQueryAdvancedJUnitTest.class);
        
        return new TestSetup(suite) {
            protected void setUp(){               
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession());
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testPhoneCountGroupByOwner() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(PhoneNumber.class, builder);
        Expression groupingExp = builder.get("owner");
        reportQuery.addItem("owner", groupingExp);
        reportQuery.addItem("phonesCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);
        
        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Employee employee = (Employee)reportResult.get("owner");
            int count = ((Number)reportResult.get("phonesCount")).intValue();
            if(employee.getPhoneNumbers().size() != count) {
                fail(employee.toString() + " has " + employee.getPhoneNumbers().size() + " phones, ReportQuery returned " + count);
            }
        }
    }

    public void testPhoneCountGroupByOwnersAddress() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(PhoneNumber.class, builder);
        Expression groupingExp = builder.get("owner").get("address");
        reportQuery.addItem("ownerAddress", groupingExp);
        reportQuery.addItem("phonesCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Address address = (Address)reportResult.get("ownerAddress");
            Employee employee = (Employee)getDbSession().readObject(Employee.class, (new ExpressionBuilder()).get("address").equal(address));
            int count = ((Number)reportResult.get("phonesCount")).intValue();
            if(employee.getPhoneNumbers().size() != count) {
                fail(employee.toString() + " has " + employee.getPhoneNumbers().size() + " phones, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMembers() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers");
        reportQuery.addItem("projectTeamMember", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Employee employee = (Employee)reportResult.get("projectTeamMember");
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMemberAddress() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers").get("address");
        reportQuery.addItem("projectTeamMemberAddress", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            Address address = (Address)reportResult.get("projectTeamMemberAddress");
            Employee employee = (Employee)getDbSession().readObject(Employee.class, (new ExpressionBuilder()).get("address").equal(address));
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    public void testProjectCountGroupByTeamMemberPhone() {
        ExpressionBuilder builder = new ExpressionBuilder();
        ReportQuery reportQuery = new ReportQuery(Project.class, builder);
        Expression groupingExp = builder.anyOf("teamMembers").anyOf("phoneNumbers");
        reportQuery.addItem("projectTeamMemberPhone", groupingExp);
        reportQuery.addItem("projectsCount", builder.count());
        reportQuery.addGrouping(groupingExp);

        Vector results = (Vector)getDbSession().executeQuery(reportQuery);

        for(int i=0; i<results.size(); i++) {
            ReportQueryResult reportResult = (ReportQueryResult)(results.elementAt(i));
            PhoneNumber phone = (PhoneNumber)reportResult.get("projectTeamMemberPhone");
            Employee employee = phone.getOwner();
            int count = ((Number)reportResult.get("projectsCount")).intValue();
            if(employee.getProjects().size() != count) {
                fail(employee.toString() + " is a team member on  " + employee.getProjects().size() + " projects, ReportQuery returned " + count);
            }
        }
    }

    protected static boolean compare() {
        for(int i=0; i < classes.length; i++) {
            if(!compare(i)) {
                return false;
            }
        }
        return true;
    }

    protected static boolean compare(int i) {
        if(objectVectors[i] == null) {
            return false;
        }
        Vector currentVector = getDbSession().readAllObjects(classes[i]);
        if(currentVector.size() != objectVectors[i].size()) {
            return false;
        }
        ClassDescriptor descriptor = getDbSession().getDescriptor(classes[i]);
        for(int j=0; j < currentVector.size(); j++) {
            Object obj1 = objectVectors[i].elementAt(j);
            Object obj2 = currentVector.elementAt(j);
            if(!descriptor.getObjectBuilder().compareObjects(obj1, obj2, (org.eclipse.persistence.internal.sessions.AbstractSession)getDbSession())) {
                return false;
            }
        }
        return true;
    }
}
