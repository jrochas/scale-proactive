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
package org.objectweb.proactive.ext.webservices.soap;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Response;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.RPCRouter;
import org.apache.soap.util.Provider;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.http.util.HttpMarshaller;
import org.objectweb.proactive.ext.webservices.WSConstants;


/**
 * @author vlegrand
 * This class is responsible to locate an active object deployed as a web service and invoke a method on this object.
 */
public class ProActiveProvider extends WSConstants implements Provider {

    static {
        System.setSecurityManager(new java.rmi.RMISecurityManager());

        // Creation of the default level which contains standard exception handlers
        ProActive.defaultLevel = new HashMap();
        //        HandlerManager.initialize();
    }

    private DeploymentDescriptor dd;
    private Envelope envelope;
    private Call call;
    private String methodName;
    private String targetObjectURI;
    private HttpServlet servlet;
    private HttpSession session;
    private Object targetObject;

    /**
     * This method is responsible for locating the active object.
     * First, we make a lookup active in order to retrieve the active object and then store it in the private field tqrgetObject
     * @see org.apache.soap.util.Provider
     */
    public void locate(DeploymentDescriptor dd, Envelope env, Call call,
        String methodName, String targetObjectURI, SOAPContext reqContext)
        throws SOAPException {
        //Set some properties to the context	
        HttpServlet servlet = (HttpServlet) reqContext.getProperty(Constants.BAG_HTTPSERVLET);
        HttpSession session = (HttpSession) reqContext.getProperty(Constants.BAG_HTTPSESSION);

        System.out.println("================================================");
        System.out.println("In ProActiveProvider.locate()");
        System.out.println("Method: " + methodName);
        System.out.println("URI: " + targetObjectURI);
        System.out.println("DD.ServiceClass: " + dd.getServiceClass());
        System.out.println("DD.ProviderClass: " + dd.getProviderClass());
        System.out.println("Call.MethodName: " + call.getMethodName());

        Hashtable props = dd.getProps();
        String className = dd.getProviderClass();

        this.dd = dd;
        this.envelope = env;
        this.call = call;
        this.methodName = methodName;
        this.targetObjectURI = targetObjectURI;
        this.servlet = servlet;
        this.session = session;

        if (!RPCRouter.validCall(dd, call)) {
            System.err.println("It's not a valid call");

            SOAPException e = new SOAPException(Constants.FAULT_CODE_CLIENT,
                    "It's not a  valid call");
            throw e;
        }

        byte[] serObj = (byte[]) props.get("Stub");

        boolean isInterfaceComponent = ((String) props.get(ProActiveDeployer.COMPONENT_INTERFACE)).equals(
                "true");

        //System.out.println("");
        try {
            if (!isInterfaceComponent) {
                targetObject = HttpMarshaller.unmarshallObject(serObj);
            } else {
                //Component c = (Component)ProActiveXMLUtils.deserializeObject(serObj);
                Object o = HttpMarshaller.unmarshallObject(serObj);
                Component c = (Component) o;
                targetObject = c.getFcInterface(targetObjectURI);
            }
        } catch (Exception e) {
            System.err.println("Exception : " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
     *  This method is responsible to invoke the method on the targetObject
     * First We invoke the method thanks to RPCRouter.invoke() method then we build an enveloppe that contains the response.
     * @see org.apache.soap.util.Provider
     **/
    public void invoke(SOAPContext reqContext, SOAPContext resContext)
        throws SOAPException {
        System.out.println("=============================================");
        System.out.println("In ProActiveProvider.invoke()");

        //  System.out.println("targetObject = " + targetObject);
        String reponse = null;

        //dd.setProviderClass(targetObject.getClass().getName());
        // Add logic to invoke the service and get back the result here
        try {
            Response resp = RPCRouter.invoke(dd, call, targetObject,
                    reqContext, resContext);

            //build the enveloppe that contains the response 	
            Envelope env = resp.buildEnvelope();
            StringWriter sw = new StringWriter();
            env.marshall(sw, call.getSOAPMappingRegistry(), resContext);
            resContext.setRootPart(sw.toString(),
                Constants.HEADERVAL_CONTENT_TYPE_UTF8);
        } catch (Exception e) {
            System.out.println("--- >exception ! " + e.getMessage() + " -- " +
                e.getClass().getName());
            e.printStackTrace(System.out);

            SOAPException ex = new SOAPException(Constants.FAULT_CODE_SERVER,
                    e.getMessage());
            System.out.println(
                "An error has occured when trying to invoke the method on the object");
            throw ex;
        }
    }
}
