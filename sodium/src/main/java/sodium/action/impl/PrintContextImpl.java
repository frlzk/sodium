package sodium.action.impl;

import java.util.Locale;

import net.sf.xmlform.data.ResultInfo;
import net.sf.xmlform.data.SortField;
import net.sf.xmlform.data.SourceInfo;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.RequestContext;
import sodium.action.PrintContext;

/**
 * @author Liu Zhikun
 */

public class PrintContextImpl implements PrintContext {
	private ActionContextImpl aci;
	private String format;
	public PrintContextImpl(ActionContextImpl context,String format) {
		aci=context;
		this.format=format;
	}
	public String getActionName() {
		return aci.getActionName();
	}
	public String getActionLabel(Locale local) {
		return aci.getActionLabel(local);
	}
	public XMLForm getSourceForm() {
		return aci.getSourceForm();
	}
	public XMLForm getResultForm() {
		return aci.getResultForm();
	}
	public FormLayout getResultFormLayout(){
		return aci.getResultFormLayout();
	}
	public SourceInfo getSourceInfo(Object data) {
		return aci.getSourceInfo(data);
	}
	public ResultInfo createResultInfo(Object data) {
		return aci.createResultInfo(data);
	}
	public int getFirstResult() {
		return aci.getFirstResult();
	}
	public int getMaxResults() {
		return aci.getMaxResults();
	}
	public SortField[] getSortFields() {
		return aci.getSortFields();
	}
	public void setTotalResults(long total) {
		aci.setTotalResults(total);
	}
	public void setResultMessage(String message) {
		aci.setResultMessage(message);
	}
	public void attachResultForm() {
		aci.attachResultForm();
	}
	
	public RequestContext getRequestContext() {
		return aci.getRequestContext();
	}
	public String getCategory() {
		return aci.getCategory();
	}
	public String getPrintFormat() {
		return format;
	}
}
