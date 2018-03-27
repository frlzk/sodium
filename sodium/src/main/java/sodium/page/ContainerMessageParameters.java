package sodium.page;


import net.sf.xmlform.web.RequestMessageParameters;

/**
 * @author Liu Zhikun
 */

public class ContainerMessageParameters extends RequestMessageParameters{
	final public static String KEY_WINDOW="window";
	final public static String KEY_BUILD_VERSION="buildVersion";
	private PageContainerContext context;
	public ContainerMessageParameters(PageContainerContext context) {
		super(context.getServletContext(), context.getServletRequest());
		this.context=context;
	}
	public String getParameter(String key) {
		if(KEY_WINDOW.equals(key))
			return context.getWindow();
		if(KEY_BUILD_VERSION.equals(key))
			return context.getBuildVersion();
		if(KEY_THEME.equals(key))
			return context.getTheme();
		Object v=context.getSessionAttributeValue(key);
		if(v!=null)
			return this.objectToString(v);
		return super.getParameter(key);
	}
	
}
