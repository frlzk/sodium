package app.admin.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.sf.xmlform.action.ActionException;


import sodium.action.ActionContext;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;

@Component
@Transactional(rollbackFor=Exception.class)
public class UserList  extends BaseAction {
	public long getMinoccurs() {
		return 0;
	}
	public List submitForm(ActionContext context, List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				Query q=session.createQuery("from SysUser order by code");
				return q.list();
		//		List result=new ArrayList();
		//		for(int i=0;i<20;i++){
		//			Map m=new HashMap();
		//			m.put("id", context.getFirstResult()+"id"+i);
		//			m.put("code", context.getFirstResult()+" code"+i);
		//			m.put("name", context.getFirstResult()+" name"+i);
		//			result.add(m);
		//		}
		//		context.setTotalResults(200);
		//		return result;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
