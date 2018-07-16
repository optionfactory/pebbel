package net.optionfactory.pebbel.ast;

/**
 * A string literal. Can be quoted with either single or double quotes. (e.g:
 * {@code "LITERAL"} or {@code 'LITERAL'})
 */
public class StringLiteral implements StringExpression {

    public String literal;
    public Source source;

    public static StringLiteral of(String literal, Source source) {
        final StringLiteral sl = new StringLiteral();
        sl.literal = literal;
        sl.source = source;
        return sl;
    }

    public <O, I> O accept(StringLiteral.Visitor<O, I> visitor, I value) {
        return visitor.visit(this, value);
    }

    @Override
    public <O, I> O accept(StringExpression.Visitor<O, I> visitor, I value) {
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

        O visit(StringLiteral node, I value);
    }
}
