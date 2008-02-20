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
package org.eclipse.persistence.jpa.config;

/**
 * Query type hint values.
 * 
 * The class contains all the valid values for EclipseLinkQueryHints.QUERY_TYPE query hint.
 * 
 * JPA Query Hint Usage:
 * 
 * <p><code>query.setHint(EclipseLinkQueryHints.QueryType, QueryType.ReadObject);</code>
 * <p>or 
 * <p><code>@QueryHint(name=EclipseLinkQueryHints.QueryType, value=QueryType.ReadObject)</code>
 * 
 * <p>Hint values are case-insensitive.
 * "" could be used instead of default value CacheUsage.DEFAULT.
 * 
 * @see EclipseLinkQueryHints
 */
public class QueryType {
    public static final String  Auto = "Auto";
    public static final String  ReadObject = "ReadObject";
    public static final String  ReadAll = "ReadAll";
    public static final String  Report = "Report";
 
    public static final String DEFAULT = Auto;
}
