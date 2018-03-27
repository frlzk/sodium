package sodium.impl;

import sodium.action.Action;

/**
 * @author Liu Zhikun
 */

public class ActionActionDesc implements ActionDesc {
	private String label;
	private Action action;
	
	public ActionActionDesc(Action action,String label) {
		super();
		this.label=label;
		this.action = action;
	}


	public String getRole() {
		return action.getRole();
	}

	
	public String getName() {
		return action.getName();
	}

	
	public String getLabel() {
		return label;
	}

	
	public String[] getPartners() {
		return action.getPartners();
	}

}
