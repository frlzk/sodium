package sodium.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

public class PropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {
	private String jndiPrefix=null;
	private Properties _properties;
	
	public String getJndiPrefix() {
		return jndiPrefix;
	}
	public void setJndiPrefix(String jndiPrefix) {
		this.jndiPrefix = jndiPrefix;
	}
//	public Object get(String key) {
//        return _properties.get(key);  
//    }
	public String getProperty(String key) {
        String str= resolvePlaceholder(key, _properties);
        return str!=null?str:System.getProperty(jndiPrefix+"."+key);
    }
	@Override
	protected void loadProperties(Properties props) throws IOException {
		super.loadProperties(props);
		try{
			Context initCtx = new InitialContext();
			Context envCtx = (Context)initCtx.lookup("java:comp/env");
			Enumeration en = props.propertyNames();
			while(en.hasMoreElements()){
				String key=en.nextElement().toString();
				fromJndi(props,envCtx,key);
			}
			this.setProperties(props);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	protected Properties mergeProperties() throws IOException {
		_properties=super.mergeProperties();
		return _properties;
	}
	protected void fromJndi(Properties props,Context envCtx,String key){
		try
		{
			Object value=envCtx.lookup(jndiPrefix==null?key:(jndiPrefix+"."+key));
			props.put(key, value);
		}catch(Exception e){
		} 
	}
	
}
