package sodium.cluster;

import java.util.HashMap;
import java.util.Map;

final public class ClusterEvent {
	private String type;
	private Map data=new HashMap();
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String[] getNames(){
		return (String[])data.keySet().toArray(new String[data.size()]);
	}
	public Object getObject(String name){
		return data.get(name);
	}
	public void setObject(String name,Object value){
		if(value!=null){
			Class type=value.getClass();
			if((!type.equals(Boolean.class))
					&&(!type.equals(Short.class))
					&&(!type.equals(Integer.class))
					&&(!type.equals(Long.class))
					&&(!type.equals(String.class))
				){
				throw new IllegalArgumentException("Not support type: "+value.getClass().getName());
			}
		}
		data.put(name, value);
	}
	public boolean getBoolean(String name){
		return (Boolean)data.get(name);
	}
	public void setBoolean(String name,boolean value){
		data.put(name, value);
	}
//	public float getFloat(String name){
//		return (Float)data.get(name);
//	}
//	public void setFloat(String name,float value){
//		data.put(name, value);
//	}
//	public double getDouble(String name){
//		return (Double)data.get(name);
//	}
//	public void setDouble(String name,double value){
//		data.put(name, value);
//	}
	public int getInt(String name){
		return (Integer)data.get(name);
	}
	public void setInt(String name,int value){
		data.put(name, value);
	}
	public long getLong(String name){
		return (Long)data.get(name);
	}
	public void setLong(String name,long value){
		data.put(name, value);
	}
	public long getShort(String name){
		return (Short)data.get(name);
	}
	public void setShort(String name,short value){
		data.put(name, value);
	}
	public String getString(String name){
		return (String)data.get(name);
	}
	public void setString(String name,String value){
		data.put(name, value);
	}
	public String toString() {
		return "type:"+type+" "+data.toString();
	}
	
}
