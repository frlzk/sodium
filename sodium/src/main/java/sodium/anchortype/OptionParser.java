package sodium.anchortype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sf.xmlform.util.I18NTexts;
import sodium.anchoropt.ArrayOption;
import sodium.anchoropt.ObjectOption;
import sodium.impl.InnerUtil;
import sodium.util.Util;

public class OptionParser {
	private static Map supportClass=new HashMap();
	private interface Parser{
		public Object parse(String str);
		public Object copy(Object src);
	};
	static class BooleanParser implements Parser{
		public Object parse(String str){
			return Boolean.parseBoolean(str);
		}
		public Object copy(Object src){
			return src;
		}
	};
	static class StringParser implements Parser{
		public Object parse(String str){
			return str;
		}
		public Object copy(Object src){
			return src;
		}
	};
	static class IntegerParser implements Parser{
		public Object parse(String str){
			return Integer.parseInt(str);
		}
		public Object copy(Object src){
			return src;
		}
	};
	static class ArrayParser implements Parser{
		public Object parse(String str){
			if(str==null){
				return null;
			}
			if(!str.startsWith("[")){
				str="["+str+"]";
			}
			try {
				return parseOptArray(new JSONArray(str));
			} catch (JSONException e) {
				throw new IllegalArgumentException("Invalid json array: "+str+" "+e.getLocalizedMessage());
			}
		}
		public Object copy(Object src){
			ArrayOption srcArr=(ArrayOption)src;
			ArrayOption destArr=new ArrayOption();
			for(int i=0;i<srcArr.length();i++){
				destArr.put(copyFrom(srcArr.get(i)));
			}
			return destArr;
		}
	};
	static class ObjectParser implements Parser{
		public Object parse(String str){
			if(str==null){
				return null;
			}
			if(!str.startsWith("{")){
				str="{"+str+"}";
			}
			try {
				return parseOptObject(new JSONObject(str));
			} catch (JSONException e) {
				throw new IllegalArgumentException("Invalid json object: "+str+" "+e.getLocalizedMessage());
			}
		}
		public Object copy(Object src){
			ObjectOption srcArr=(ObjectOption)src;
			ObjectOption destArr=new ObjectOption();
			String keys[]=srcArr.keys();
			for(int i=0;i<keys.length;i++){
				destArr.put(keys[i],copyFrom(srcArr.get(keys[i])));
			}
			return destArr;
		}
	};
	static class I18NTextParser implements Parser{
		public I18NTexts parse(String str){
			if(str==null){
				return null;
			}
			try {
				if(str.startsWith("{")){
					return parseI18NTexts(new JSONObject(str));
				}
				return Util.asI18NTexts(str);
			}catch (JSONException e) {
				throw new IllegalArgumentException("Invalid json object: "+str+" "+e.getLocalizedMessage());
			}
		}
		public Object copy(Object src){
			I18NTexts srcArr=(I18NTexts)src;
			try {
				return (I18NTexts)srcArr.clone();
			} catch (CloneNotSupportedException e) {
				throw new IllegalArgumentException("Copy I18NTexts: "+e.getLocalizedMessage());
			}
		}
	};
	static{
		supportClass.put(String.class,new StringParser());
		supportClass.put(Integer.class,new IntegerParser());
		supportClass.put(Boolean.class,new BooleanParser());
		supportClass.put(ObjectOption.class,new ObjectParser());
		supportClass.put(ArrayOption.class,new ArrayParser());
		supportClass.put(I18NTexts.class,new I18NTextParser());
	}
	static public Object parseOpt(Class valueClass,String str){
		if(str==null||str.length()==0)
			return null;
		return ((Parser)supportClass.get(valueClass)).parse(str);
	}
	static public void checkSupport(Class cls){
		if(isSupport(cls)==false)
			throw new IllegalArgumentException("Not support value class: "+cls.getName());
	}
	static public boolean isSupport(Class cls){
		return supportClass.containsKey(cls);
	}
	static public ObjectOption parseOptObject(JSONObject obj) throws JSONException{
		ObjectOption opt=new ObjectOption();
		Iterator it=obj.keys();
		while(it.hasNext()){
			String key=(String)it.next();
			opt.put(key, parseObject(obj.get(key)));
		}
		return opt;
	}
	static public ArrayOption parseOptArray(JSONArray obj)throws JSONException{
		ArrayOption arr=new ArrayOption();
		for(int i=0;i<obj.length();i++){
			arr.put(parseObject(obj.get(i)));
		}
		return arr;
	}
	static public I18NTexts parseI18NTexts(JSONObject obj){
		return InnerUtil.parseI18NTexts(obj);
	}
	static public Object parseObject(Object v)throws JSONException{
		if(v instanceof JSONObject){
			JSONObject jobj=(JSONObject)v;
			String kind=null;
			if(jobj.has("@class")){
				kind=jobj.getString("@class");
			}
			if("i18n".equals(kind)){
				return parseI18NTexts(jobj);
			}
			return parseOptObject(jobj);
		}else if(v instanceof JSONArray){
			return parseOptArray((JSONArray)v);
		}else{
			return v;
		}
	}
	static public Object copyFrom(Object obj){
		if(obj==null)
			return null;
		return ((Parser)supportClass.get(obj.getClass())).copy(obj);
	}
}
