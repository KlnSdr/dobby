package dobby.annotations;

import java.lang.annotation.*;

/**
 * Annotation for PATCH requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Patch {
    String value();
}
