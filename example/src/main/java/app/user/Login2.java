package app.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import sodium.annotation.Action;
import sodium.annotation.Anchor;
import sodium.annotation.ActionGroup;
import net.sf.xmlform.action.ActionException;
import app.impl.RequestContextImpl;
import app.util.DbUtil;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.action.ReqCtx;

@Transactional(rollbackFor=Exception.class)
@Component
public class Login2 extends BaseAction {

	final static public String WORKING_DEPT="userWorkingDept";
	
	public List submitForm(final ActionContext ctx,final List data)throws ActionException {
		if(1==1)
				throw new ActionException("2222");
		return null;
	}
}
