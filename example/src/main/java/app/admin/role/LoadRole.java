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

import sodium.action.ActionContext;
import sodium.engine.ActionNode;
import sodium.engine.Engine;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.action.ReqCtx;
import app.hibernate.QueryParams;

@Component
@Transactional(rollbackFor=Exception.class)
public class LoadRole extends BaseAction {

	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Engine engine=(Engine)((ReqCtx)context.getRequestContext()).getGlobal("engine");
				Set roles=new HashSet();
				List actions=engine.getActionNodes(context.getRequestContext(), roles);
				String id=(String)getSimpleParameter(data,"id");
				Map res=new HashMap();
				res.put("actions", addItems(session,actions,id));
				if(id!=null){
					Map role=(Map)session.get("adminRole", id);
					res.put("name", role.get("name"));
				}else{
					res.put("name", "");
				}
				List list=new ArrayList();
				Map row=new HashMap();
				JSONObject obj=new JSONObject(res);
				row.put("id", obj.toString());
				list.add(row);
				return list;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
	private List addItems(Session session,List actions,String roleId) {
		JSONObject parentObj;
		
		List array=new ArrayList();
		if(actions.size()>0){
			Iterator it=actions.iterator();
			while(it.hasNext()){
				ActionNode r=(ActionNode)it.next();
				Map obj=new HashMap();
				array.add(obj);
				obj.put("id", r.getName());
				obj.put("text", r.getLabel());
				obj.put("checked", isCheck(session,r.getName()));
				List items=addItems(session,r.getChildActions(),roleId);
				if(items!=null&&items.size()>0)
					obj.put("children", items);
				obj.put("leaf", items.size()==0);
			}
		}
		return array;
	}
	private boolean isCheck(Session session,String id){
		QueryParams qp=new QueryParams();
		StringBuilder sb=new StringBuilder("SELECT * FROM sys_systemroleaction WHERE ra_action='"+id+"'");
		Query query=session.createSQLQuery(sb.toString());
		qp.apply(query);
		List list=query.list();
		return list.size()>0;
	}
}
