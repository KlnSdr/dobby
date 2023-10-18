package dobby.annotations;

import java.lang.annotation.*;

/**
 * Annotation for PUT requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Put {
    String value();
}
