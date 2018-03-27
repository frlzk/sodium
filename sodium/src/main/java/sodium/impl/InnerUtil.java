package sodium.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import net.sf.xmlform.util.FormUtils;
import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;

import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import sodium.util.Util;

/**
 * @author Liu Zhikun
 */

final public class InnerUtil {
	static private ResourcePatternResolver resourcePatternResolver=new PathMatchingResourcePatternResolver();
	public static I18NTexts parseLabel(Element element){
		Element child=element.element("label");
		if(child!=null){
			return parseI18NTexts(child);
		}
		return Util.asI18NTexts(element.attributeValue("label"));
	}
	public static I18NTexts parseI18NTexts(Element labelElement){
		Iterator it=labelElement.elementIterator(FormUtils.DOM_ELEMENT_TEXT);
		I18NTexts texts=new I18NTexts();
		while(it.hasNext()){
			Element childElem=(Element)it.next();
			Locale locale=FormUtils.parseLocale(childElem.attributeValue(FormUtils.DOM_ATTRIBUTE_LANG));
			String value=childElem.getTextTrim();
			if(value!=null){
				texts.put(new I18NText(locale,value));
			}
		}
		return texts;
	}
	public static I18NTexts parseJsonI18NTexts(Object obj)throws JSONException{
		if(obj instanceof String){
			String s=(String)obj;
			if(s.startsWith("{")){
				JSONObject jobj=new JSONObject(s);
				return parseI18NTexts(jobj);
			}
			return Util.asI18NTexts(s);
		}else if(obj instanceof JSONObject){
			return parseI18NTexts((JSONObject)obj);
		}
		throw new JSONException("Must be String or JSONObject");
	}
	public static I18NTexts parseI18NTexts(JSONObject json){
		Iterator it=json.keys();
		I18NTexts texts=new I18NTexts();
		while(it.hasNext()){
			String key=(String)it.next();
			Locale locale=FormUtils.parseLocale(key);
			try {
				texts.put(new I18NText(locale,json.getString(key)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return texts;
	}
//	public static String normalizeJsonString(String str){
//		if(str==null||str.length()==0)
//			return str;
//		str=str.trim();
//		if(str.indexOf(":")>0){
//			if(!str.startsWith("{")){
//				return "{"+str+"}";
//			}else{
//				return str;
//			}
//		}else{
//			return str;
//		}
//	}
//	public static long scanPackages(String packagesToScan[],long last,String ext,ResourceVisitor vis) throws IOException{
//		long max=last;
//		String ex="*.*";
//		if(ext!=null){
//			ex="*"+ext;
//		}
//		for (String pkg : packagesToScan) {
//			String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
//					ClassUtils.convertClassNameToResourcePath(pkg) + "/**/"+ex;
//			Resource[] resources = resourcePatternResolver.getResources(pattern);
//			//MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
//			for (Resource resource : resources) {
//				if (resource.isReadable()) {
//					if(resource.lastModified()>last){
//						vis.visit(resource);
//					}
//					if(resource.lastModified()>max){
//						max=resource.lastModified();
//					}						
//				}
//			}
//		}
//		return max;
//	}
}
