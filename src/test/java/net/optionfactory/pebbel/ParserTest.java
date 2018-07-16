package net.optionfactory.pebbel;

import java.util.Arrays;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.results.Problem;
import net.optionfactory.pebbel.results.Result;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rferranti
 */
public class ParserTest {

    @Test
    public void nonTerminatedExpressionsAreReportedAsProblem() {
        final String incomplete = "bool('TRUE') or";
        final PebbelParser language = new PebbelParser();
        final Result<Expression> parse = language.parse(incomplete, Boolean.class);
        Assert.assertTrue(parse.isError());
    }

    @Test
    public void anEmptyExpressionResultInProblem() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("  ", Boolean.class);
        Assert.assertEquals(true, result.isError());
    }

    @Test
    public void canParseFunctionCall() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("function_name()", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());
    }

    @Test
    public void spacesAreIgnored() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse(" function_name ( \n\t\r) ", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());

    }

    @Test
    public void stringLiteralsCanBeWrittenInSingleQuotes() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("function_name('LITERAL')", Boolean.class);
        Assert.assertEquals(false, result.isError());
    }

    @Test
    public void stringLiteralsCanBeWrittenInDoubleQuotes() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("function_name(\"LITERAL\")", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());

    }

    @Test
    public void canCallNaryFunctions() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("nary('1', '2', '3')", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());

    }

    @Test
    public void canPassVariablesToFunctions() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("nary(VAR1, VAR2)", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());

    }
    
    @Test
    public void canUseAndOperator() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("a() && b()", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());
    }

    @Test
    public void canUseOrOperator() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("a() || b()", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());
    }

    @Test
    public void canUseColonInFunctionNames() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("day:is_even()", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());
    }

    @Test
    public void canUseParensForPrecedence() {
        final PebbelParser dsl = new PebbelParser();
        final Result<Expression> result = dsl.parse("a() && ( b() || c() )", Boolean.class);
        Assert.assertEquals(Arrays.<Problem>asList(), result.getErrors());
    }
}
