package app.admin.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.xmlform.action.ActionException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.Action;
import sodium.action.ActionContext;
import sodium.engine.Engine;
import sodium.util.Util;
import app.impl.RequestContextImpl;
import app.util.DbUtil;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.action.ReqCtx;
import app.hibernate.QueryParams;

@Component
@Transactional(rollbackFor=Exception.class)
public class SaveRole extends BaseAction {
	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				String roleDef=(String)getSimpleParameter(data,"id");
				try {
					JSONObject obj=new JSONObject(roleDef);
					Map req=Util.jsonToMap(obj);
					String id=saveRole(session,context,req);
					List result=new ArrayList();
					Map map=new HashMap();
					map.put("id",id);
					result.add(map);
					context.setResultMessage("角色保存成功");
					session.flush();
					return result;
				} catch (Exception e) {
					e.printStackTrace();
					throw new ActionException(e.getLocalizedMessage());
				}
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
	public String saveRole(Session session,ActionContext ctx,Map req)throws Exception {
		ReqCtx rtx=(ReqCtx)ctx.getRequestContext();
		Engine engine=(Engine)rtx.getGlobal("engine");
		String id=(String)req.get("id");
		String name=(String)req.get("name");
		if(id==null||id.length()==0){
			Query q=session.createQuery("from adminRole where name=:name");
			q.setString("name", name);
			checkName(q);
			Map role=new HashMap();
			id=DbUtil.nextUuid();
			role.put("id", id);
			role.put("name", name);
			role.put("createuser",rtx.getLocal(RequestContextImpl.USER_ID));
			role.put("createdate",DbUtil.getCurrentTimestamp());
			role.put("lastuser",rtx.getLocal(RequestContextImpl.USER_ID));
			role.put("lastdate",DbUtil.getCurrentTimestamp());
			session.save("adminRole", role);
		}else{
			Query q1=session.createQuery("from adminRole where name=:name and id<>:id");
			q1.setString("id", id);
			q1.setString("name", name);
			checkName(q1);
			Map role=(Map)session.get("adminRole", id);
			role.put("name", name);
			role.put("lastuser",rtx.getLocal(RequestContextImpl.USER_ID));
			role.put("lastdate",DbUtil.getCurrentTimestamp());
			session.update("adminRole", role);
			StringBuilder sb=new StringBuilder("DELETE FROM adminRoleAction WHERE ");
			QueryParams qp=new QueryParams();
			qp.add(sb,"role", "=", id);
			Query q=session.createQuery(sb.toString());
			qp.apply(q);
			q.executeUpdate();
		}
		List actions=(List)req.get("actions");
		Set savedActions=new HashSet();
		Set partActions=new HashSet();
		for(int i=0;i<actions.size();i++){
			Map a=(Map)actions.get(i);
			String actionName=(String)a.get("name");
			savedActions.add(actionName);
			Action action=engine.getAction(actionName);
			String isbottom="0";
			if(action!=null){
				isbottom="1";
				if(action.getPartners()!=null){
					String pats[]=action.getPartners();
					for(int p=0;p<pats.length;p++){
						partActions.add(pats[p]);
					}
				}
			}
			insertAction(session,id,actionName,isbottom);
		}
		Iterator it=partActions.iterator();
		while(it.hasNext()){
			String actionName=(String)it.next();
			if(savedActions.contains(actionName))
				continue;
			insertAction(session,id,actionName,"1");
		}
		
		return id;
	}
	private void insertAction(Session session,String role,String action,String isbottom){
		Map act=new HashMap();
		act.put("id", DbUtil.nextUuid());
		act.put("role", role);
		act.put("action", action);
		act.put("isbottom", isbottom);
		session.save("adminRoleAction", act);
	}
	private void checkName(Query q) throws Exception{
		if(q.list().size()>0){
			throw new Exception("角色名称重复");
		}
	}
}
