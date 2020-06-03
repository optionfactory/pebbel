package net.optionfactory.pebbel;

import net.optionfactory.pebbel.parsing.ast.Expression;
import net.optionfactory.pebbel.results.Result;

public interface Parser {

    Result<Expression> parse(String source, Class<?> expectedType);
}
