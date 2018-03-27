package app.impl;

import java.util.HashSet;
import java.util.Set;

public class Role {
	private String id;
	private String name;
	private Set actions=new HashSet();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set getActions() {
		return actions;
	}
	public void setActions(Set actions) {
		this.actions = actions;
	}
}
