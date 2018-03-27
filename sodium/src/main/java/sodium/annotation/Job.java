package sodium.annotation;

/**
 * @author Liu Zhikun
 */

public @interface Job {
	String cron() default "";
}
