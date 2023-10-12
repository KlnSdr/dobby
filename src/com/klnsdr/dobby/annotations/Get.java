package com.klnsdr.dobby.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for GET requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Get {
    String value();
}
