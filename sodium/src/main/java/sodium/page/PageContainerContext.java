package sodium.page;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import sodium.RequestContext;
import sodium.engine.SessionAttributes;

/**
 * @author Liu Zhikun
 */

public interface PageContainerContext {
	public Locale getLocale();
	public String getTheme();
	public String getWindow();
	public ServletContext getServletContext();
	public HttpServletRequest getServletRequest();
	public String getBuildVersion();
	public String[] getSessionAttributeNames();
	public Object getSessionAttributeValue(String name);
}
