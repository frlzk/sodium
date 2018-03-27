package sodium.page.jsparser;

/**
 * @author Liu Zhikun
 */

public interface JsCompiler {
	public String getPageFileType();
	public String getComponentFileType();
	public String compile(String script);
}
