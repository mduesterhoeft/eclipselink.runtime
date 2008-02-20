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
package org.eclipse.persistence.testing.tests.optimization.queryandsqlcounting;

import java.util.*;

import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.testing.models.employee.domain.*;
import org.eclipse.persistence.queries.*;

/**
 * Ensure the correct amount of SQL and queries are generated by queries with batch reading enabled.
 */
public class BatchReadingValueholderInstantiationTest extends TestCase {
    protected QuerySQLTracker tracker = null;
    protected int initialSQLStatements = 0;
    protected int postIndirectionTriggerSQLStatements = 0;
    protected int postSecondQuerySQLStatements = 0;
    protected int initialQueries = 0;
    protected int postIndirectionTriggerQueries = 0;
    protected int postSecondQueryQueries = 0;
    public static final int EXPECTED_INITIAL_STATEMENTS = 1;
    public static final int EXPECTED_INDIRECTION_STATEMENTS = 2;
    public static final int EXPECTED_SECOND_QUERY_STATEMENTS = 3;
    public static final int EXPECTED_INITIAL_QUERIES = 1;
    public static final int EXPECTED_INDIRECTION_QUERIES = 2;
    public static final int EXPECTED_SECOND_QUERY_QUERIES = 3;

    public BatchReadingValueholderInstantiationTest() {
        setDescription("Ensure the proper SQL statements are generated with batch reading when valueholders are triggered.");
    }

    public void setup() {
        getSession().getIdentityMapAccessor().initializeIdentityMaps();
        tracker = new QuerySQLTracker(getSession());
    }

    public void test() {
        ReadAllQuery query = new ReadAllQuery(Employee.class);
        query.addBatchReadAttribute("manager");
        Vector emps = (Vector)getSession().executeQuery(query);

        initialSQLStatements = tracker.getSqlStatements().size();
        initialQueries = tracker.getQueries().size();

        Iterator i = emps.iterator();
        while (i.hasNext()) {
            Employee e = (Employee)i.next();
            Employee m = (Employee)e.getManager();
            if (m != null) {
                m.hashCode();
            }
        }

        postIndirectionTriggerSQLStatements = tracker.getSqlStatements().size();
        postIndirectionTriggerQueries = tracker.getQueries().size();

        emps = (Vector)getSession().executeQuery(query);
        i = emps.iterator();
        while (i.hasNext()) {
            Employee e = (Employee)i.next();
            Employee m = (Employee)e.getManager();
            if (m != null) {
                m.hashCode();
            }
        }

        postSecondQuerySQLStatements = tracker.getSqlStatements().size();
        postSecondQueryQueries = tracker.getQueries().size();

    }

    public void verify() {
        if (initialSQLStatements != EXPECTED_INITIAL_STATEMENTS) {
            throw new TestErrorException("A ReadAllQuery with batching executed an incorrect number of SQL Statements. " + " expected: " + EXPECTED_INITIAL_STATEMENTS + " got: " + initialSQLStatements);
        }
        if (postIndirectionTriggerSQLStatements != EXPECTED_INDIRECTION_STATEMENTS) {
            throw new TestErrorException("Triggering indirection on a batch read attribute executed the incorrect number of SQL statements. " + " expected: " + (EXPECTED_INDIRECTION_STATEMENTS - EXPECTED_INITIAL_STATEMENTS) + " got: " + (postIndirectionTriggerSQLStatements - EXPECTED_INITIAL_STATEMENTS));
        }
        if (postSecondQuerySQLStatements != EXPECTED_SECOND_QUERY_STATEMENTS) {
            throw new TestErrorException("Rerunning a ReadAllQuery with batch and triggering indirection executed the incorrect number of SQL statements. " + " expected: " + (EXPECTED_SECOND_QUERY_STATEMENTS - EXPECTED_INDIRECTION_STATEMENTS) + " got: " + (postSecondQuerySQLStatements - EXPECTED_INDIRECTION_STATEMENTS));
        }
        if (initialQueries != EXPECTED_INITIAL_QUERIES) {
            throw new TestErrorException("A ReadAllQuery with batching executed an incorrect number of Queries. " + " expected: " + EXPECTED_INITIAL_QUERIES + " got: " + initialQueries);
        }
        if (postIndirectionTriggerQueries != EXPECTED_INDIRECTION_QUERIES) {
            throw new TestErrorException("Triggering indirection on a batch read attribute executed the incorrect number of Queries. " + " expected: " + (EXPECTED_INDIRECTION_QUERIES - EXPECTED_INITIAL_QUERIES) + " got: " + (postIndirectionTriggerQueries - EXPECTED_INITIAL_QUERIES));
        }
        if (postSecondQueryQueries != EXPECTED_SECOND_QUERY_STATEMENTS) {
            throw new TestErrorException("Rerunning a ReadAllQuery with batch and triggering indirection executed the incorrect number of Queries. " + " expected: " + (EXPECTED_SECOND_QUERY_QUERIES - EXPECTED_INDIRECTION_QUERIES) + " got: " + (postSecondQueryQueries - EXPECTED_INDIRECTION_QUERIES));
        }
    }

    public void reset() {
        getSession().getIdentityMapAccessor().initializeIdentityMaps();
        tracker.remove();
    }
}