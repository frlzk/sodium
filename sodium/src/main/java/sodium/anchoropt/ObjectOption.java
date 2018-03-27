package sodium.anchoropt;

import java.util.HashMap;
import java.util.Map;

import net.sf.xmlform.form.Default;
import net.sf.xmlform.util.I18NTexts;

import org.json.JSONException;
import org.json.JSONObject;

import sodium.anchortype.OptionParser;

/**
 * @author Liu Zhikun
 */


final public class ObjectOption implements Cloneable {
	Map optObj=new HashMap();
	public ObjectOption(){
	}
	public ObjectOption(String jsonStr){
		try {
			optObj.putAll(OptionParser.parseOptObject(new JSONObject(jsonStr)).optObj);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
	}
	public int length(){
		return optObj.size();
	}
	public String[] keys(){
		return (String[])optObj.keySet().toArray(new String[optObj.size()]);
	}
	public boolean has(String key){
		return optObj.containsKey(key);
	}
	public ObjectOption getObject(String key){
		return (ObjectOption)optObj.get(key);
	}
	public void putObject(String key,ObjectOption object){
		optObj.put(key, object);
	}
	public ArrayOption getArray(String key){
		return (ArrayOption)optObj.get(key);
	}
	public void putArray(String key,ArrayOption array){
		optObj.put(key, array);
	}
	public String getString(String key){
		return (String)optObj.get(key);
	}
	public void putString(String key,String str){
		optObj.put(key, str);
	}
	public I18NTexts getI18NTexts(String key){
		return (I18NTexts)optObj.get(key);
	}
	public void putI18NTexts(String key,I18NTexts str){
		optObj.put(key, str);
	}
	public Integer getInt(String key){
		return (Integer)optObj.get(key);
	}
	public void putInt(String key,Integer str){
		optObj.put(key, str);
	}
	public Object get(String key){
		return optObj.get(key);
	}
	public void put(String key,Object obj){
		if(obj!=null)
			OptionParser.checkSupport(obj.getClass());
		optObj.put(key, obj);
	}
	public void putAll(ObjectOption obj){
		optObj.putAll(obj.optObj);
	}
	
	public Object clone() throws CloneNotSupportedException {
		Default cloneObj=(Default)super.clone();
		
		return cloneObj;
	}
}
