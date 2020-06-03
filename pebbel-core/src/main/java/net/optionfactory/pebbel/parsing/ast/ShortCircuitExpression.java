package net.optionfactory.pebbel.parsing.ast;

/*
 * A short circuited boolean expression. (e.g: {@code a() && b()})
 */
public class ShortCircuitExpression implements BooleanExpression {

    public BooleanOperator[] operators;
    public BooleanExpression[] terms;
    public Source source;

    public static ShortCircuitExpression of(BooleanOperator[] operators, BooleanExpression[] terms, Source source) {
        final ShortCircuitExpression scbe = new ShortCircuitExpression();
        scbe.operators = operators;
        scbe.terms = terms;
        scbe.source = source;
        return scbe;
    }

    public <R, T> R accept(ShortCircuitExpression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(BooleanExpression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(Expression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public Source source() {
        return source;
    }

    public interface Visitor<R, T> {

        R visit(ShortCircuitExpression node, T value);
    }
}
