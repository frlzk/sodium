package sodium.anchortype.common;

import sodium.action.Anchor;
import sodium.anchortype.Option;
import sodium.anchortype.Options;
import sodium.anchortype.PageAnchorType;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public class View implements PageAnchorType {
	private static Option opts[]=new Option[]{
//		Options.ATTACH,
		Options.SOURCE,
		Options.SOURCE2,
		Options.CONSTSOURCE,
		Options.REFRESH,
		Options.CASCADE,
		Options.TRIGGER,
		Options.STYLE
	};
	private int order=900;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Page page, Anchor anchor) {
		if(anchor.getIcon()!=null)
			anchor.setIcon("view");
		AnchorTypeUtil.nullif(anchor,Options.REFRESH_NAME,AnchorTypeUtil.getSourceForm(anchor));
		AnchorTypeUtil.nullif(anchor,Options.TRIGGER_NAME,Anchor.RECORD_DBLCLICK_TRIGGER);
		AnchorTypeUtil.nullif(anchor,Options.STYLE_NAME, "resusable",true);
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
