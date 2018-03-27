package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;
import sodium.anchortype.Options;

/**
 * @author Liu Zhikun
 */

public class Save implements ActionAnchorType {
	private static Option opts[]=new Option[]{
//		Options.ATTACH,
		Options.SOURCE,
		Options.SOURCE2,
		Options.CONSTSOURCE,
		Options.RESULT,
		Options.REFRESH,
		Options.CASCADE,
		Options.STYLE
	};
	private int order=100;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		if(anchor.getIcon()!=null)
			anchor.setIcon("save");
		if(action.getSourceForm()!=null){
			AnchorTypeUtil.nullif(anchor,Options.SOURCE_NAME,Options.SOURCE.parseOpt(action.getSourceForm()));
		}
		AnchorTypeUtil.nullif(anchor,Options.RESULT_NAME,action.getResultForm());
		AnchorTypeUtil.nullif(anchor,Options.TRIGGER_NAME,Anchor.AFTER_LAST_FIELD_TRIGGER);
		AnchorTypeUtil.nullif(anchor,Options.REFRESH_NAME,"parent");
		AnchorTypeUtil.setDefaultAttach(action,anchor);
		
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
