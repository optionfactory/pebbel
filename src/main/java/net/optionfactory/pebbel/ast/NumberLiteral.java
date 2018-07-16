package net.optionfactory.pebbel.ast;

/**
 * A number literal.
 */
public class NumberLiteral implements NumberExpression {

    public Double value;
    public Source source;

    public static NumberLiteral of(Double value, Source source) {
        final NumberLiteral sl = new NumberLiteral();
        sl.value = value;
        sl.source = source;
        return sl;
    }

    public <R, T> R accept(NumberLiteral.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(NumberExpression.Visitor<R, T> visitor, T value) {
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

        R visit(NumberLiteral node, T value);
    }
}
