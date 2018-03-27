package sodium.util;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.sf.xmlform.data.impl.FieldTypeFacet;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.form.Form;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.type.BaseTypes;
import net.sf.xmlform.type.DateTimeType;
import net.sf.xmlform.type.IType;
import net.sf.xmlform.util.FormUtils;
import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;

import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Node;

import sodium.engine.MenuNode;

/**
 * @author Liu Zhikun
 */

public class Util {
	static private Map types=new HashMap();
	static private void init(){
		if(types!=null&&types.size()>0)
			return;
		types=new HashMap();
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
		init();
		if(timestamp==null || timestamp.length()==0){
			return null;
		}
		Calendar c=Calendar.getInstance();
		SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		c.setTime(sf.parse(timestamp));
		return new Timestamp(c.getTimeInMillis());
	}
	static public String formatDate(Date date){
		init();
		if(date==null)
			return "";
		SimpleDateFormat sdf=(SimpleDateFormat)types.get("date");
		return sdf.format(date);
	}
	static public String formatDatetime(Timestamp datetime){
		init();
		if(datetime==null)
			return "";
		SimpleDateFormat sdf=(SimpleDateFormat)types.get("timestamp");
		return sdf.format(datetime);
	}
	static public I18NTexts asI18NTexts(String text){
		I18NTexts ts=new I18NTexts();
		if(text!=null){
			ts.put(new I18NText(text));
		}
		return ts;
	}
	static public String getMD5(String data){
		return DigestUtils.md5Hex(data);
	}
	static public JSONArray menuNodeToJson(List menus,boolean collapse){
		return menuNodeToJson(menus,collapse,1);
	}
	static private JSONArray menuNodeToJson(List menus,boolean collapse,int level){
		
		JSONArray tbarItems = new JSONArray();
		try{
			Iterator menuIt = menus.iterator();
			while (menuIt.hasNext()) {
				MenuNode menu = (MenuNode) menuIt.next();
				JSONObject tbarItem = new JSONObject();
				tbarItem.put("label", menu.getLabel());
				int size=menu.getChildMenus().size();
				if(size>0){
					if (size == 1&&collapse==true) {
						tbarItem=menuNodeToJson(menu.getChildMenus(),collapse).getJSONObject(0);
					}else{
						tbarItem.put("children", menuNodeToJson(menu.getChildMenus(),collapse,level+1));
					}
				}else {
					tbarItem.put("page",menu.getPage());
				}
				tbarItems.put(tbarItem);
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		return tbarItems;
	}
	static public Map jsonToMap(JSONObject obj) throws JSONException{
		Map result=new HashMap();
		Iterator it=obj.keys();
		while(it.hasNext()){
			String key=(String)it.next();
			Object value=obj.get(key);
			if(value instanceof JSONObject){
				result.put(key, jsonToMap((JSONObject)value));
			}else if(value instanceof JSONArray){
				result.put(key, parseArray((JSONArray)value));
			}else
				result.put(key, value);
		}
		return result;
	}
	static private List parseArray(JSONArray array) throws JSONException{
		List list=new ArrayList();
		for(int a=0;a<array.length();a++){
			Object value=array.get(a);
			if(value instanceof JSONObject){
				list.add(jsonToMap((JSONObject)value));
			}else if(value instanceof JSONArray){
				list.add(parseArray((JSONArray)value));
			}else
				list.add(value);
		}
		return list;
	}
	public List formatFormData(XMLForm form,List data){
		return formatFormData(Locale.ENGLISH,form,data);
	}
	public List formatFormData(Locale locale,XMLForm form,List data){
		if(data==null)
			return Collections.EMPTY_LIST;
		if(form==null){
			return Collections.EMPTY_LIST;
		}
		return doFormatPrintData(form,form.getRootForm(),data);
	}
	static private List doFormatPrintData(XMLForm forms,Form form,List list){
		List res=new ArrayList(list.size());
		Field fs[]=(Field[])form.getFields().values().toArray(new Field[form.getFields().size()]);
		for(int i=0;i<list.size();i++){
			Map bean=(Map)list.get(i);
			if(bean==null)
				continue;
			Map r=new HashMap();
			res.add(r);
			for(int f=0;f<fs.length;f++){
				Object v=bean.get(fs[f].getName());
				if(v==null)
					continue;
				if(v instanceof Object[]){
					Object vv[]=(Object[])v;
					r.put(fs[f].getName(), formatField(fs[f],vv[0]));
					r.put(fs[f].getName()+"_text", vv[1]);
				}else{
					r.put(fs[f].getName(), formatField(fs[f],v));
				}
			}
		}
		return res;
	}
	static private String formatField(Field field,Object v){
		if(v==null)
			return null;
		IType type=BaseTypes.getTypeByClass(v.getClass());
		String text=type.objectToString(new FieldTypeFacet(Locale.getDefault(),field),v);
		if(type.getName().equals(DateTimeType.NAME)){
			text=text.replace("T", " ");
		}
		return text;
	}
	static public Method getMethodByName(Class cls,String name){
		Method ms[]=cls.getMethods();
		for(int i=0;i<ms.length;i++){
			if(ms[i].getName().equals(name))
				return ms[i];
		}
		return null;
	}
	/*
	 static private Random ran = new Random();
    private final static int delta = 0x9fa5 - 0x4e00 + 1;
	static public char getRandomHan() {
        return (char)(0x4e00 + ran.nextInt(delta)); 
    }
	 * 
	 */
}
