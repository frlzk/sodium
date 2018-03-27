package sodium.impl;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import sodium.RequestContext;
import sodium.action.FormAdapteContext;
import sodium.action.impl.FormAdapter;
import sodium.engine.Configuration;
import sodium.engine.Setting;
import sodium.print.impl.DefaultPrinterProvider;
import net.sf.xmlform.XMLFormPastport;
import net.sf.xmlform.form.XMLForm;

/**
 * @author Liu Zhikun
 */

public class XmlformPort extends net.sf.xmlform.impl.XMLFormPortImpl{
	private Map forms=new WeakHashMap();
	private Setting setting=null;
	private Configuration configuration;
	public XmlformPort(){
		setActionExecutor(new ActionExecutorImpl());
	}
	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
	}
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	public XMLForm getForm(final XMLFormPastport pastport, String formName) {
		//if(setting.isDebug()){
		XMLForm form=super.getForm(pastport, formName);
		List<FormAdapter> adapters=configuration.getFormAdapters().get(formName);
		if(adapters==null)
			return form;
		FormAdapteContext ctx=new FormAdapteContext(){
			public RequestContext getRequestContext() {
				return (RequestContext)pastport;
			}
		};
		for(int i=0;i<adapters.size();i++){
			form=adapters.get(i).adapte(ctx, form);
		}
		return form;
		//}
//		XMLForm form=(XMLForm)forms.get(formName);
//		if(form==null){
//			form=super.getForm(pastport, formName);
//			if(form!=null)
//				forms.put(form.getName(), form);
//			else
//				return null;
//		}
//		try {
//			return (XMLForm)form.clone();
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//		}
//		return null;
	}
	
}
