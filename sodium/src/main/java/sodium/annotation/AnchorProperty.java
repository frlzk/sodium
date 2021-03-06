package sodium.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Liu Zhikun
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface AnchorProperty {
	String name() ;
	String value();
}
