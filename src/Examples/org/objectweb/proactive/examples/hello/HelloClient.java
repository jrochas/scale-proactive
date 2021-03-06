/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.hello;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This shows an example of how to access another Active Object,
 * which may have been created by another application.
 */
public class HelloClient {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** Looks for a Hello Active Object bepending on args, and calls a method on it */
    public static void main(String[] args) {
        Hello myServer;
        String message;
        try {
            // checks for the server's URL
            if (args.length == 0) {
                // There is no url to the server, so create an active server within this VM
                myServer = (Hello) org.objectweb.proactive.api.PAActiveObject.newActive(
                        Hello.class.getName(), new Object[] { "local" });
            } else {
                // Lookups the server object
                logger.info("Using server located on " + args[0]);
                myServer = org.objectweb.proactive.api.PAActiveObject.lookupActive(Hello.class, args[0]);
            }

            // Invokes a remote method on this object to get the message
            message = myServer.sayHello().toString();
            // Prints out the message
            logger.info("The message is : " + message);
        } catch (Exception e) {
            logger.error("Could not reach/create server object");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
