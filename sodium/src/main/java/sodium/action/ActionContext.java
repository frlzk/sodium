package sodium.action;

import java.util.Locale;

import net.sf.xmlform.data.ResultInfo;
import net.sf.xmlform.data.SortField;
import net.sf.xmlform.data.SourceInfo;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.RequestContext;


/**
 * @author Liu Zhikun
 */

public interface ActionContext{
	public String getActionName();
	public String getActionLabel(Locale local);
	public XMLForm getSourceForm();
	public XMLForm getResultForm();
	public FormLayout getResultFormLayout();
	public SourceInfo getSourceInfo(Object data);
	public ResultInfo createResultInfo(Object data);
	public int getFirstResult();
	public int getMaxResults();
	public SortField[] getSortFields();
	public void setTotalResults(long total);
	public void setResultMessage(String message);
	public void attachResultForm();
	public RequestContext getRequestContext();
	public String getCategory();
}
