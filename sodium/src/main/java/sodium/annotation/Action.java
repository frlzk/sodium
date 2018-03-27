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
public @interface Action {
	String name() default "";
	String desc() default "";
	String label() default "";
	String role() default "";
	String partners() default "";
	String leaders() default "";
//	String previous() default "";
	Anchor[] anchors() default {};
	
//	String source() default "";
//	String sourcetype() default "form";
//	String result() default "";
//	String printMethod() default "";
//	String textMethod() default "";
//	int max() default Integer.MAX_VALUE;
//	int min() default 0;
}