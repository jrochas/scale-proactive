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
package nonregressiontest.component.creation.remote.newactive.primitive;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import nonregressiontest.component.ComponentTest;
import nonregressiontest.component.creation.ComponentA;
import nonregressiontest.component.creation.ComponentInfo;
import nonregressiontest.descriptor.defaultnodes.TestNodes;


/**
 * @author Matthieu Morel
 *
 * creates a primitive component on a remote node with ACs
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;
    String remoteHost;

    public Test() {
        super("Creation of a primitive component on a remote node",
            "Test newActiveComponent method for a primitive component on a remote node");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    public void action() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory type_factory = Fractal.getTypeFactory(boot);
        GenericFactory cf = Fractal.getGenericFactory(boot);

        componentA = cf.newFcInstance(type_factory.createFcType(
                    new InterfaceType[] {
                        type_factory.createFcItfType("componentInfo",
                            ComponentInfo.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE)
                    }),
                new ControllerDescription("componentA", Constants.PRIMITIVE),
                new ContentDescription(ComponentA.class.getName(),
                    new Object[] { "toto" }, TestNodes.getRemoteACVMNode()));
        //logger.debug("OK, instantiated the component");
        // start the component!
        Fractal.getLifeCycleController(componentA).startFc();
        ComponentInfo ref = (ComponentInfo) componentA.getFcInterface(
                "componentInfo");
        name = ref.getName();
        nodeUrl = ((ComponentInfo) componentA.getFcInterface("componentInfo")).getNodeUrl();
    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    public boolean preConditions() throws Exception {
        remoteHost = TestNodes.getRemoteHostname();
        return (super.preConditions() && (remoteHost != null));
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
    }

    public boolean postConditions() throws Exception {
        return (name.equals("toto") && (nodeUrl.indexOf(remoteHost) != -1));
    }
}
