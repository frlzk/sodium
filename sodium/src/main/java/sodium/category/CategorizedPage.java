package sodium.category;

import sodium.action.Anchor;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public interface CategorizedPage {
	/* call when get menu */
	public ForkedAnchor[] forkAnchor(ForkContext ctx,Page page,Anchor an);
	/* call when get page link */
	public AdaptedPage adaptPage(CategoryContext ctx,Page page);
	public AdaptedAnchor adaptAnchor(CategoryContext ctx,Page page,Anchor an);
}

