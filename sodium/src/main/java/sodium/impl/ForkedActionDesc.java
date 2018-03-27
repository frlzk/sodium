package sodium.impl;

import sodium.action.Action;
import sodium.action.impl.ActionImpl;
import sodium.category.CategorizedAction;
import sodium.category.CategoryUtil;
import sodium.category.ForkedAction;
import sodium.engine.Engine;

/**
 * @author Liu Zhikun
 */

public class ForkedActionDesc implements ActionDesc {
	private String name,parts[],role;
	private String label;
	public ForkedActionDesc(Engine eng,Action action,ForkedAction fa) {
		this.role=action.getRole();
		name=CategoryUtil.createCategoryName(action.getName(), fa.getCategory());
		label=fa.getLabel();
		if(action.getPartners()!=null){
			String parts[]=action.getPartners();
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<parts.length;i++){
				ActionImpl act=(ActionImpl)eng.getAction(parts[i]);
				if(act==null){
					continue;
				}
				if(sb.length()>0){
					sb.append(",");
				}
				if(act.getActionInstance() instanceof CategorizedAction){
					sb.append(CategoryUtil.createCategoryName(act.getName(), fa.getCategory()));
				}else{
					sb.append(parts[i]);
				}
			}
		}
	}


	public String getRole() {
		return role;
	}

	
	public String getName() {
		return name;
	}

	
	public String getLabel() {
		return label;
	}

	
	public String[] getPartners() {
		return parts;
	}
	
}
