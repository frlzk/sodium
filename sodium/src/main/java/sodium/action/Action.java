package sodium.action;

import java.util.List;

import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.data.SourceType;
import net.sf.xmlform.util.I18NTexts;

/**
 * @author Liu Zhikun
 */

public interface Action {
	public String getName();
	public I18NTexts getLabel();
	public String getDesc();
	public List getAnchors();
	public String getRole();
	public String[] getPartners();
	public String[] getLeaders();
//	public String[] getPrevious();
	
	public SourceType getSourceType();
	public String getSourceForm();
	public String getResultForm();
	public long getMinoccurs();
	public long getMaxoccurs();
//	public List execute(ActionContext context,List data)throws ActionException;
}