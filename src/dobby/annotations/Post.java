package dobby.annotations;

import java.lang.annotation.*;

/**
 * Annotation for POST requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {
    String value();
}
