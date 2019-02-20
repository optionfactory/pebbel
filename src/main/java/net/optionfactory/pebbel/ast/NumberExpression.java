package net.optionfactory.pebbel.ast;

/**
 * An expression evaluating as a Double. Can be either a number literal (e.g.:
 * {@code 123.4} or a function returning a Double
 * {@code num("123")}.
 */
public interface NumberExpression extends Expression {

    <R, T> R accept(Visitor<R, T> visitor, T value);

    public interface Visitor<R, T> extends FunctionCall.Visitor<R, T>, NumberLiteral.Visitor<R, T> {

        R visit(NumberExpression node, T value);
    }
}
