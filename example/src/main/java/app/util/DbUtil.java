package app.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.xmlform.form.Form;
import net.sf.xmlform.form.XMLForm;

import org.apache.commons.codec.digest.DigestUtils;

import sodium.action.ActionContext;



public class DbUtil {
	static private Map types=new HashMap();
	static{
		types.put("date", new SimpleDateFormat("yyyy-MM-dd"));
		types.put("time", new SimpleDateFormat("HH:mm:ss"));
		types.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}
	static public String nextUuid(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	static public String md5(String str){
		if(str==null)
			return null;
		return DigestUtils.md5Hex(str);
	}
	static public Timestamp getCurrentTimestamp(){
		Calendar c=Calendar.getInstance();
		return new Timestamp(c.getTimeInMillis());
	}
	static public java.util.Date addCalendarDate(Date date,int type,int days){
		Calendar cc=Calendar.getInstance();
		cc.setTime(date);
		cc.add(type, days);
		return cc.getTime();
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
	static public void removeFormFields(Form form,String retainFields[]){
		Set set=new HashSet();
		for(int i=0;i<retainFields.length;i++)
			set.add(retainFields[i]);
		removeFormFields(form,set);
	}
	static public void removeFormFields(Form form,Set retainFields){
		Set names=new HashSet();
		names.addAll(form.getFields().keySet());
		names.addAll(form.getSubforms().keySet());
		Iterator it=names.iterator();
		while(it.hasNext()){
			String name=(String)it.next();
			if(retainFields.contains(name))
				continue;
			Object field=form.getFields().remove(name);
			if(field==null){
				form.getSubforms().remove(name);
			}
		}
	}
	static public Map buildQueryColumns(ActionContext ctx,String tab){
		Map newMap = new HashMap();
		XMLForm xform=ctx.getResultForm();
		Iterator m =(xform.getRootForm()).getFields().keySet().iterator();
		while(m.hasNext()){
			String key=(String)m.next();
			newMap.put(key, tab+"."+key);
		}
		return newMap;
	}
	static public String buildQueryColumnMap(Map cols){
		StringBuilder sb=new StringBuilder();
		Iterator m = cols.keySet().iterator();
		while(m.hasNext()){
			String key=(String)m.next();
			if(sb.length()>0)
				sb.append(",");
			sb.append(cols.get(key)).append(" as ").append(key);
		}
		return sb.toString();
	}
}
