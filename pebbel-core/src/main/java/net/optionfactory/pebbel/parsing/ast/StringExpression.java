package net.optionfactory.pebbel.parsing.ast;

/**
 * An expression evaluating as a String. Can be either a string literal (e.g.:
 * {@code 'LITERAL'} or {@code "LITERAL"} or a function returning a string
 * {@code trim(" A ")}.
 */
public interface StringExpression extends Expression {

    <R, T> R accept(Visitor<R, T> visitor, T value);

    public interface Visitor<R, T> extends FunctionCall.Visitor<R, T>, Variable.Visitor<R, T>, StringLiteral.Visitor<R, T> {

        R visit(StringExpression node, T value);
    }
}
