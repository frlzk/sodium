package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchoropt.ArrayOption;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;

/**
 * @author Liu Zhikun
 */

public class ResetForm implements ActionAnchorType {
	private static Option opts[]=new Option[]{
		new Option("forms",ArrayOption.class)
	};
	private int order=110;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		AnchorTypeUtil.setDefaultAttach(action,anchor);
		if(anchor.getIcon()!=null)
			anchor.setIcon("resetform");
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
