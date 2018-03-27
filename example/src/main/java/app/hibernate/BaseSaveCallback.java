package app.hibernate;

import java.util.Map;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import net.sf.xmlform.action.ActionException;
import sodium.action.ActionContext;

public class BaseSaveCallback implements SaveCallback{
	public Map beforeCreateEntity(ActionContext context,Session session,String entityName,Map entity) throws ActionException{
		return entity;
	}
	public Map beforeModifyEntity(ActionContext context,Session session,String entityName,Map oldEntity,Map newEntity) throws ActionException{
		return newEntity;
	}
	public void beforeDeleteEntity(ActionContext context,Session session,String entityName,Map entity) throws ActionException{
		
	}
}
