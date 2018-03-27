package sodium.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xmlform.XMLFormPastport;
import net.sf.xmlform.config.TypeDefinition;
import net.sf.xmlform.impl.DefaultConfigurationProvider;
import sodium.RequestContext;
import sodium.action.TypeAdapteContext;
import sodium.action.impl.TypeAdapter;

public class InnerConfigurationProvider extends DefaultConfigurationProvider {
	private Map typeAdapters=new HashMap();
	public InnerConfigurationProvider(Map adapters){
		typeAdapters=adapters;
	}
	
	public TypeDefinition getType(final XMLFormPastport pastport, String name) {
		TypeDefinition td=super.getType(pastport, name);
		List<TypeAdapter> adapters=(List<TypeAdapter>)typeAdapters.get(name);
		if(td==null||adapters==null)
			return td;
		try {
			td=(TypeDefinition)td.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
		TypeAdapteContext ctx=new TypeAdapteContext(){
			public RequestContext getRequestContext() {
				return (RequestContext)pastport;
			}
		};
		for(int i=0;i<adapters.size();i++){
			td=adapters.get(i).adapte(ctx, td);
		}
		return td;
		
	}
}
