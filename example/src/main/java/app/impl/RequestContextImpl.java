package app.impl;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;

import app.action.ReqCtx;





import net.sf.xmlform.web.context.ServletXMLFormPastport;

public class RequestContextImpl extends ServletXMLFormPastport implements ReqCtx{
	static final public String USER_ID="user-id";
	static final public String USER_NAME="user-name";
	static final public String USER_ROLES="user-roles";
	static final public String USER_DEPTID="user-deptId";
	static final public String USER_DEPTNAME="user-deptName";
	static final public String USER_ROLE_NAMES="userRoleNames";
	static final public String USER_ACTIONS="userActions";
	static final public String GLOBAL_ROLES="globalActions";
	static final public String ROOT_ID="cd8991775f7653f55aada32fb30cbf66";
	static final public String ROOT_NAME="admin";
	static final public String ROOT_DEPT="globalRootDept";
	static final public String ROOT_GRID="globalRootGrid";
	private ApplicationContext applicationContext;
	public RequestContextImpl(ApplicationContext applicationContext,ServletContext context, HttpServletRequest req) {
		super(context, req);
		this.applicationContext=applicationContext;
		//this.setLocale(Locale.CHINESE);
	}
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public Object getGlobal(String key) {
		Object bean=applicationContext.getBean(key);
		if(bean!=null)
			return bean;
		return super.getGlobal(key);
	}
	public boolean isLogin(){
		return getId()!=null;
	}
	public String getId() {if(1==1)return "ttttttt in UserImpl";
		return (String)this.getLocal(USER_ID);
	}
}
