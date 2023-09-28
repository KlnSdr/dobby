package dobby.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for POST requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Post {
    String value();
}
