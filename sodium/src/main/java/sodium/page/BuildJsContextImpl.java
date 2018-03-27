package sodium.page;

import net.sf.xmlform.XMLFormPort;
import net.sf.xmlform.formlayout.XMLFormLayoutPort;
import sodium.RequestContext;
import sodium.engine.Engine;

/**
 * @author Liu Zhikun
 */

public class BuildJsContextImpl implements BuildJsContext {
	private String pageName;
	private String category;
	private Engine engine;
	private XMLFormPort formPort;
	private XMLFormLayoutPort layoutPort;
	private RequestContext requestContxt;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return pageName;
	}
	public void setName(String pageName) {
		this.pageName = pageName;
	}
	public Engine getEngine() {
		return engine;
	}
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	public XMLFormPort getFormPort() {
		return formPort;
	}
	public void setFormPort(XMLFormPort formPort) {
		this.formPort = formPort;
	}
	public XMLFormLayoutPort getLayoutPort() {
		return layoutPort;
	}
	public void setLayoutPort(XMLFormLayoutPort layoutPort) {
		this.layoutPort = layoutPort;
	}
	public RequestContext getRequestContext() {
		return requestContxt;
	}
	public void setRequestContext(RequestContext requestContxt) {
		this.requestContxt = requestContxt;
	}
}
