package app.admin.dept;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;

import app.impl.RequestContextImpl;
import app.user.Login;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.action.ReqCtx;
import app.hibernate.QueryParams;

@Component
@Transactional(rollbackFor=Exception.class)
public class DeptItems extends BaseAction {

	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				ReqCtx rtx=(ReqCtx)context.getRequestContext();
				Map id=(Map)data.get(0);
				String parent=(String)id.get("id");
				String all=(String)id.get("all");
				if(all==null){
					all="0";
				}
				String sql=null;
				QueryParams qp=new QueryParams();
				if(parent==null){
					if(RequestContextImpl.ROOT_ID.equals(rtx.getLocal(RequestContextImpl.USER_ID))){
						sql="from adminDept WHERE isdel='0' and parent is null ORDER BY orderno";
					}else{
						List wd=(List)rtx.getLocal(Login.WORKING_DEPT);
						if(wd==null){
							sql="from adminDept WHERE isdel='0' and parent is null ORDER BY orderno";
						}else{
							StringBuilder sb=new StringBuilder("from adminDept WHERE isdel='0' and id in('1'");
							Iterator it=wd.iterator();
							while(it.hasNext()){
								sb.append(",'").append((String)it.next()).append("'");
							}
							sb.append(") ORDER BY orderno");
							sql=sb.toString();
						}
					}
				}else{
					sql="from adminDept WHERE isdel='0' and parent='"+parent+"' ORDER BY orderno";
				}
				Query query=session.createQuery(sql);
				qp.apply(query);
				return evictList(session,query.list());
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
