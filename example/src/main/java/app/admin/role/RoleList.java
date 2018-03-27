package app.admin.role;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;

import net.sf.xmlform.action.ActionException;

import app.action.JsonQuery;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;

@Component
@Transactional(rollbackFor=Exception.class)
public class RoleList extends BaseAction {

	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				JsonQuery jq=new JsonQuery(data,JsonQuery.getQueryForm(context), "r");
				StringBuilder sb=new StringBuilder(" FROM adminRole r ");
				//sb.append(jq.getJsonCriteria("where ", null));
				sb.append(" order by name");
				Query q=session.createQuery(sb.toString());
				jq.setParameters(q);
				List result=q.list();
				return result;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
