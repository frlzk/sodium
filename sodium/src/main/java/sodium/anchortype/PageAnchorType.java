package sodium.anchortype;

import sodium.action.Anchor;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public interface PageAnchorType {
	public Option[] getOptions();
	public void postCreate(Page page,Anchor anchor);
}
