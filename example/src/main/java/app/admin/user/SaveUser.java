package app.admin.user;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;


import app.util.DbUtil;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.admin.dept.LoadDept;
import app.hibernate.QueryParams;
import app.hibernate.SaveTemplate;

@Transactional(rollbackFor=Exception.class)
@Component
public class SaveUser extends BaseAction {
	@Autowired
	private LoadUser LoadUser;
	public List submitForm(final ActionContext context,final List data) throws ActionException {
		if(1==1)throw new ActionException("999");
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Map user=(Map)data.get(0);
				List roles=(List)user.remove("roles");
				List depts=(List)user.remove("depts");
				
				String id=(String)user.get("id");
				QueryParams qp=new QueryParams();
				StringBuilder sql=new StringBuilder("from SysUser where ");
				qp.add(sql, "status", "<>", "2");
				sql.append(" and ");
				qp.add(sql, "code", "=",user.get("code"));
				if(id!=null){
					sql.append(" AND id<>'").append(id).append("'");
				}
				Query query=session.createQuery(sql.toString());
				qp.apply(query);
				if(query.list().size()>0){
					context.getSourceInfo(user).setError("code", "登录名重复");
					return null;
				}
				
				if(id==null){
					user.put("password",DbUtil.md5(((String)user.get("password"))));
				}else{
					String pwd=(String)user.get("password");
					Map oldUser=(Map)session.get("SysUser", id);
					String oldPwd=(String)oldUser.get("password");
					if(!oldPwd.equals(pwd))
						user.put("password", DbUtil.md5(pwd));
				}
				
				Map newUser=(Map)SaveTemplate.saveEntity(context,session,"SysUser",user,SaveUser.this);
				String uid=(String)newUser.get("id");
				setFieldValue(roles,"user",uid);
				SaveTemplate.saveEntities(context,session,"SysUserRole",roles,SaveUser.this);
				setFieldValue(depts,"user",uid);
				SaveTemplate.saveEntities(context,session,"SysUserDept",depts,SaveUser.this);
				context.setResultMessage("保存成功");
				session.flush();
				return LoadUser.submitForm(context, Arrays.asList(newUser));
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
	
	public Map beforeCreateEntity(ActionContext context, Session session, String entityName, Map entity) throws ActionException {
		entity.put("id", DbUtil.nextUuid());System.out.println("*** "+DbUtil.nextUuid());
		return entity;
	}
	

}
