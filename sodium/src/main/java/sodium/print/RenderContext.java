package sodium.print;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Liu Zhikun
 */

public interface RenderContext {
	public String getTitle();
	public String getFormat();
	public ServletContext getServletContext();
	public HttpServletRequest getServletRequest();
//	public HttpServletResponse getServletResponse();
}
