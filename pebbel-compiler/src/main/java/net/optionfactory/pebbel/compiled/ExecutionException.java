package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.results.LanguageException;
import net.optionfactory.pebbel.results.Problem;

public class ExecutionException extends LanguageException {

    private static final long serialVersionUID = 1L;
    public final Problem problem;

    public ExecutionException(String reason, Source source, Throwable cause) {
        super("execution-exception:", String.format("execution problem: %s %s %s", reason, source, cause));
        this.problem = Problem.of("EXECUTION_EXCEPTION", reason, source, cause);
    }
}
