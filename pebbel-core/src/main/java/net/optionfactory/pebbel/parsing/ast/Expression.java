package net.optionfactory.pebbel.parsing.ast;

public interface Expression {

    <R, T> R accept(Visitor<R, T> visitor, T value);

    Source source();

    public interface Visitor<R, T> extends StringExpression.Visitor<R, T>, BooleanExpression.Visitor<R, T>, NumberExpression.Visitor<R, T> {

        R visit(Expression node, T value);
    }
}
