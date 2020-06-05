package net.optionfactory.pebbel.compiled;

import net.optionfactory.pebbel.parsing.ast.Source;
import net.optionfactory.pebbel.parsing.ast.StringLiteral;

public class Asd {

    public static String a(){
        return ";";
    }

    public String foo(Long l) {
        Source src = null;
        try {
            src = Source.of(1231,1232,1233,1234);
            return a();
        } catch (Throwable ex) {
            throw new ExecutionException(ex.getMessage(), src, ex);
        }
    }
}
