package sodium.action.impl;

import net.sf.xmlform.form.Argument;
import net.sf.xmlform.form.Reference;
import sodium.RequestContext;
import sodium.action.ReferenceContext;

/**
 * @author Liu Zhikun
 */

public class ReferenceContextImpl implements ReferenceContext {
	private RequestContext requestContext;
	private Reference ref;
	private Object parent;
	public ReferenceContextImpl(RequestContext requestContext,Reference ref){
		this.requestContext=requestContext;
		this.ref=ref;
	}
	
	public RequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public String getProperty(String name) {
		return (String)ref.getProperties().get(name);
	}

	public String getArgument(String name) {
		Argument a=(Argument)ref.getArguments().get(name);
		if(a==null)
			return null;
		return a.getValue();
	}
	
}
