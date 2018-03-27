package sodium.page;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import sodium.RequestContext;
import sodium.engine.SessionAttributes;

/**
 * @author Liu Zhikun
 */

public class PageContainerContextImpl implements PageContainerContext {
	private ServletContext servletContext;
	private HttpServletRequest httpRequest;
	private String window;
	private String buildVersion;
	private SessionAttributes sessionAttributes;
	private RequestContext requestContext;
	private String theme;
	public Locale getLocale(){
		return requestContext.getLocale();
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public RequestContext getRequestContext() {
		return requestContext;
	}
	public void setRequestContext(RequestContext requestContext) {
		this.requestContext = requestContext;
	}
	public String getWindow() {
		return window;
	}
	public void setWindow(String window) {
		this.window = window;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public HttpServletRequest getServletRequest() {
		return httpRequest;
	}
	public void setServletRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	public String getBuildVersion() {
		return buildVersion;
	}
	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}
	public SessionAttributes getSessionAttributes() {
		return sessionAttributes;
	}
	public void setSessionAttributes(SessionAttributes sessionAttributes) {
		this.sessionAttributes = sessionAttributes;
	}
	public String[] getSessionAttributeNames(){
		return sessionAttributes.getNames(requestContext);
	}
	public Object getSessionAttributeValue(String name){
		return sessionAttributes.getValue(requestContext, name);
	}
}
