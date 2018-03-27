package sodium.impl;

import java.util.List;

import net.sf.xmlform.XMLFormPastport;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.impl.XMLFormLayoutPortImpl;
import sodium.RequestContext;
import sodium.action.LayoutAdapteContext;
import sodium.action.impl.LayoutAdapter;
import sodium.engine.Configuration;

/**
 * @author Liu Zhikun
 */

public class XmlformLayoutPort extends XMLFormLayoutPortImpl {
	private Configuration configuration;
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public FormLayout getFormLayout(final XMLFormPastport pastport, String id) {
		FormLayout layout= super.getFormLayout(pastport, id);
		List<LayoutAdapter> adapters=configuration.getLayoutAdapters().get(id);
		if(adapters==null)
			return layout;
		LayoutAdapteContext ctx=new LayoutAdapteContext(){
			public RequestContext getRequestContext() {
				return (RequestContext)pastport;
			}
		};
		for(int i=0;i<adapters.size();i++){
			layout=adapters.get(i).adapte(ctx, layout);
		}
		return layout;
		
	}
	
}
