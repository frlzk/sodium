package app.impl;

import sodium.RequestContext;
import sodium.engine.AccessController;
import sodium.engine.CheckAction;
import sodium.engine.Permission;

public class AccessControllerImpl implements AccessController {

	public Permission checkAccess(RequestContext ctx, CheckAction action) {
		return Permission.GRANTED;
	}

}
