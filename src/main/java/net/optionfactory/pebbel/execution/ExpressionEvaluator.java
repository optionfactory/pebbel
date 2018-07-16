package net.optionfactory.pebbel.execution;

import java.util.Arrays;
import net.optionfactory.pebbel.ast.BooleanExpression;
import net.optionfactory.pebbel.ast.Expression;
import net.optionfactory.pebbel.ast.FunctionCall;
import net.optionfactory.pebbel.ast.NumberExpression;
import net.optionfactory.pebbel.ast.NumberLiteral;
import net.optionfactory.pebbel.ast.ShortCircuitExpression;
import net.optionfactory.pebbel.ast.StringExpression;
import net.optionfactory.pebbel.ast.StringLiteral;
import net.optionfactory.pebbel.ast.Variable;
import net.optionfactory.pebbel.execution.Function.ExecutionException;
import net.optionfactory.pebbel.loading.Symbols;
import net.optionfactory.pebbel.results.Result;

/**
 * An AST visitor evaluating an expression.
 */
public class ExpressionEvaluator<VV, VDMD> implements Expression.Visitor<Object, Symbols<VV, VDMD>> {

    public <R> Result<R> evaluate(Symbols<VV, VDMD> symbols, Expression expression) {
        try {
            return Result.value((R) expression.accept(this, symbols));
        } catch (ExecutionException ex) {
            return Result.error(ex.problem);
        }
    }

    @Override
    public Object visit(Expression node, Symbols<VV, VDMD> symbols) {
        return node.accept(this, symbols);
    }

    @Override
    public Object visit(Variable node, Symbols<VV, VDMD> symbols) {
        return symbols.variables.value(node.name).orElse(null);
    }

    @Override
    public String visit(StringLiteral node, Symbols<VV, VDMD> symbols) {
        return node.literal;
    }

    @Override
    public String visit(StringExpression node, Symbols<VV, VDMD> symbols) {
        return (String) node.accept(this, symbols);
    }

    @Override
    public Boolean visit(BooleanExpression node, Symbols<VV, VDMD> symbols) {
        return (Boolean) node.accept(this, symbols);
    }

    @Override
    public Double visit(NumberExpression node, Symbols<VV, VDMD> symbols) {
        return (Double) node.accept(this, symbols);
    }

    @Override
    public Double visit(NumberLiteral node, Symbols<VV, VDMD> symbols) {
        return node.value;
    }

    @Override
    public Object visit(FunctionCall node, Symbols<VV, VDMD> symbols) {
        final Object[] arguments = Arrays.stream(node.arguments)
                .map(n -> n.accept(this, symbols))
                .toArray();
        return symbols.functions.value(node.function).get().perform(node.source, arguments);
    }

    @Override
    public Boolean visit(ShortCircuitExpression node, Symbols<VV, VDMD> symbols) {
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
