package sodium.impl;

import sodium.action.Action;
import sodium.engine.CheckAction;

/**
 * @author Liu Zhikun
 */

public class CheckActionImpl implements CheckAction {
	private Action action;
	private String name;
	
	public CheckActionImpl(String name, Action action) {
		super();
		this.name = name;
		this.action = action;
	}

	public String getName() {
		return name;
	}
	
	public String getRole() {
		return action.getRole();
	}
	
}
