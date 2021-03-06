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
package org.objectweb.proactive.extensions.timitspmd.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Element;
import org.objectweb.proactive.extensions.timitspmd.util.XMLHelper;


public class Benchmark extends Tag {
    private HashMap<String, String> variables;

    public Benchmark(Element eBench) {
        super(eBench);
        this.variables = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Iterator it = eBench.getChildren("descriptorVariable").iterator();
        while (it.hasNext()) {
            Element var = (Element) it.next();
            this.variables.put(var.getAttributeValue("name"), var.getAttributeValue("value"));
        }
    }

    @Override
    public String get(String name) {
        name = name.toLowerCase();
        String value = super.get(name);

        if (value != null) {
            return value;
        }

        // Specify default values
        if (name.equals("run")) {
            return "1";
        }
        if (name.equals("warmup")) {
            return "0";
        }
        if (name.equals("timeout")) {
            return "10000";
        }
        if (name.equals("removeextremums")) {
            return "false";
        }
        if (name.equals("writeeveryrun")) {
            return "true";
        }
        if (name.equals("descriptorgenerated")) {
            return "";
        }

        throw new RuntimeException("Variable benchmark.'" + name + "' missing in configuration file");
    }

    public HashMap<String, String> getVariables() {
        return this.variables;
    }

    @SuppressWarnings("unchecked")
    public static Benchmark[] toArray(List benchmarkList) {
        ArrayList<String> seqList;
        int quantity = benchmarkList.size();
        ArrayList<Benchmark> result = new ArrayList<Benchmark>();

        Pattern p = Pattern.compile("[^\\x23\\x7B\\x7D]*\\x23\\x7B" + // *#{
            "([^\\x7D]*)" + // A,B,C
            "\\x7D[^\\x7D\\x23\\x7B]*"); // }*

        for (int i = 0; i < quantity; i++) {
            Element eBench = (Element) benchmarkList.get(i);
            seqList = new ArrayList<String>();

            // 1 : searching sequences in attributes, then in descVariables
            searchSequences(eBench, p, seqList);
            @SuppressWarnings("unchecked")
            Iterator itVars = eBench.getChildren().iterator();
            while (itVars.hasNext()) {
                searchSequences((Element) itVars.next(), p, seqList);
            }

            // 2 : expanding sequences (recursive call)
            if (seqList.size() > 0) {
                expand(eBench, seqList, 0, result);
            } else {
                result.add(new Benchmark(eBench));
            }
        }

        return result.toArray(new Benchmark[1]);
    }

    private static void searchSequences(Element elt, Pattern p, ArrayList<String> seqList) {
        @SuppressWarnings("unchecked")
        Iterator<Attribute> itAttr = elt.getAttributes().iterator();
        while (itAttr.hasNext()) {
            Attribute attr = (Attribute) itAttr.next();
            Matcher m = p.matcher(attr.getValue());
            while (m.find()) {
                String sequence = m.group(1);
                if (!seqList.contains(sequence)) {
                    seqList.add(sequence);
                }
            }
        }
    }

    private static void expand(Element eBench, ArrayList<String> seqList, int index, ArrayList<Benchmark> out) {
        String seq = seqList.get(index);
        String[] values = seq.split(",");

        for (String value : values) {
            Element eBenchClone = (Element) eBench.clone();
            XMLHelper.replaceAll(eBenchClone, "\\x23\\x7B" + seq + "\\x7D", // #{*}
                    value);
            @SuppressWarnings("unchecked")
            Iterator itDesc = eBenchClone.getDescendants();
            while (itDesc.hasNext()) {
                Object eDesc = itDesc.next();
                if (eDesc instanceof Element) {
                    XMLHelper.replaceAll((Element) eDesc, "\\x23\\x7B" + seq + "\\x7D", // #{*},
                            value);
                }
            }

            if ((index + 1) < seqList.size()) {
                expand(eBenchClone, seqList, index + 1, out);
            } else {
                out.add(new Benchmark(eBenchClone));
            }
        }
    }
}
