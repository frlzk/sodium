package app.action;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import net.sf.xmlform.action.ActionException;

public class ActionUtil {
	static private Map types=new HashMap();
	static{
		types.put("date", new SimpleDateFormat("yyyy-MM-dd"));
		types.put("time", new SimpleDateFormat("HH:mm:ss"));
		types.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	static public String nextUuid(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	static public Timestamp getCurrentTimestamp(){
		Calendar c=Calendar.getInstance();
		return new Timestamp(c.getTimeInMillis());
	}
	static public Date getCurrentDate(){
		Calendar c=Calendar.getInstance();
		return new Date(c.getTimeInMillis());
	}
	static public Date parseDate(String date) throws ParseException{
		if(date==null || date.length()==0){
			return null;
		}
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
		c.setTime(sf.parse(date));
		return new Date(c.getTimeInMillis());
	}
	static public Timestamp parseTimestamp(String timestamp) throws ParseException{
		if(timestamp==null || timestamp.length()==0){
			return null;
		}
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		c.setTime(sf.parse(timestamp));
		return new Timestamp(c.getTimeInMillis());
	}
	static public String formatDate(Date date){
		if(date==null)
			return "";
		SimpleDateFormat sdf=(SimpleDateFormat)types.get("date");
		return sdf.format(date);
	}
	static public String formatDatetime(Timestamp datetime){
		if(datetime==null)
			return "";
		SimpleDateFormat sdf=(SimpleDateFormat)types.get("timestamp");
		return sdf.format(datetime);
	}
	static public String getMD5(String data){
		return DigestUtils.md5Hex(data);
	}
	static public Object exec(HibernateTemplate hiberanteTemplate,final Hscb hs)throws ActionException{
		final ActionException ae[]=new ActionException[1];
		Object res=hiberanteTemplate.execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				try {
					return hs.exec(session);
				} catch (ActionException e) {
					ae[0]=e;
					return null;
				}
			}
			
		});
		if(ae[0]!=null)
			throw ae[0];
		return res;
	}
}
