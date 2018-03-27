package sodium.category.impl;

import sodium.RequestContext;
import sodium.category.CategoryContext;
import sodium.category.ForkContext;

/**
 * @author Liu Zhikun
 */

public class CatagoryContextImpl implements CategoryContext,ForkContext {
	private RequestContext pbc;
	private String group;
	public CatagoryContextImpl(RequestContext pbc,String group){
		this.pbc=pbc;
		this.group = group;
	}

	public String getCategory() {
		return group;
	}

	public RequestContext getRequestContext() {
		return pbc;
	}
	
}
