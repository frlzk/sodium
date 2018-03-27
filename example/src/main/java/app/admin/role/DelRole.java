package app.admin.role;

import java.util.Iterator;
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
public class DelRole extends BaseAction {

	public List submitForm(ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Iterator it=data.iterator();
				while(it.hasNext()){
					Map user=(Map)it.next();
					String id=(String)user.get("id");
					Query q=session.createQuery("delete from adminRoleAction where role=:id");
					q.setString("id", id);
					q=session.createQuery("delete from adminRole where id=:id");
					q.setString("id", id);
					q.executeUpdate();
				}
				session.flush();
				return null;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
