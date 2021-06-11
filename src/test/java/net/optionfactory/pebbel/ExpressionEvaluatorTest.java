package net.optionfactory.pebbel;

import java.io.StringReader;
import net.optionfactory.pebbel.ast.BooleanExpression;
import net.optionfactory.pebbel.ast.NumberExpression;
import net.optionfactory.pebbel.execution.ExpressionEvaluator;
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

    private double evalNumber(String text) throws ParseException {
        final ExpressionEvaluator<Object, Object> ce = new ExpressionEvaluator<>();
        final JavaccParser parser = new JavaccParser(new StringReader(text));
        final NumberExpression expression = parser.numberExpression();
        return ce.visit(expression, BindingsMother.SYMBOLS);
    }

    @Test
    public void canEvaluateNumberLiteral() throws ParseException {
        Assert.assertEquals(1d, evalNumber("1"), 0.000001);
    }

    @Test
    public void canEvaluateNegativeNumberLiteral() throws ParseException {
        Assert.assertEquals(-1d, evalNumber("-1"), 0.000001);
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
