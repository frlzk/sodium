package sodium.page;

/**
 * @author Liu Zhikun
 */

public interface JsClass {
	public String getName();
	public String buildJsClass(BuildJsContext ctx);
}
