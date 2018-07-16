package net.optionfactory.pebbel.loading;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BindingDoc {

    String value();
}
