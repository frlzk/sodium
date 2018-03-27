package app.admin.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xmlform.action.ActionException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;

@Component
@Transactional(rollbackFor=Exception.class)
public class LoadUser extends BaseAction {
	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				String uid=(String)getSimpleParameter(data,"id");
				Map user=(Map)session.get("SysUser", uid);
				if(user==null)
					return null;
				Query q=session.createQuery("from SysUserRole where user=:user");
				q.setString("user", uid);
				user.put("roles", q.list());
				
				q=session.createQuery("from SysUserDept where user=:user");
				q.setString("user", uid);
				user.put("depts", q.list());
				
				q=session.createQuery("from SysUserGrid where user=:user");
				q.setString("user", uid);
				user.put("grids", q.list());
		
				List result=new ArrayList();
				result.add(user);
				
				Map formMap=new HashMap();
				formMap.put("admin.user.UserForm", new String[]{"createuser","lastuser","status","roles","depts","grids"});
				formMap.put("admin.user.UserRoleForm", new String[]{"role"});
				formMap.put("admin.user.UserDeptForm", new String[]{"dept"});
				formMap.put("admin.user.UserGridForm", new String[]{"grid"});
				
				return result;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
