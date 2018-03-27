package sodium.category;

import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.action.Action;
import sodium.action.Anchor;

/**
 * @author Liu Zhikun
 */

public interface CategorizedAction {
	/* call when get menu */
	public ForkedAction[] forkAction(ForkContext ctx,Action action);
	/* call when get page link */
	public XMLForm adaptForm(CategoryContext ctx,Action action,XMLForm form);
	public FormLayout adaptLayout(CategoryContext ctx,Action action,FormLayout layout);
	public AdaptedAnchor adaptAnchor(CategoryContext ctx,Action action,Anchor an);
}
