package app.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.data.SourceInfo;
import net.sf.xmlform.data.Status;
import sodium.action.ActionContext;

public class SaveTemplate {
	static public void saveEntities(ActionContext context,Session session,String entityName,List entities,SaveCallback cb)throws ActionException{
		saveEntities(context,session,entityName,entities,cb,"id");
	}
	static public void saveEntities(ActionContext context,Session session,String entityName,List entities,SaveCallback cb,String idName)throws ActionException{
		if(entities==null)
			return;
		Iterator it=entities.iterator();
		while(it.hasNext()){
			Map obj=(Map)it.next();
			SourceInfo info=context.getSourceInfo(obj);
			saveEntity(context,session,entityName,obj,cb,idName);
		}
	}
	static public Map saveEntity(ActionContext context,Session session,String entityName,Map entity,SaveCallback cb)throws ActionException{
		return saveEntity(context,session,entityName,entity,cb,"id");
	}
	static public Map saveEntity(ActionContext context,Session session,String entityName,Map entity,SaveCallback cb,String idName)throws ActionException{
		SourceInfo info=context.getSourceInfo(entity);
		if(info==null)
			throw new ActionException("Not found entity info");
		Status st=info.getStatus();
		Serializable sid=(Serializable)entity.get(idName);
		if(st.equals(Status.NEW)||sid==null){
			Map dat=cb.beforeCreateEntity(context,session,entityName,entity);
			if(dat!=null)
				session.save(entityName, dat);
			return dat;
		}else if(st.equals(Status.MODIFIED)){
			Map old=null;
			if(sid!=null)
				old=(Map)session.get(entityName, sid);
			if(old==null){
				old=cb.beforeCreateEntity(context,session,entityName,entity);
				if(old!=null){
					session.save(entityName, old);
				}
			}else{
				old.putAll(cb.beforeModifyEntity(context,session,entityName,old,entity));
				session.update(entityName, old);
			}
			return old;
		}else if(st.equals(Status.REMOVED)){
			cb.beforeDeleteEntity(context,session,entityName,entity);
			session.delete(entityName, entity);
			return null;
		}
		return entity;
	}
}
