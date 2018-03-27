package sodium.anchortype.common;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.anchoropt.ArrayOption;
import sodium.anchoropt.ObjectOption;
import sodium.anchortype.Options;
import sodium.page.Page;

/**
 * @author Liu Zhikun
 */

public class AnchorTypeUtil {
	static public ObjectOption parseSourceField(Object obj){
		if(obj==null)
			return null;
		if(obj instanceof ObjectOption){
			return (ObjectOption)obj;
		}
		ObjectOption oo=new ObjectOption(obj.toString());
		return oo;
	}
	static public void nullif(Anchor anchor,String key,Object val){
		if(anchor.getOptions().has(key)){
			return;
		}
		anchor.getOptions().put(key, val);
	}
	static public void nullif(Anchor anchor,String key,String prop,Object val){
		ObjectOption obj=anchor.getOptions().getObject(key);
		if(obj==null){
			obj=new ObjectOption();
			anchor.getOptions().put(key, obj);
		}
		if(!obj.has(prop))
			obj.put(prop, val);
	}
	static public String getSourceForm(Anchor anchor){
		ArrayOption a=anchor.getOptions().getArray(Options.SOURCE_NAME);
		if(a==null)
			return null;
		return a.getString(0);
	}
	static public String getResultForm(Anchor anchor){
		return anchor.getOptions().getString(Options.RESULT_NAME);
	}
	static public Object getOptProperty(Anchor anchor,String key,String prop){
		ObjectOption obj=anchor.getOptions().getObject(key);
		if(obj==null)
			return null;
		return obj.get(prop);
	}
	static public void setDefaultAttach(Action action,Anchor anchor){
		String at=anchor.getAttach();
		if("s".equals(at)||"source".equals(at)){
			String sf=getSourceForm(anchor);
			anchor.setAttach(sf!=null?sf:action.getSourceForm());
			return;
		}
		if("r".equals(at)||"result".equals(at)){
			String rf=getResultForm(anchor);
			anchor.setAttach(rf!=null?rf:action.getResultForm());
			return;
		}
		if(at==null||at.length()==0){
			anchor.setAttach("page");
			return;
		}
	}
	static public void setDefaultAttach(Page page,Anchor anchor){
		String at=anchor.getAttach();
		if("s".equals(at)||"source".equals(at)){
			anchor.setAttach(getSourceForm(anchor));
			return;
		}
		if("r".equals(at)||"result".equals(at)){
			anchor.setAttach(getResultForm(anchor));
			return;
		}
		if(at==null||at.length()==0){
			anchor.setAttach("page");
			return;
		}
	}
//	static public void appendStyle(Anchor an,String key,String value){
//		an.setStyle(appendKeyValue(an.getStyle(),key,value));
//	}
//	static public void appendSource(Anchor an,String key,String value){
//		an.setSource(appendKeyValue(an.getSource(),key,value));
//	}
//	static public String getStyle(Anchor an,String key){
//		return getJsonKey(an.getStyle(),key);
//	}
//	static public String getFormsFromSource(Anchor an){
//		return getJsonKeys(an.getSource());
//	}
//	static public String getMasterSourceForm(Anchor an){
//		String namesStr=getJsonKeys(an.getSource());
//		if(namesStr==null)
//			return null;
//		String names[]=namesStr.split(",");
//		if(names.length==1)
//			return names[0];
//		for(int i=0;i<names.length;i++){
//			if(names[i].startsWith("!")){
//				if(names[i].startsWith("!!"))
//					return names[i].substring(2);
//				return names[i].substring(1);
//			}
//		}
//		return names[0];
//	}
//	static private String getJsonKeys(String str){
//		try{
//			if(str==null||str.length()==0)
//				return null;
//			if(str.startsWith("{")){
//				StringBuilder sb=new StringBuilder();
//				JSONObject obj=new JSONObject(str);
//				Iterator it=obj.keys();
//				int idx=0;
//				while(it.hasNext()){
//					if(idx>0)
//						sb.append(",");
//					sb.append(it.next());
//				}
//				return sb.toString();
//			}
//			return str;
//		}catch(Exception e){
//			throw new IllegalStateException(e.getMessage(),e);
//		}
//	}
//	static private String getJsonKey(String str,String key){
//		try{
//			if(str==null||str.length()==0)
//				return null;
//			if(str.startsWith("{")){
//				JSONObject obj=new JSONObject(str);
//				if(obj.has(key))
//					return obj.get(key).toString();
//				return null;
//			}
//			return null;
//		}catch(Exception e){
//			throw new IllegalStateException(e.getMessage(),e);
//		}
//	}
//	static private String appendKeyValue(String old,String key,String value){
//		try{
//			if(old==null||old.length()==0){
//				return keyValue(key,value);
//			}else{
//				return objKeyValue(old,key,value);
//			}
//		}catch(Exception e){
//			throw new IllegalStateException(e.getMessage(),e);
//		}
//	}
//	static private String keyValue(String k,String v) throws JSONException{
//		JSONObject obj=new JSONObject();
//		obj.put(k, v);
//		return obj.toString();
//	}
//	static private String objKeyValue(String old,String k,String v) throws JSONException{
//		JSONObject obj=new JSONObject(old);
//		if(!obj.has(k))
//			obj.put(k, v);
//		return obj.toString();
//	}
}
