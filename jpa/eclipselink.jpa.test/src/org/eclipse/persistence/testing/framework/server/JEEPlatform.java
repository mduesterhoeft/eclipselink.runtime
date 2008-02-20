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
package org.eclipse.persistence.testing.framework.server;

import javax.naming.InitialContext;

import javax.naming.NamingException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

public class JEEPlatform implements ServerPlatform {

    /**
     * Return if the JTA transaction is active.
     */
    public boolean isTransactionActive() {
        try {
            return getUserTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Start a new JTA transaction.
     */
    public void beginTransaction() {
        try {
            getUserTransaction().begin();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Commit the existing JTA transaction.
     */
    public void commitTransaction() {
        try {
            getUserTransaction().commit();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Roll back the existing JTA transaction.
     */
    public void rollbackTransaction() {
        try {
            getUserTransaction().rollback();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public UserTransaction getUserTransaction() {
        try {
            return (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        } catch (NamingException exception) {
            throw new RuntimeException(exception);
        }
    }
   
    /**
     * Mark the existing JTA transaction for rollback.
     */
    public void setTransactionForRollback() {
        try {
            getUserTransaction().setRollbackOnly();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Is the platform Oracle?
     */
    public boolean isOc4j() {
        return false;
    }

    /**
     * Is the platform Weblogic?
     */
    public boolean isWeblogic() {
        return false;
    }

    /**
     * Is the platform JBoss?
     */
    public boolean isJBoss() {
        return false;
    }

    /**
     * Is the platform clustered?
     */
    public boolean isClustered() {
        return false;
    }
    
    /**
     * Return the managed EntityManager for the persistence unit.
     */
    public EntityManager getEntityManager(String persistenceUnit) {
        String contextName = "java:comp/env/persistence/" + persistenceUnit + "/entity-manager";
        try {
            return (EntityManager)new InitialContext().lookup(contextName);
        } catch (NamingException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Return the managed EntityManagerFactory for the persistence unit.
     */
    public EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        String contextName = "java:comp/env/persistence/" + persistenceUnit + "/factory";
        try {
            return (EntityManagerFactory)new InitialContext().lookup(contextName);
        } catch (NamingException exception) {
            throw new RuntimeException(exception);
        }
    }
    
}
