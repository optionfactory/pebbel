package net.optionfactory.pebbel;

import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.linking.ExpressionVerifier;
import net.optionfactory.pebbel.results.Problem;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import net.optionfactory.pebbel.parsing.JavaccParser;
import net.optionfactory.pebbel.parsing.JavaccParserTokenManager;
import net.optionfactory.pebbel.parsing.ParseException;
import net.optionfactory.pebbel.parsing.SimpleCharStream;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionVerifierTest {

    public static final ExpressionVerifier EXPRESSION_CHECKER = new ExpressionVerifier();

    private Expression parse(String expr) throws ParseException {
        final JavaccParserTokenManager tokenizer = new JavaccParserTokenManager(new SimpleCharStream(new StringReader(expr)));
        final JavaccParser parser = new JavaccParser(tokenizer);
        return parser.terminalBooleanExpression();
    }

    @Test
    public void canEvaluateSingleFunctionCall() throws ParseException {
        final ExpressionVerifier.Request state = ExpressionVerifier.Request.of(BindingsMother.DESCRIPTORS, Boolean.class, new ArrayList<>());
        EXPRESSION_CHECKER.visit(parse("true()"), state);
        Assert.assertEquals(Arrays.<Problem>asList(), state.problems);
    }

    @Test
    public void unknownFunctionSymbolYieldsUNKNOWN_SYMBOL() throws ParseException {

        final ExpressionVerifier.Request state = ExpressionVerifier.Request.of(BindingsMother.DESCRIPTORS, Boolean.class, new ArrayList<>());
        EXPRESSION_CHECKER.visit(parse("unknown()"), state);
        Assert.assertEquals("UNKNOWN_SYMBOL", state.problems.get(0).type);
    }

    @Test
    public void wrongArityYieldsARITY_MISMATCH() throws ParseException {
        final ExpressionVerifier.Request state = ExpressionVerifier.Request.of(BindingsMother.DESCRIPTORS, Boolean.class, new ArrayList<>());
        EXPRESSION_CHECKER.visit(parse("false(true())"), state);
        Assert.assertEquals("ARITY_MISMATCH", state.problems.get(0).type);
    }

    @Test
    public void wrongArgumentTypeYieldsTYPE_MISMATCH() throws ParseException {
        final ExpressionVerifier.Request state = ExpressionVerifier.Request.of(BindingsMother.DESCRIPTORS, Boolean.class, new ArrayList<>());
        EXPRESSION_CHECKER.visit(parse("not('STRING')"), state);
        Assert.assertEquals("TYPE_MISMATCH", state.problems.get(0).type);
    }

}
