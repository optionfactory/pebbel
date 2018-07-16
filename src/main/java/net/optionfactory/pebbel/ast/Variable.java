package net.optionfactory.pebbel.ast;

/**
 * An AST node referencing a variable. Variables are always in uppercase. (e.g.:
 * {@code VAR1})
 */
public class Variable implements BooleanExpression, StringExpression, NumberExpression {

    public String name;
    public Source source;

    public static Variable of(String name, Source source) {
        final Variable var = new Variable();
        var.name = name;
        var.source = source;
        return var;
    }

    public <O, I> O accept(Variable.Visitor<O, I> visitor, I value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(BooleanExpression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <O, I> O accept(StringExpression.Visitor<O, I> visitor, I value) {
        return visitor.visit(this, value);
    }

    @Override
    public <R, T> R accept(NumberExpression.Visitor<R, T> visitor, T value) {
        return visitor.visit(this, value);
    }

    @Override
    public <O, I> O accept(Expression.Visitor<O, I> visitor, I value) {
        return visitor.visit(this, value);
    }

    @Override
    public Source source() {
        return source;
    }

    public interface Visitor<O, I> {

        O visit(Variable node, I value);
    }
}
