package sodium.annotation;


import java.lang.annotation.ElementType;  
import java.lang.annotation.Retention;  
import java.lang.annotation.RetentionPolicy;  
import java.lang.annotation.Target;

/**
 * @author Liu Zhikun
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Anchor {
	String icon() default "";
	String label() default "";
	String page();
	String attach() default "";
	String type() default "";
	int order()  default sodium.action.Anchor.BASE_ORDER;
	Opt[] options() default {};
}
