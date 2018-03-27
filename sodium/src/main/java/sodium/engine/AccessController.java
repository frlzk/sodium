package sodium.engine;

import sodium.RequestContext;

/**
 * @author Liu Zhikun
 */

public interface AccessController {
	public Permission checkAccess(RequestContext ctx,CheckAction action);
}
