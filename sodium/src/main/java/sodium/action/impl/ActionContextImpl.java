package sodium.action.impl;


import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.xmlform.data.ResultInfo;
import net.sf.xmlform.data.SortField;
import net.sf.xmlform.data.SourceInfo;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.format.JSONConstants;
import net.sf.xmlform.formlayout.LayoutDescriptor;
import net.sf.xmlform.formlayout.XMLFormLayoutPort;
import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.RequestContext;
import sodium.action.ActionContext;
import sodium.print.renderer.FormReportUtil;

/**
 * @author Liu Zhikun
 */

public class ActionContextImpl implements ActionContext{
	private net.sf.xmlform.action.ActionContext actionContext;
	private EngineContext engineContext;
	private String category;
	private ActionImpl action;
	private FormLayout formLayout;
	public ActionContextImpl(EngineContext engineContext,net.sf.xmlform.action.ActionContext actionContext,String group,ActionImpl action) {
		this.actionContext=actionContext;
		this.engineContext=engineContext;
		this.category=group;
		this.action=action;
	}
	public String getActionName(){
		return action.getName();
	}
	public String getActionLabel(Locale local){
		return action.getLabel().getText(local);
	}
	
	public RequestContext getRequestContext() {
		return (RequestContext)actionContext.getPastport();
	}
	
	public String getCategory(){
		return category;
	}

	public XMLForm getSourceForm() {
		return actionContext.getSourceForm();
	}
	
	public XMLForm getResultForm() {
		return actionContext.getResultForm();
	}
	
	public FormLayout getResultFormLayout(){
		XMLForm f=getResultForm();
		if(f==null)
			return null;
		if(formLayout==null){
			XMLFormLayoutPort xfp=engineContext.getEngine().getXmlformLayoutPort();
			LayoutDescriptor des[]=xfp.getFormLayouts(getRequestContext(), f.getName());
			if(des.length==0){
				return null;
			}
			formLayout=xfp.getFormLayout(getRequestContext(), des[0].getId());
		}
		return formLayout;
	}

	public SourceInfo getSourceInfo(Object data) {
		return actionContext.getSourceInfo(data);
	}

	public ResultInfo createResultInfo(Object data) {
		return actionContext.createResultInfo(data);
	}

	public int getFirstResult() {
		return (int)actionContext.getFirstResult();
	}

	public int getMaxResults() {
		if(Long.MAX_VALUE==actionContext.getMaxResults())
			return Integer.MAX_VALUE;
		return (int)actionContext.getMaxResults();
	}
	
	public SortField[] getSortFields(){
		return actionContext.getSortFields();
	}

	public void setTotalResults(long total) {
		actionContext.setTotalResults(total);
	}
	
	public void setResultMessage(String message){
		actionContext.setResultMessage(message);
	}
	
	public void attachResultForm(){
		actionContext.addAttachment(JSONConstants.ATTACHMENT_FORM, this.getResultForm());
		if(formLayout!=null){
			actionContext.addAttachment(JSONConstants.ATTACHMENT_FORMLAYOUT, formLayout);
		}
	}
	
	public JasperPrint createJasperPrint(String template,Map parameters,List fieldValues){
		return FormReportUtil.buildPrintablePage(engineContext.getEngine(), template, parameters, fieldValues);
	}
}
