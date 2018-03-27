package app.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import sodium.annotation.Action;
import sodium.annotation.Anchor;
import sodium.annotation.FormAction;
import sodium.annotation.ActionGroup;
import net.sf.xmlform.action.ActionException;
import app.impl.RequestContextImpl;
import app.util.DbUtil;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.action.ReqCtx;

@Transactional(rollbackFor=Exception.class)
@ActionGroup(name="user")
public class Login extends BaseAction {

	final static public String WORKING_DEPT="userWorkingDept";
	
	@Action(name="login",role="anyone",
			anchors={
				@Anchor(type="save",label="login",page="user.Login"),
				@Anchor(type="save",label="login",page="user.LoginDialog")
			}
		)
	@FormAction(source="user.Login",result="user.Login")
	public List submitForm(final ActionContext ctx,final List data)throws ActionException {
		ReqCtx rtx=(ReqCtx)ctx.getRequestContext();
		Map login=(Map)data.get(0);
		List users=query("from SysUser where code=:logonName AND password=:pwd AND status='0'",
				"logonName", (String)login.get("code"),
				"pwd", DbUtil.md5((String)login.get("password"))
				);
		if(users.size()==0){
			ctx.getSourceInfo(login).setError("password", "登录名或密码错误");
			return null;
		}
		Map user=(Map)users.get(0);
		List depts=null;
		String userDeptId=(String)login.get("dept");
		String userDeptName="";
		if(RequestContextImpl.ROOT_ID.equals((String)user.get("id"))){
			depts=query("from adminDept where parent is null");
			if(depts.size()>0){
				userDeptId=(String)((Map)depts.get(0)).get("id");
			}
		}else{
			depts=query("select new map(d.dept as id) from SysUserDept d where user=:id AND dept=:dept","id", (String)user.get("id"),"dept", userDeptId);
			if(depts.size()==0){
				ctx.getSourceInfo(login).setError("dept", "无效");
				return null;
			}
		}
		
		List ds=new ArrayList();
		Iterator it=depts.iterator();
		while(it.hasNext()){
			Map d=(Map)it.next();
			String did=(String)d.get("id");
			ds.add(did);
			if(did.equals(userDeptId)){
				userDeptName=(String)d.get("fullname");
			}
		}
		
		List roles=new ArrayList();
		if(!RequestContextImpl.ROOT_ID.equals((String)user.get("id"))){
			it=query("select new map(d.role as id) from SysUserRole d where user=:id ","id", (String)user.get("id")).iterator();
			while(it.hasNext()){
				Map d=(Map)it.next();
				roles.add(d.get("id"));
			}
		}
		
		rtx.setLocal(WORKING_DEPT, ds);
		rtx.setLocal(RequestContextImpl.USER_ID, user.get("id"));
		rtx.setLocal(RequestContextImpl.USER_NAME, user.get("name"));
		rtx.setLocal(RequestContextImpl.USER_ROLES, roles.toArray(new String[roles.size()]));
		
		rtx.setLocal(RequestContextImpl.USER_DEPTID, userDeptId);
		rtx.setLocal(RequestContextImpl.USER_DEPTNAME, userDeptName);
		return null;
	}
}
