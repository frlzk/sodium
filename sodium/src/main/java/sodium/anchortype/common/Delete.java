package sodium.anchortype.common;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;
import sodium.anchortype.Options;

/**
 * @author Liu Zhikun
 */

public class Delete implements ActionAnchorType {
	private static Option opts[]=new Option[]{
//		Options.ATTACH,
		Options.SOURCE,
		Options.SOURCE2,
		Options.CONSTSOURCE,
		Options.REFRESH,
		Options.CASCADE,
		Options.CONFIRM,
		Options.STYLE
	};
	private int order=1100;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		if(anchor.getIcon()!=null)
			anchor.setIcon("delete");
		if(anchor.getOrder()==Anchor.BASE_ORDER)
			anchor.setOrder(order);
		AnchorTypeUtil.nullif(anchor,Options.REFRESH_NAME,AnchorTypeUtil.getSourceForm(anchor));
		I18NTexts confirm=anchor.getOptions().getI18NTexts(Options.CONFIRM_NAME);
		if(confirm==null||confirm.size()==0){
			String keyfield=(String)AnchorTypeUtil.getOptProperty(anchor, Options.STYLE_NAME,"keyfield");
			I18NText it=null;
			if(keyfield!=null)
				it=new I18NText("确定要删除${"+keyfield+"}吗?");
			else
				it=new I18NText("确定要删除吗?");
			I18NTexts its=new I18NTexts();
			its.put(it);
			anchor.getOptions().put(Options.CONFIRM_NAME,its);
		}
		AnchorTypeUtil.setDefaultAttach(action,anchor);
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
}
