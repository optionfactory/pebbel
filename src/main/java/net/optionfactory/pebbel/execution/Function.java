package net.optionfactory.pebbel.execution;

import net.optionfactory.pebbel.ast.Source;
import net.optionfactory.pebbel.results.Problem;

public interface Function {

    public Object perform(Source source, Object[] t);

    public class ExecutionException extends RuntimeException {

        public final Problem problem;

        public ExecutionException(String reason, Source source, Throwable cause) {
            super(reason, cause);
            this.problem = Problem.of("EXECUTION_EXCEPTION", reason, source, cause);
        }
    }

}
