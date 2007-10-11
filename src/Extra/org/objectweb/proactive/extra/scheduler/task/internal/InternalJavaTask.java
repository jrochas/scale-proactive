/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.scheduler.task.internal;

import org.objectweb.proactive.extra.scheduler.common.exception.TaskCreationException;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableJavaTask;
import org.objectweb.proactive.extra.scheduler.common.task.ExecutableTask;


/**
 * Description of a java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author ProActive Team
 * @version 1.0, Jul 16, 2007
 * @since ProActive 3.2
 */
public class InternalJavaTask extends InternalAbstractJavaTask {

    /** Serial Version UID */
    private static final long serialVersionUID = -6946803819032140410L;

    /** the java task to launch */
    private ExecutableJavaTask task;

    /**
     * ProActive empty constructor
     */
    public InternalJavaTask() {
    }

    /**
     * Create a new Java task descriptor using instantiated java task.
     *
     * @param task the already instantiated java task.
     */
    public InternalJavaTask(ExecutableJavaTask task) {
        this.task = task;
    }

    /**
     * Create a new Java task descriptor using a specific Class.
     *
     * @param taskClass the class instance of the class to instantiate.
     */
    public InternalJavaTask(Class<ExecutableJavaTask> taskClass) {
        super(taskClass);
    }

    /**
     * @see org.objectweb.proactive.extra.scheduler.task.internal.InternalTask#getTask()
     */
    @Override
    public ExecutableTask getTask() throws TaskCreationException {
        // create task from taskClass
        if (task == null) {
            try {
                task = (ExecutableJavaTask) taskClass.newInstance();
            } catch (InstantiationException e) {
                throw new TaskCreationException("Cannot create javatask from task class ",
                    e);
            } catch (IllegalAccessException e) {
                throw new TaskCreationException("Cannot create javatask from task class ",
                    e);
            }
        }

        // init task
        try {
            task.init(args);
        } catch (Exception e) {
            throw new TaskCreationException("Cannot initialize task ", e);
        }
        return task;
    }

    /**
     * Set the instantiated java task.
     *
     * @param task the instantiated java task.
     */
    public void setTask(ExecutableJavaTask task) {
        this.task = task;
    }
}
