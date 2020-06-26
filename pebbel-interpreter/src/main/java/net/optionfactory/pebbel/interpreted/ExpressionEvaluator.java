package net.optionfactory.pebbel.interpreted;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.parsing.ast.BooleanExpression;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.parsing.ast.FunctionCall;
import net.optionfactory.pebbel.parsing.ast.NumberExpression;
import net.optionfactory.pebbel.parsing.ast.NumberLiteral;
import net.optionfactory.pebbel.parsing.ast.ShortCircuitExpression;
import net.optionfactory.pebbel.parsing.ast.StringExpression;
import net.optionfactory.pebbel.parsing.ast.StringLiteral;
import net.optionfactory.pebbel.parsing.ast.Variable;
import net.optionfactory.pebbel.results.Result;

/**
 * An AST visitor evaluating an expression.
 */
public class ExpressionEvaluator<VAR, VARMETA> implements Expression.Visitor<Object, Symbols<VAR, VARMETA, MethodHandle>> {

    public <R> Result<R> evaluate(Symbols<VAR, VARMETA, MethodHandle> symbols, Expression expression) {
        try {
            return Result.value((R) expression.accept(this, symbols));
        } catch (ExecutionException ex) {
            return Result.error(ex.problem);
        }
    }

    @Override
    public Object visit(Expression node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return node.accept(this, symbols);
    }

    @Override
    public Object visit(Variable node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return symbols.variables.value(node.name).orElse(null);
    }

    @Override
    public String visit(StringLiteral node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return node.literal;
    }

    @Override
    public String visit(StringExpression node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return (String) node.accept(this, symbols);
    }

    @Override
    public Boolean visit(BooleanExpression node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return (Boolean) node.accept(this, symbols);
    }

    @Override
    public Double visit(NumberExpression node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return (Double) node.accept(this, symbols);
    }

    @Override
    public Double visit(NumberLiteral node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        return node.value;
    }

    @Override
    public Object visit(FunctionCall node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        final Object[] arguments = Arrays.stream(node.arguments)
                .map(n -> n.accept(this, symbols))
                .toArray();
        final MethodHandle mh = symbols.functions.value(node.function).get();
        try {
            return mh.invokeWithArguments(arguments);
        } catch (Throwable ex) {
            throw new ExecutionException(ex.getMessage(), node.source, ex);
        }

    }


    @Override
    public Boolean visit(ShortCircuitExpression node, Symbols<VAR, VARMETA, MethodHandle> symbols) {
        Boolean value = null;
        for (int i = 0; i != node.terms.length; ++i) {
            final BooleanExpression bexp = node.terms[i];
            value = (Boolean) bexp.accept(this, symbols);
            if (i == node.operators.length || node.operators[i].shouldShortCircuit(value)) {
                return value;
            }
        }
        return value;
    }

}
