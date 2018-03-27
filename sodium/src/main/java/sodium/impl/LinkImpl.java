package sodium.impl;

import sodium.action.Anchor;
import sodium.anchoropt.ObjectOption;
import sodium.engine.Link;

/**
 * @author Liu Zhikun
 */

public class LinkImpl implements Link {
	private Anchor anchor;
	private String page;
	private String label;
	private boolean perm;
	private String from;
	private String action;
	public LinkImpl(String from,Anchor an,String label,String page,String action,boolean perm){
		anchor=an;
		this.page=page;
		this.perm=perm;
		this.label=label;
		this.from=from;
		this.action=action;
	}
	public String getType(){
		return anchor.getType();
	}
	public String getIcon(){
		return anchor.getIcon();
	}
	public String getAttach(){
		return anchor.getAttach();
	}
	public boolean hasPerm(){
		return perm;
	}
	public String getLabel() {
		return label;
	}
	public String getPage() {
		return page;
	}
	public String getAction() {
		return action;
	}
	public int getOrder() {
		return anchor.getOrder();
	}
	public String getFrom(){
		return from;
	}
	public ObjectOption getOptions(){
		return anchor.getOptions();
	}
	
}
