package sodium.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sodium.engine.ActionNode;
import sodium.engine.Configuration;

/**
 * @author Liu Zhikun
 */

public class ActionNodeImpl implements ActionNode {
	private String name;
	private String label;
	private String partners[]=new String[0];
	private String role;
	private List items=new ArrayList();
	private int order=0;
	
	public String getName(){
		return name;
	}
	public String getLabel() {
		return label;
	}
	public String getRole() {
		return role;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String[] getPartners() {
		return partners;
	}
//	public void setPartners(String partners[]) {
//		this.partners = partners;
//	}
	public void setName(String name) {
		this.name = name;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public void sort(){
		for(int i=0;i<items.size();i++){
			ActionNodeImpl impl=(ActionNodeImpl)items.get(i);
			impl.sort();
		}
		
		Comparator c=new Comparator(){
			public int compare(Object o1, Object o2) {
				ActionNodeImpl a1=(ActionNodeImpl)o1;
				ActionNodeImpl a2=(ActionNodeImpl)o2;
				return a1.order-a2.order;
			}
		};
		items.sort(c);
	}
	public List getChildActions(){
		return items;
	}
	public void addItem(Configuration conf,Map extParts,ActionDesc act){
		if(act.getLabel()==null)
			return;
		String label=act.getLabel();
		if(label.startsWith("/"))
			label=label.substring(1);
		addItem(conf,extParts,act,label.split("/"),0,"/");
	}
	private void addItem(Configuration conf,Map extParts,ActionDesc act,String labels[],int idx,String fullName){
		if(idx>=labels.length)
			return;
		Iterator it=items.iterator();
		while(it.hasNext()){
			ActionNodeImpl item=(ActionNodeImpl)it.next();
			if(item.label.equals(labels[idx])){
				item.addItem(conf,extParts,act,labels, idx+1,item.name);
				return ;
			}
		}
		ActionNodeImpl item=new ActionNodeImpl();
		item.label=labels[idx];
		item.role=act.getRole();
		fullName="/".equals(fullName)?item.label:fullName+"/"+item.label;
		if(idx==labels.length-1){
			item.name=act.getName();
		}else{
			item.name=fullName;
		}
		if(item.order==0){
			item.order=conf.getGroupOrder(fullName);
		}
		List parts=(List)extParts.remove(item.name);
		if(parts!=null)
			item.partners=(String[])parts.toArray(new String[parts.size()]);
		items.add(item);
		item.addItem(conf,extParts,act,labels, idx+1,item.name);
	}
}
