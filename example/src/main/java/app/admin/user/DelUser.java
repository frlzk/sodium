package app.admin.user;

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
public class DelUser extends BaseAction {

	public List submitForm(ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Iterator it=data.iterator();
				while(it.hasNext()){
					Map user=(Map)it.next();
					String uid=(String)user.get("id");
					Query q=session.createQuery("update SysUser set status=:status where id=:user");
					q.setString("status", "2");
					q.setString("user", uid);
					q.executeUpdate();
				}
				return null;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
