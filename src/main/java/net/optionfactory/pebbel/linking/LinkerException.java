package net.optionfactory.pebbel.linking;

import java.util.List;
import net.optionfactory.pebbel.ast.Source;
import net.optionfactory.pebbel.results.LanguageException;
import net.optionfactory.pebbel.results.Problem;

public class LinkerException extends LanguageException {

    private final List<Problem> problems;

    public LinkerException(List<Problem> problems) {
        super("linker-exception:", String.format("linking problems: %s", problems.toString()));
        this.problems = problems;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public static void enforceNoProblems(List<Problem> problems) {
        if (problems.isEmpty()) {
            return;
        }
        throw new LinkerException(problems);
    }

    public static Problem problem(String type, String reason, Source source, String symbol, Integer index, Object expected, Object got) {
        final Details details = new Details();
        details.source = source;
        details.symbol = symbol;
        details.index = index;
        details.expected = expected;
        details.got = got;
        return Problem.of(type, reason, details);
    }

    public static class Details {

        public Source source;
        public String symbol;
        public Integer index;
        public Object expected;
        public Object got;

        @Override
        public String toString() {
            return String.format("%s@%s index:%s expected: %s, got: %s", symbol, source, index, expected, got);
        }

    }

}
