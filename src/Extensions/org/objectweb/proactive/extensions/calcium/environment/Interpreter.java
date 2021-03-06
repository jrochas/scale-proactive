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
package org.objectweb.proactive.extensions.calcium.environment;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class corresponds to a skeleton interpreter, which
 * can be seen as a worker of the skeleton framework.
 *
 * The interpreter will loop taking the top skeletal instruction
 * from the task's instruction stack and execute it.
 *
 * When the instruction is executed, the task's stack can be
 * modified. For example the "if" skeleton will choose which
 * branch must be computed and place this branch in the
 * task's stack.
 *
 * The loop will continue to execute until a task is found
 * to have children tasks, or the task has no more instructions.
 * In either case, the task (and it's children) will be returned.
 *
 * @author The ProActive Team
 *
 */
public class Interpreter implements Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);

    public Task interpret(FileServerClient fserver, Task task, Timer tUnusedCPU) {
        Timer timer = new Timer();

        timer.start();

        try {
            SkeletonSystemImpl system = new SkeletonSystemImpl();

            FileStaging files = stageIn(task, system, fserver);
            task = theLoop(task, system, tUnusedCPU);
            task = stageOut(task, files, system, fserver);
        } catch (Exception e) {
            task.setException(e);
        }

        //The task is finished
        task.getStats().addComputationTime(timer.getTime());

        timer.stop();

        return task;
    }

    public FileStaging stageIn(Task<?> task, SkeletonSystemImpl system, FileServerClient fserver)
            throws Exception {
        //Keep track of current stored files

        return new FileStaging(task, fserver, system.getWorkingSpace());
    }

    public Task<?> theLoop(Task<?> task, SkeletonSystemImpl system, Timer timer) throws Exception {
        timer.stop();
        try {
            //Stop loop if task is finished or has ready children 
            while (task.hasInstruction() && !task.family.hasReadyChildTask()) {
                if (logger.isDebugEnabled()) {
                    System.out.println(task.stackToString());
                }

                task = task.compute(system);
            } //while
        } catch (Exception e) {
            throw e;
        } finally {
            task.getStats().addUnusedCPUTime(timer.getTime());
            timer.start();
        }

        return task;
    }

    public Task<?> stageOut(Task<?> task, FileStaging files, SkeletonSystemImpl system,
            FileServerClient fserver) throws Exception {

        //Update new/modified/unreferenced files
        files.stageOut(fserver, task);

        //From now on, the parameters inside each task have different spaces
        task.family.splitfReadyTasksSpace();

        //Clean the working space
        system.getWorkingSpace().delete();

        return task;
    }
}
