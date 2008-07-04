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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.HostFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.HostListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public final class HostEditPart extends AbstractMonitoringEditPart {
    private HostObject castedModel;
    private HostFigure castedFigure;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public HostEditPart(HostObject model) {
        super(model);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * Convert the result of EditPart.getModel()
     * to HostObject (the real type of the model).
     * @return the casted model
     */
    @SuppressWarnings("unchecked")
    @Override
    public HostObject getCastedModel() {
        if (castedModel == null) {
            castedModel = (HostObject) getModel();
        }
        return castedModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HostFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (HostFigure) getFigure();
        }
        return castedFigure;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(arg instanceof MVCNotification)) {
            return;
        }

        final MVCNotification notif = (MVCNotification) arg;
        MVCNotificationTag mvcNotif = notif.getMVCNotification();
        Object data = notif.getData();
        switch (mvcNotif) {
            case STATE_CHANGED: {
                if (data == State.NOT_MONITORED) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            refreshChildren();
                        }
                    });
                    return;
                }
            }
            case HOST_OBJECT_UPDATED_OSNAME_AND_VERSON: {
                if (data instanceof String) {
                    getCastedFigure().changeTitle((String) data);
                }
            }
        } //switch

        getViewer().getControl().getDisplay().asyncExec(this);
    }

    @Override
    public void run() {
        refresh();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated
     * with the type of model object the
     * EditPart is associated with. So here, it returns a new HostFigure.
     * @return a new HostFigure view associated with the HostObject model.
     */
    protected IFigure createFigure() {
        HostFigure figure = new HostFigure(getCastedModel().toString());
        HostListener listener = new HostListener(getCastedModel(), figure, getMonitoringView());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    /**
     * Returns a List containing the children model objects.
     * @return the List of children
     */
    protected List<AbstractData> getModelChildren() {
        return getCastedModel().getMonitoredChildrenAsList();
    }

    /**
     * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
     */
    protected void createEditPolicies() { /* Do nothing */
    }
}
