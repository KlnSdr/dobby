package dobby.annotations;

import java.lang.annotation.*;

/**
 * Annotation for GET requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Get {
    String value();
}
