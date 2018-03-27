package app.admin.dept;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import sodium.annotation.ActionGroup;
import net.sf.xmlform.action.ActionException;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.hibernate.QueryParams;

@Transactional(rollbackFor=Exception.class)
@ActionGroup(name="aaaaaa")
public class DelDept extends BaseAction {
	public List submitForm(ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Map dept=(Map)data.get(0);
				String id=(String)dept.get("id");
				QueryParams qp=new QueryParams();
				StringBuilder sb=new StringBuilder("from adminDept where isdel='1' and ");
				qp.add(sb,"parent", "=", id);
				Query query=session.createQuery(sb.toString());
				qp.apply(query);
				if(query.list().size()>0){
					throw new ActionException("有下级机构不能删除");
				}
				
				sb=new StringBuilder("update adminDept set isdel='1' where ");
				qp=new QueryParams();
				qp.add(sb,"id", "=", id);
				query=session.createQuery(sb.toString());
				qp.apply(query);
				query.executeUpdate();
				
				sb=new StringBuilder("delete adminDeptRel where ");
				qp=new QueryParams();
				qp.add(sb,"cid", "=", id);
				query=session.createQuery(sb.toString());
				qp.apply(query);
				query.executeUpdate();
				
				return null;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
}
