package sodium.engine;

import sodium.anchoropt.ObjectOption;

/**
 * @author Liu Zhikun
 */

public interface Link {
	public final static String FROM_ACTION="action",FROM_PAGE="page";
	public String getFrom();
	public String getType();
	public String getLabel();
	public String getIcon();
	public String getPage();
	public String getAttach();
	public int getOrder();
	public String getAction();
	public ObjectOption getOptions();
	public boolean hasPerm();
//	public String getRefresh();
//	public String getConfirm();
//	public String getSource();
//	public String getSource2();
//	public String getResult();
//	public String getTrigger();
//	public String getMark();
//	public String getStyle();
//	public Map getProperties();
}
