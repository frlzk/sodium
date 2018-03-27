package sodium.annotation;

import java.lang.annotation.ElementType;  
import java.lang.annotation.RetentionPolicy;  
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * @author Liu Zhikun
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ActionGroup {
	String name();
	String label() default "";
	String previous() default "";
}
