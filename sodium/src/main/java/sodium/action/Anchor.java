package sodium.action;

import net.sf.xmlform.util.I18NTexts;
import sodium.anchoropt.ObjectOption;

/**
 * @author Liu Zhikun
 */

public class Anchor implements Cloneable {
	public final static String TYPE="type";
	public final static String OPEN_PAGE_TRIGGER="pageopen",RECORD_SELECT_TRIGGER="recordselect",
					RECORD_DBLCLICK_TRIGGER="recorddblclick",AFTER_LAST_FIELD_TRIGGER="afterlastfield";
	public final static int BASE_ORDER=2222;
	private String icon;
	private I18NTexts label=new I18NTexts();
	private String type;
	private String page;
	private String action;
	private String attach;
	private String previous[];
	private int order=BASE_ORDER;
	private boolean self=false;
	/*
	 * formname   include all
	 * formname,f1,f2  only include f1 f2
	 * formname,-,f1,f2 excluse f1,f2
	 */
	/*
	 * maxresults: result max rows
	 * showerror:true|false
	 * keyfield:formname!filedname
	 * remember:true|false
	 */
	private ObjectOption options=new ObjectOption();
	public void setType(String type) {
		this.type=type;
	}
	public String getType() {
		return type;
	}
	public ObjectOption getOptions() {
		return options;
	}
	public I18NTexts getLabel() {
		return label;
	}
	public void setLabel(I18NTexts label) {
		this.label = label;
	}
	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
	public String[] getPrevious() {
		return previous;
	}
	public void setPrevious(String previous[]) {
		this.previous = previous;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
//	public boolean isSelf() {
//		return self;
//	}
//	public void setSelf(boolean self) {
//		this.self = self;
//	}
	public Object clone() throws CloneNotSupportedException {
		Anchor n=(Anchor)super.clone();
		Anchor old=this;
		n.self=self;
		n.options.putAll(old.options);
		n.setIcon(old.getIcon());
		n.setLabel(old.getLabel());
		n.setAttach(old.attach);
		n.setOrder(old.getOrder());
		n.setAction(old.getAction());
		n.setPage(old.getPage());
		return n;
	}
}
