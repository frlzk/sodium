package sodium.page;

/**
 * @author Liu Zhikun
 */


public interface PageContainerRenderer {
	public String getDefaultTheme();
	public String createHtml(PageContainerContext ctx,String initScript);
}
