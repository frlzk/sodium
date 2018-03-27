package app.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

public class HibernateUtil {
	static public Object singleValue(Session session,String sql,Object params[]){
		Query query=createQuery(session,sql,params);
		List list=query.list();
		if(list.size()==0)
			return null;
		return list.get(0);
	}
	static public Query createQuery(Session session,String sql,Object params[]){
		Query query=session.createQuery(sql);
		int i=0;
		while(i<params.length){
			String name=(String)params[i];
			Object value=params[i+1];
			if(value instanceof String){
				query.setString(name, (String)value);
			}else if(value instanceof Long){
				query.setLong(name, (Long)value);
			}else if(value instanceof Timestamp){
				query.setTimestamp(name, (Timestamp)value);
			}else if(value instanceof Integer){
				query.setInteger(name, (Integer)value);
			}else if(value instanceof Date){
				query.setDate(name, (Date)value);
			}else{
				throw new RuntimeException("Not process type: "+value.getClass().getCanonicalName());
			}
			i=i+2;
		}
		return query;
	}
}
