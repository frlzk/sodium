package sodium.action;

import sodium.RequestContext;

/**
 * @author Liu Zhikun
 */

public interface ReferenceContext {
//	public Object getParent();
	public String getProperty(String name);
	public String getArgument(String name);
	public RequestContext getRequestContext();
}
