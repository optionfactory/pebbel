package net.optionfactory.pebbel;

import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Stream;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.ast.Source;
import net.optionfactory.pebbel.parsing.JavaccParser;
import net.optionfactory.pebbel.parsing.JavaccParserConstants;
import net.optionfactory.pebbel.parsing.ParseException;
import net.optionfactory.pebbel.parsing.Token;
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
                .toArray(l -> new String[l][]);
            return Result.error(Problem.of("UNPARSEABLE", ex.getMessage(), details));
        }
    }
    
    private String[] idsToLabels(int[] tids){
        return Arrays.stream(tids)
                .mapToObj(tid -> JavaccParserConstants.tokenImage[tid])
                .toArray(l -> new String[l]);
    }

    public static class ParsingProblemDetails {

        public Source source;
        public String image;
        public String[][] expected;
    }

}
