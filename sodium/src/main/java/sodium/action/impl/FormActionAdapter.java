package sodium.action.impl;

import java.util.List;

import sodium.action.impl.ActionContextImpl;
import net.sf.xmlform.action.ActionContext;
import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.data.SourceType;

/**
 * @author Liu Zhikun
 */

public class FormActionAdapter implements net.sf.xmlform.action.Action{
	private ActionImpl action;
	private EngineContext engineContext;
	private String group;
	public FormActionAdapter(EngineContext engineContext,ActionImpl action,String group){
		this.action=action;
		this.engineContext=engineContext;
		this.group=group;
	}
	public SourceType getSourceType() {
		return action.getSourceType();
	}
	
	public ActionImpl getAction() {
		return action;
	}
	public List execute(ActionContext context, List list) throws ActionException{
		ActionContextImpl ac=new ActionContextImpl(engineContext,context,group,action);
		List res=doExecute(ac, list);
		return res;
	}
	protected List doExecute(ActionContextImpl context,List data)throws ActionException{
		List res=action.execute(context,data);
		return res;
	}
	public String getName() {
		return action.getName();
	}

	public String getSourceForm() {
		return action.getSourceForm();
	}

	public String getResultForm() {
		return action.getResultForm();
	}


	public long getMinoccurs() {
		return action.getMinoccurs();
	}

	public long getMaxoccurs() {
		return action.getMaxoccurs();
	}
}
