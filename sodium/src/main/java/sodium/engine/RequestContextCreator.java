package sodium.engine;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import sodium.RequestContext;

/**
 * @author Liu Zhikun
 */

public interface RequestContextCreator {
	public RequestContext createRequestContext(HttpServletRequest httpServletRequest);
}
