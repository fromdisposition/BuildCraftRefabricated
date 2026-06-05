package buildcraft.api.transport.pipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PipeEventHandler {

    PipeEventPriority priority() default PipeEventPriority.NORMAL;

    boolean receiveCancelled() default false;
}
