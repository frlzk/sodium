package sodium.anchortype.common;

import sodium.action.Anchor;
import sodium.anchortype.Option;
import sodium.anchortype.Options;
import sodium.anchortype.PageAnchorType;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public class Create implements PageAnchorType {
	private static Option opts[]=new Option[]{
		Options.SOURCE,
		Options.SOURCE2,
		Options.CONSTSOURCE,
		Options.REFRESH,
		Options.CASCADE,
//		Options.ATTACH,
		Options.STYLE
	};
	public Option[] getOptions() {
		return opts;
	}
	private int order=800;
	public void postCreate(Page page, Anchor anchor) {
		if(anchor.getIcon()!=null)
			anchor.setIcon("create");
		AnchorTypeUtil.nullif(anchor,Options.REFRESH_NAME,anchor.getAttach());
		AnchorTypeUtil.nullif(anchor,Options.STYLE_NAME, "resusable",true);
		AnchorTypeUtil.nullif(anchor,Options.SOURCESCOPE_NAME,"any");
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
