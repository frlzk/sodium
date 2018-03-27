package sodium.anchortype.common;

import sodium.action.Anchor;
import sodium.anchortype.Option;
import sodium.anchortype.Options;
import sodium.anchortype.PageAnchorType;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public class Link implements PageAnchorType {
	private static Option opts[]=new Option[]{
		Options.SOURCE,
		Options.SOURCE2,
		Options.CONSTSOURCE,
//		Options.ATTACH,
		Options.REFRESH,
		Options.CASCADE,
		Options.TRIGGER,
		Options.STYLE
		
	};
	private int order=4000;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Page page, Anchor anchor) {
		AnchorTypeUtil.setDefaultAttach(page,anchor);
		if(anchor.getIcon()!=null)
			anchor.setIcon("link");
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
