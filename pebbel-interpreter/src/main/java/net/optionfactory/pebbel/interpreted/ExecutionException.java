package net.optionfactory.pebbel.interpreted;

import java.util.HashMap;
import java.util.Map;
import net.optionfactory.pebbel.results.LanguageException;
import net.optionfactory.pebbel.results.Problem;

public class ExecutionException extends LanguageException {

    private static final long serialVersionUID = 1L;
    public final Problem problem;

    public ExecutionException(String type, Problem problem) {
        super(type, problem.reason);
        this.problem = problem;
    }

    public static Problem problem(String reason, int col, int colEnd) {
        final Map<String, Object> details = new HashMap<>();
        details.put("col", col);
        details.put("endCol", colEnd);
        return Problem.of("EXECUTION_PROBLEM", reason, details);
    }

}
