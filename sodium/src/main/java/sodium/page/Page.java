package sodium.page;

import java.util.List;

import net.sf.xmlform.util.I18NTexts;
import sodium.page.BuildJsContext;

/**
 * @author Liu Zhikun
 */


public interface Page extends JsClass {
	public String getName();
	public I18NTexts getTitle();
	public List getAnchors();
	public List getSelfAnchors();
	public Object getPageObject();
	public String buildJsClass(BuildJsContext ctx);
}
