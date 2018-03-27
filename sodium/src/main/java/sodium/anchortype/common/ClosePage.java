package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;

/**
 * @author Liu Zhikun
 */

public class ClosePage implements ActionAnchorType {
	private static Option opts[]=new Option[]{
		};
	private int order=9000;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		AnchorTypeUtil.setDefaultAttach(action,anchor);
		if(anchor.getIcon()!=null)
			anchor.setIcon("closepage");
		if(anchor.getOrder()==Anchor.BASE_ORDER)
			anchor.setOrder(order);
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
}
