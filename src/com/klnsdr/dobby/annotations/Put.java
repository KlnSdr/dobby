package com.klnsdr.dobby.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for PUT requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Put {
    String value();
}
