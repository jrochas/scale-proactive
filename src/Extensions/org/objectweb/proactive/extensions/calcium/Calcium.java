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
package org.objectweb.proactive.extensions.calcium;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.environment.Environment;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentServices;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;
import org.objectweb.proactive.extensions.calcium.statistics.StatsGlobal;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


/**
 * This class corresponds to the main entry point of the skeleton framework.
 *
 * To instantiate this class, an {@link org.objectweb.proactive.extensions.calcium.environment.Environment  EnvironmentFactory}  must be provided.
 * The skeleton framework can be used with different EnvironmentFactories, for example:
 * <ul>
 * <li>{@link org.objectweb.proactive.extensions.calcium.environment.multithreaded.MultiThreadedEnvironment} Executes the framework using threads on the local machine.</li>
 * <li>{@link org.objectweb.proactive.extensions.calcium.environment.proactive.ProActiveEnvironment} Executes the framework using ProActive.</li>
 * <li>{@link org.objectweb.proactive.extra.calcium.environment.proactivescheduler.ProActiveSchedulerEnvironment ProActiveSchedulerEnvironment} Executes the framework using ProActive Scheduler.</li>
 * </ul>
 *
 *
 * @author The ProActive Team
 */
@PublicAPI
public class Calcium {
    private Facade facade;
    private TaskPool taskpool;
    private FileServerClient fserver;
    EnvironmentServices environment;

    /**
     * The main construction method
     *
     * @param environment An EnvironmentFactory that will be used to execute the framework
     */
    public Calcium(Environment environment) {

        EnvironmentServices env = (EnvironmentServices) environment;

        this.taskpool = env.getTaskPool();
        this.environment = env;
        this.facade = new Facade(taskpool);
        this.fserver = env.getFileServer();
    }

    /**
     * This method is used to instantiate a new stream from the framework.
     * The stream is then used to input a parameter T into the framework, and
     * then retrieve the results (R) from the framework.
     *
     * All T inputed into this stream will be computed using the
     * skeleton program specified as parameter.
     *
     * @param <T> The type of the T this stream will accept.
     * @param root The skeleton program that will be computed on each T inputed into the framework
     * @param <R> The result type of the skeleton program.
     * @return A {@link Stream Stream} that can input T and output  from the framework.
     */
    public <T extends java.io.Serializable, R extends java.io.Serializable> Stream<T, R> newStream(
            Skeleton<T, R> root) {
        return new Stream<T, R>(facade, fserver, root);
    }

    /**
     * Boots the framework by calling, among others,  start on the {@link org.objectweb.proactive.extensions.calcium.environment.Environment  EnvironmentFactory}.
     */
    public void boot() {
        facade.boot();
        environment.start();
    }

    /**
     * Shuts down the framework by calling shutdown, among others, shutdown on the {@link org.objectweb.proactive.extensions.calcium.environment.Environment  EnvironmentFactory}.
     */
    public void shutdown() {
        facade.shutdown();
        environment.shutdown();
    }

    /**
     * This method can be used to get a snapshot on the global statistics of the framework.
     * To get an updated version of the statistics, the method must be re-invoked.
     *
     * @return The current status of the global statistics.
     */
    public StatsGlobal getStatsGlobal() {
        return taskpool.getStatsGlobal();
    }
}
