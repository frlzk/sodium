package sodium.anchortype;

import sodium.action.Action;
import sodium.action.Anchor;

/**
 * @author Liu Zhikun
 */

public interface ActionAnchorType {
	public Option[] getOptions();
	public void postCreate(Action action,Anchor anchor);
}
