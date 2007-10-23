package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.util.ArrayList;

import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.IllegalReturnTypeException;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidParameterException;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;


public class AOMatlabSplitter extends AOSimpleMatlab {

    /**
         *
         */
    private static final long serialVersionUID = -1673684977325219341L;
    private int numberOfChildren;

    public AOMatlabSplitter() {
    }

    /**
     * Constructor for a Splitter task
     * @param matlabCommandName the name of the Matlab command
     * @param inputScript a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     * @param numberOfChildren the number of children to which the input will be divided
     */
    public AOMatlabSplitter(String matlabCommandName, String inputScript,
        ArrayList<String> scriptLines, Integer numberOfChildren) {
        super(matlabCommandName, inputScript, scriptLines);
        this.numberOfChildren = numberOfChildren;
    }

    public Object execute(TaskResult... results) throws Throwable {
        if (results.length > 1) {
            throw new InvalidNumberOfParametersException(results.length);
        }
        if (results.length == 1) {
            TaskResult res = results[0];
            if (res.hadException()) {
                throw res.getException();
            }
            if (!(res.value() instanceof Token)) {
                throw new InvalidParameterException(res.value().getClass());
            }
            Token in = (Token) res.value();
            MatlabEngine.put("in", in);
        }
        MatlabEngine.put("nout", new IntToken(numberOfChildren));
        executeScript();
        Token result = MatlabEngine.get("out");
        if (!(result instanceof ArrayToken)) {
            throw new IllegalReturnTypeException(result.getClass());
        }

        return new SplittedResult((ArrayToken) result);
    }
}
