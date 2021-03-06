/*******************************************************************************
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation
 *     Gunnar Wagenknecht - isExternal support
 ******************************************************************************/
package org.eclipse.persistence.internal.nosql.adapters.mongo;

import javax.resource.*;
import javax.resource.cci.*;

import org.eclipse.persistence.exceptions.ValidationException;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Connection to Mongo
 * This connection wraps a Mongo DB.
 *
 * @author James
 * @since EclipseLink 2.7
 */
public class MongoDatabaseConnection implements Connection {
    protected MongoJCAConnectionSpec spec;
    protected MongoTransaction transaction;
    protected MongoDatabase db;
    protected boolean isExternal;
    private MongoClient mongo;

    /**
     * Create the connection on a native AQ session.
     * The session must be connected to a JDBC connection.
     */
    public MongoDatabaseConnection(MongoClient mongo, MongoDatabase db, boolean isExternal, MongoJCAConnectionSpec spec) {
        this.mongo = mongo;
        this.db = db;
        this.transaction = new MongoTransaction(this);
        this.spec = spec;
        this.isExternal = isExternal;
    }

    public MongoClient getMongo() {
        return mongo;
    }

    public MongoDatabase getDB() {
        return db;
    }

    /**
     * Close the AQ native session and the database connection.
     */
    public void close() throws ResourceException {
        try {
            this.getMongo().close();
        } catch (Exception exception) {
            ResourceException resourceException = new ResourceException(exception.toString());
            resourceException.initCause(exception);
            throw resourceException;
        }
    }

    public Interaction createInteraction() {
        return new MongoDatabaseInteraction(this);
    }

    public MongoJCAConnectionSpec getConnectionSpec() {
        return spec;
    }

    public LocalTransaction getLocalTransaction() {
        return transaction;
    }

    public MongoTransaction getMongoTransaction() {
        return transaction;
    }

    public ConnectionMetaData getMetaData() {
        return new MongoDatabaseConnectionMetaData(this);
    }

    /**
     * Result sets are not supported.
     */
    public ResultSetInfo getResultSetInfo() {
        throw ValidationException.operationNotSupported("getResultSetInfo");
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }
}