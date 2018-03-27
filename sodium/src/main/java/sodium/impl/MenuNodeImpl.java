package sodium.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sodium.action.impl.OrderInfo;
import sodium.engine.Configuration;
import sodium.engine.Link;
import sodium.engine.MenuNode;
import sodium.util.Util;

/**
 * @author Liu Zhikun
 */

public class MenuNodeImpl implements MenuNode{
	String label;
	Link link;
	int order=0;
	List items = new ArrayList();
	
	public String getLabel(){
		return label;
	}
	public String getPage(){
		return link.getPage();
	}
	public List getChildMenus(){
		return items;
	}
	public void setLabel(String label){
		this.label=label;
	}
	public void addItem(Configuration conf, Link ipa) {
		if (ipa.getLabel() == null)
			return;
		String label=ipa.getLabel();
		if(label.startsWith("/"))
			label=label.substring(1);
		addItem(conf, ipa, label.split("/"), 0);
	}

	private void addItem(Configuration conf, Link ipa, String labels[],
			int idx) {
		if (idx >= labels.length)
			return;
		String label = labels[idx];
		Iterator it = items.iterator();
		while (it.hasNext()) {
			MenuNodeImpl item = (MenuNodeImpl) it.next();
			if (item.label.equals(label)) {
				item.addItem(conf, ipa, labels, idx + 1);
				return;
			}
		}
		MenuNodeImpl item = new MenuNodeImpl();
		item.label = label;
		item.link = ipa;
		items.add(item);
		
		if(idx<labels.length-1){
			if(item.order==0){
				StringBuilder sb=new StringBuilder();
				for(int i=0;i<=idx;i++){
					if(sb.length()>0)
						sb.append("/");
					sb.append(labels[i]);
				}
				item.order=conf.getGroupOrder(sb.toString());
			}
		}else{
			item.order=ipa.getOrder();
		}
		
		item.addItem(conf, ipa, labels, idx + 1);
	}
}
