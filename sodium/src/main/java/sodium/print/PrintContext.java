package sodium.print;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sodium.RequestContext;

public interface PrintContext {
	public String getPrinter();
	public RequestContext getRequestContext();
	public ServletContext getServletContext();
	public HttpServletRequest getServletRequest();
	public HttpServletResponse getServletResponse();
}
