/*******************************************************************************
 * Copyright (c) 2012, 2015 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     11/19/2012-2.5 Guy Pelletier
 *       - 389090: JPA 2.1 DDL Generation Support (foreign key metadata support)
 ******************************************************************************/
package org.eclipse.persistence.testing.models.jpa22.advanced;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="JPA22_SHOE_TAG")
public class ShoeTag {
    @Id
    @GeneratedValue
    protected Integer id;
    protected String tag;

    public ShoeTag() {}

    public ShoeTag(String tag) {
        this.tag = tag;
    }

    public Integer getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
