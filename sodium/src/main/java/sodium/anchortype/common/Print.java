package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchoropt.ArrayOption;
import sodium.anchortype.Option;
import sodium.anchortype.Options;

/**
 * @author Liu Zhikun
 */

public class Print extends Query {
	private static Option opts[];
	static{
		Option ops2[]=new Option[]{
				new Option("printers",ArrayOption.class),
				new Option("formats",ArrayOption.class)
			};
		Query q=new Query();
		Option ops1[]=q.getOptions();
		opts=new Option[ops1.length+ops2.length];
		System.arraycopy(ops1, 0, opts, 0, ops1.length);
		System.arraycopy(ops2, 0, opts, ops1.length, ops2.length);
	}
	private int order=150;
	public Option[] getOptions() {
		return opts;
	}
	public void postCreate(Action action, Anchor anchor) {
		int order=anchor.getOrder();
		super.postCreate(action,anchor);
		AnchorTypeUtil.setDefaultAttach(action,anchor);
		if(order==Anchor.BASE_ORDER)
			anchor.setOrder(order);
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
}
