package sodium.impl;

import java.util.List;

import sodium.action.impl.FormActionAdapter;
import net.sf.xmlform.action.Action;
import net.sf.xmlform.action.ActionContext;
import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.action.ActionExecutor;

/**
 * @author Liu Zhikun
 */

public class ActionExecutorImpl implements ActionExecutor {

	public List execute(ActionContext context,Action action,List data)throws ActionException {
		FormActionAdapter faa=(FormActionAdapter)action;
		return faa.execute(context, data);
	}

}
