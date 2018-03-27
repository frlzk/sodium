package sodium.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Liu Zhikun
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormAction {
	String source() default "";
	String sourcetype() default "form";
	String result() default "";
	int max() default Integer.MAX_VALUE;
	int min() default 1;
	String printMethod() default "";
	String textMethod() default "";
}
