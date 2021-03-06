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
package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.Vector;


public class OutQuery extends Query {
    public int bound1;
    public int bound2;
    public int bbound1;
    public int bbound2;
    public int lastmask;
    public int endbit;
    public int[] board;

    public OutQuery(int q, int n, int i, int j, int m, int e) {
        super(n, 1, q << 1, (q | ~((1 << n) - 1)), q >> 1);
        initParameters = new int[] { q, n, i, j, m, e };

        int[] b = new int[n];
        b[0] = q;
        bound1 = i;
        bound2 = j;
        lastmask = m;
        endbit = e;
        board = b;
    }

    public long run() {
        long n = Out.run(this);

        //System.out.println(mask(down,this.n)+" "+n);
        return (n);
    }

    public Query next(int q) {
        int[] nb = new int[n];
        for (int i = 0; i < done; i++)
            nb[i] = board[i];
        nb[done] = q;
        OutQuery r = new OutQuery(q, n, bound1, bound2, lastmask, endbit);
        r.bbound1 = bbound1;
        r.bbound2 = bbound2;
        r.board = nb;
        r.left = (left | q) << 1;
        r.down = (down | q);
        r.right = (right | q) >> 1;
        r.done = done + 1;
        return (r);
    }

    public Vector split(Vector v) {
        int n = this.n;
        int sidemask = (1 << (n - 1)) | 1;
        int y = done;
        int todo = ~(left | down | right);
        if (y < bound1) {
            todo |= sidemask;
            todo ^= sidemask;
        } else if (y == bound2) {
            if ((down & sidemask) == 0) {
                return (v);
            }
            if ((down & sidemask) != sidemask) {
                todo &= sidemask;
            }
        }
        while (todo != 0) {
            int q = -todo & todo;
            if (y == bound1) {
                bbound1 = q;
            }
            if (y == bound2) {
                bbound2 = q;
            }
            todo ^= q;
            v.add(next(q));
        }
        return (v);
    }
}
