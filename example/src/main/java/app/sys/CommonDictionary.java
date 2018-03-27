package app.sys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.util.LRUMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import sodium.action.ReferenceContext;
import net.sf.xmlform.action.ActionException;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;

@Component
@Transactional(rollbackFor=Exception.class)
public class CommonDictionary extends BaseAction {
	private Map groups=new LRUMap(1000);
	private Map items=new LRUMap(10000);

	public List submitForm(ActionContext ctx,final List data)throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				if(data.size()==0)
					throw new IllegalArgumentException("Unknow common dictionary type.");
				Map params=(Map)data.get(0);
				String type=(String)params.get("type");
				List res=new ArrayList();
				Query q=session.createQuery("from SysCommonDictionaryItem where group=:type and stop='0' order by order");
				q.setString("type", type);
				Iterator it=q.list().iterator();
				
				while(it.hasNext()){
					Map dic=(Map)it.next();
					dic.put("key", dic.get("key"));
					dic.put("text", dic.get("text"));
					dic.put("fullText", dic.get("fullText"));
					dic.put("spell", dic.get("spell"));
					res.add(dic);
				}
				session.clear();
				return res;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

	public String getDisplayText(final ReferenceContext refCtx,Object bean,final Object key) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				if(key==null)
					return null;items.clear();
				String keyStr=key.toString();
				Object keyValue=keyStr;
				String value=(String)items.get(keyStr);
				if(value!=null)
					return value;
				String sql="from SysCommonDictionaryItem where group=:type and key=:key";
				if(keyStr.indexOf(",")>0){
					keyValue=Arrays.asList(keyStr.split(","));
					sql="from SysCommonDictionaryItem where group=:type and key in (:key)";
				}
				Query q=session.createQuery(sql);
				q.setString("type", refCtx.getArgument("type"));
				if(keyValue instanceof String)
					q.setString("key", (String)keyValue);
				else
					q.setParameterList("key", (List)keyValue);
				List list=q.list();
				if(list.size()>0){
					if(keyValue instanceof String){
						Map item=(Map)list.get(0);
						value=(String)item.get("fullText");
					}else{
						StringBuilder sb=new StringBuilder();
						Iterator it=list.iterator();
						while(it.hasNext()){
							if(sb.length()>0)
								sb.append(",");
							Map item=(Map)it.next();
							sb.append((String)item.get("fullText"));
						}
						value=sb.toString();
					}
					items.put(keyStr, value);
					return value;
				}
				value=key.toString();
				items.put(keyStr, value);
				return value;
			}
		};
		return (String)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
