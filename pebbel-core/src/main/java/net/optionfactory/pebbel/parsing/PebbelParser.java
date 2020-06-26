package net.optionfactory.pebbel.parsing;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.optionfactory.pebbel.Parser;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;

public class PebbelParser implements Parser {

    @Override
    public Result<Expression> parse(String source, Class<?> expectedType) {
        if (source == null || source.trim().isEmpty()) {
            final ParsingProblemDetails emptySourceDetails = new ParsingProblemDetails();
            emptySourceDetails.source = Source.of(1, 1, 1, 1);
            emptySourceDetails.image = "";
            emptySourceDetails.expected = null;
            return Result.error(Problem.of("UNPARSEABLE", "Source expression is empty", emptySourceDetails));
        }
        try {
            final JavaccParser parser = new JavaccParser(new StringReader(source));
            return Result.value(parser.parse(expectedType));
        } catch (ParseException ex) {
            final Token current = ex.currentToken;
            final Token reference = current.next != null ? current.next : current;
            final ParsingProblemDetails details = new ParsingProblemDetails();
            details.source = Source.of(
                    Math.max(reference.beginLine, 1),
                    Math.max(reference.beginColumn, 1),
                    Math.max(reference.endLine, 1),
                    Math.max(reference.endColumn, 1));
            details.image = current.image;
            details.expected = Stream.of(ex.expectedTokenSequences)
                    .map(this::idsToLabels)
                    .distinct()
                    .map(l -> l.toArray(new String[0]))
                    .toArray(l -> new String[l][]);
            return Result.error(Problem.of("UNPARSEABLE", ex.getMessage(), details));
        }
    }

    private List<String> idsToLabels(int[] tids) {
        return Arrays.stream(tids)
                .mapToObj(tid -> JavaccParserConstants.tokenImage[tid])
                .collect(Collectors.toList());
    }

    public static class ParsingProblemDetails {

        public Source source;
        public String image;
        public String[][] expected;
    }

}
