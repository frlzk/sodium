package app.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;

import app.hibernate.SaveCallback;
import app.util.DbUtil;
import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;

abstract public class BaseAction implements SaveCallback {
	@Autowired
	private HibernateTemplate hiberanteTemplate;
	protected HibernateTemplate getHiberanteTemplate(){
		return hiberanteTemplate;
	}
	protected List query(String sql,Object ...v){
		int size=1;
		if(v.length>0)
			size=v.length/2;
		List names=new ArrayList(size);
		List values=new ArrayList(size);
		for(int i=0;i<v.length;i+=2){
			names.add(v[i]);
			values.add(v[i+1]);
		}
		return this.getHiberanteTemplate().findByNamedParam(sql, (String[])names.toArray(new String[names.size()]),(Object[])values.toArray(new Object[values.size()]));
	}
	protected void setFieldValue(List data,String name,Object value){
		if(data==null)
			return;
		Iterator it=data.iterator();
		while(it.hasNext()){
			Map obj=(Map)it.next();
			obj.put(name, value);
		}
	}
	private List createSimpleParameters(Object... data){
		List result=new ArrayList();
		if(data==null||data.length==0)
			return result;
		if(data.length%2!=0){
			throw new IllegalArgumentException("Data length must pair");
		}
		Map param=new HashMap();
		for(int i=0;i<data.length;i+=2){
			param.put(data[i], data[i+1]);
		}
		result.add(param);
		return result;
	}
	protected Object getSimpleParameter(List data,String name){
		if(data==null||data.size()==0)
			return null;
		Map map=(Map)data.get(0);
		return map.get(name);
	}
	protected List evictList(Session s,List list){
		if(list==null)
			return null;
		for(int i=0;i<list.size();i++){
			Map m=(Map)list.get(i);
			s.evict(m);
		}
		return list;
	}
	public Map beforeCreateEntity(ActionContext context,Session session,String entityName,Map entity) throws ActionException{
		return entity;
	}
	public Map beforeModifyEntity(ActionContext context,Session session,String entityName,Map oldEntity,Map newEntity) throws ActionException{
		return newEntity;
	}
	public void beforeDeleteEntity(ActionContext context,Session session,String entityName,Map entity) throws ActionException{
		
	}
	abstract protected List submitForm(ActionContext context,List data)throws ActionException;
}
