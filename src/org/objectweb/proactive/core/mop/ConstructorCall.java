/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.mop;


/**
 * A reified constructor call.
 */
public interface ConstructorCall {

    /**
     * Makez a deep copy of all arguments of the constructor
     */
    public void makeDeepCopyOfArguments() throws java.io.IOException;

    /**
     * Return the name of the target class that constructor is for
     */
    public String getTargetClassName();

    /**
     * Performs the object construction that is reified vy this object
     * @throws InvocationTargetException
     * @throws ConstructorCallExecutionFailedException
     */
    public Object execute()
        throws java.lang.reflect.InvocationTargetException, 
            ConstructorCallExecutionFailedException;
}
