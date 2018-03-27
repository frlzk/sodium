package sodium.category.impl;

import sodium.category.CategorizedName;

/**
 * @author Liu Zhikun
 */

public class CategorizedNameImpl implements CategorizedName {
	private String category,name;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
