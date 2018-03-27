package sodium.engine;

import sodium.RequestContext;

public interface SessionAttributes {
	public String[] getNames(RequestContext ctx);
	public Object getValue(RequestContext ctx,String name);
}
