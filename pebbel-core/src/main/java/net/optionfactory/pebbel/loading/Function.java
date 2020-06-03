package net.optionfactory.pebbel.loading;

import java.lang.reflect.Method;
import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.results.Problem;

public interface Function {

    public Method method();
    
    public Object perform(Source source, Object[] t);

    public class ExecutionException extends RuntimeException {

        public final Problem problem;

        public ExecutionException(String reason, Source source, Throwable cause) {
            super(reason, cause);
            this.problem = Problem.of("EXECUTION_EXCEPTION", reason, source, cause);
        }
    }

}
