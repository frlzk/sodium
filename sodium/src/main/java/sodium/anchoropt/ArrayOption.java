package sodium.anchoropt;

import java.util.ArrayList;
import java.util.List;

import net.sf.xmlform.form.Default;
import net.sf.xmlform.util.I18NTexts;
import sodium.anchortype.OptionParser;


/**
 * @author Liu Zhikun
 */

final public class ArrayOption implements Cloneable{
	List optArray=new ArrayList();
	public ArrayOption(){
	}
	public ArrayOption(String a){
		ArrayOption oa=(ArrayOption)OptionParser.parseOpt(ArrayOption.class,a);
		optArray=oa.optArray;
	}
	public int length(){
		return optArray.size();
	}
	public ObjectOption getObject(int idx){
		return (ObjectOption)optArray.get(idx);
	}
	public void putObject(int idx,ObjectOption object){
		optArray.add(idx,object);
	}
	public void putObject(ObjectOption object){
		optArray.add(object);
	}
	public ArrayOption getArray(int idx){
		return (ArrayOption)optArray.get(idx);
	}
	public void putArray(int idx,ArrayOption array){
		optArray.add(idx,array);
	}
	public void putArray(ArrayOption array){
		optArray.add(array);
	}
	public String getString(int idx){
		return (String)optArray.get(idx);
	}
	public void putString(int idx,String str){
		optArray.add(idx,str);
	}
	public void putString(String str){
		optArray.add(str);
	}
	public Integer getInt(int idx){
		return (Integer)optArray.get(idx);
	}
	public void putInt(int idx,Integer str){
		optArray.add(idx,str);
	}
	public void putInt(Integer str){
		optArray.add(str);
	}
	public I18NTexts getI18NTexts(int idx){
		return (I18NTexts)optArray.get(idx);
	}
	public void putI18NTexts(int idx,I18NTexts str){
		optArray.add(idx,str);
	}
	public void putI18NTexts(I18NTexts str){
		optArray.add(str);
	}
	public Object get(int idx){
		return optArray.get(idx);
	}
	public void put(int idx,Object obj){
		if(obj!=null)
			OptionParser.checkSupport(obj.getClass());
		optArray.add(idx,obj);
	}
	public void put(Object obj){
		if(obj!=null)
			OptionParser.checkSupport(obj.getClass());
		optArray.add(obj);
	}
	public void putAll(ArrayOption arr){
		optArray.addAll(arr.optArray);
	}
	public Object clone() throws CloneNotSupportedException {
		Default cloneObj=(Default)super.clone();
		
		return cloneObj;
	}
}
