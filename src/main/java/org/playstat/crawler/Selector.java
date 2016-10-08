package org.playstat.crawler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Selector {
    String NOVALUE = "[unassigned]";
    String value();
    String attr() default "";
    String format() default NOVALUE;
    String locale() default NOVALUE;
    String defValue() default NOVALUE;
}
