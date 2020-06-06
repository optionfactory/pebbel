package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.parsing.ast.BooleanExpression;
import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.parsing.ast.NumberExpression;
import net.optionfactory.pebbel.parsing.ast.StringExpression;

public abstract class AbstractVisitor<R,T> implements Expression.Visitor<R, T> {

    @Override
    public R visit(Expression node, T value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public R visit(BooleanExpression node, T value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public R visit(NumberExpression node, T value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public R visit(StringExpression node, T value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
