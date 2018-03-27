package sodium.engine;

/**
 * @author Liu Zhikun
 */

public interface BeanCreator {
	public Object createBean(String className)throws Exception;
}
