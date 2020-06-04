package net.optionfactory.pebbel.interpreted;

import java.io.StringReader;
import net.optionfactory.pebbel.parsing.ast.BooleanExpression;
import net.optionfactory.pebbel.parsing.JavaccParser;
import net.optionfactory.pebbel.parsing.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionEvaluatorTest {

    private boolean evalBool(String text) throws ParseException {
        final ExpressionEvaluator<Object, Object> ce = new ExpressionEvaluator<>();
        final JavaccParser parser = new JavaccParser(new StringReader(text));
        final BooleanExpression expression = parser.booleanExpression();
        return ce.visit(expression, BindingsMother.SYMBOLS);
    }

    @Test
    public void canEvaluateFunctionCall() throws ParseException {
        Assert.assertEquals(true, evalBool("bool('true')"));
    }

    @Test
    public void evaluationIsPerformedWithShortCircuit() throws ParseException {
        Assert.assertEquals(false, evalBool("true() && false()"));
    }

    @Test
    public void rhsIsEvaluatedOnlyIfNeeded() throws ParseException {
        Assert.assertEquals(false, evalBool("false() && b0rked()"));
        Assert.assertEquals(true, evalBool("true() || b0rked()"));
    }

    @Test
    public void canNestFunctionCalls() throws ParseException {
        Assert.assertEquals(false, evalBool("not(bool('true'))"));
    }

    @Test
    public void canChainMultipleExpressions() throws ParseException {
        Assert.assertEquals(true, evalBool("false() || true() && true()"));
    }
}
