package sodium.page;

import sodium.RequestContext;
import sodium.engine.Engine;

import net.sf.xmlform.XMLFormPort;

/**
 * @author Liu Zhikun
 */


public interface BuildJsContext {
	public String getCategory();
	public String getName();
	public Engine getEngine();
//	public XMLFormPort getFormPort();
//	public XMLFormLayoutPort getLayoutPort();
	public RequestContext getRequestContext() ;
}
