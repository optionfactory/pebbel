package net.optionfactory.pebbel.ast;

/**
 * An AST node representing a boolean expression. Can be either a
 * shortCircuiting expression (e.g: {@code a && b},{@code a || b}) or a function
 * call with a boolean return value (e.g: {@code true()}).
 */
public interface BooleanExpression extends Expression {

    <R, T> R accept(Visitor<R, T> visitor, T value);

    public interface Visitor<R, T> extends ShortCircuitExpression.Visitor<R, T>, FunctionCall.Visitor<R, T> {

        R visit(BooleanExpression node, T value);
    }
}
