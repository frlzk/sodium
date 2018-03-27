package sodium.page.jsparser;

/**
 * @author Liu Zhikun
 */

public class Js3 implements JsCompiler {
	public String getPageFileType(){
		return ".page.js";
	}
	public String getComponentFileType(){
		return ".comp.js";
	}

	public String compile(String script) {
		return script;
	}

}
