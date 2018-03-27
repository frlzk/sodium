package sodium.page;


import net.sf.xmlform.util.I18NTexts;

/**
 * @author Liu Zhikun
 */

public class ComponentImpl implements Component {
	private String name;
	private String body;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String buildJsClass(BuildJsContext ctx) {
		return body;
	}

}
