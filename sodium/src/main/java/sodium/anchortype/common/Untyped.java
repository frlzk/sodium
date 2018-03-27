package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;
import sodium.anchortype.Options;
import sodium.anchortype.PageAnchorType;
import sodium.page.Page;

public class Untyped implements PageAnchorType,ActionAnchorType{
	private static Option opts[]=new Option[]{
			Options.SOURCE,
			Options.SOURCESCOPE,
			Options.SOURCE2,
			Options.CONSTSOURCE,
			Options.MARK,
			Options.RESULT,
			Options.STYLE,
			Options.REFRESH,
			Options.CASCADE,
			Options.CONFIRM,
			Options.TRIGGER,
			Options.ENABLE,
			Options.VFOLLOWE
		};
	private int order=6000;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		AnchorTypeUtil.setDefaultAttach(action,anchor);
		if(anchor.getOrder()==Anchor.BASE_ORDER)
			anchor.setOrder(6000);
	}
	public void postCreate(Page page, Anchor anchor) {
		AnchorTypeUtil.setDefaultAttach(page,anchor);
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
