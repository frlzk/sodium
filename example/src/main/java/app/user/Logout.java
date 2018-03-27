package app.user;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;

import app.impl.RequestContextImpl;


import net.sf.xmlform.action.ActionException;

import app.action.BaseAction;
import app.action.ReqCtx;

@Component
@Transactional(rollbackFor=Exception.class)
public class Logout extends BaseAction {
	final static public String WORKING_DEPT="userWorkingDept";
	final static public String WORKING_GRID="userWorkingGrid";
	public List submitForm(ActionContext ctx, List data)throws ActionException {
		ReqCtx rtx=(ReqCtx)ctx.getRequestContext();
		rtx.setLocal(WORKING_DEPT, null);
		rtx.setLocal(WORKING_GRID, null);
		rtx.setLocal(RequestContextImpl.USER_ID, null);
		rtx.setLocal(RequestContextImpl.USER_NAME, null);
		rtx.setLocal(RequestContextImpl.USER_ROLES, null);
		return null;
	}

}
