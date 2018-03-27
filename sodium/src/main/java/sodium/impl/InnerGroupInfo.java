package sodium.impl;

public class InnerGroupInfo {
	private String name,label,previous;
	
	public InnerGroupInfo(String n,String l,String pre){
		name=n;
		label=l;
		previous=pre;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}
	
}
