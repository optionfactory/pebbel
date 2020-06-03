package net.optionfactory.pebbel.parsing.ast;

/**
 * A function call. e.g: {@code true()} or {@code day_of_the_month()}. Functions
 * are always lowercase.
 */
public class FunctionCall implements BooleanExpression, StringExpression, NumberExpression {

    public String function;
    public Expression[] arguments;
    public Source source;

    public static FunctionCall of(String function, Expression[] arguments, Source source) {
        final FunctionCall call = new FunctionCall();
        call.function = function;
        call.arguments = arguments;
        call.source = source;
        return call;
    }

    public <R, T> R accept(FunctionCall.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(NumberExpression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(StringExpression.Visitor<R, T> visitor, T value) {
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

        R visit(FunctionCall node, T value);
    }
}
