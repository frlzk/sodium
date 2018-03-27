package app.user;

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
import app.util.DbUtil;

@Component
@Transactional(rollbackFor=Exception.class)
public class Depts extends BaseAction {

	public List submitForm(ActionContext ctx,final List data)throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				String user=(String)getSimpleParameter(data, "id");
				StringBuilder sb=new StringBuilder("select new map(d.id as id,d.fullname as name) from SysUser u,SysUserDept ud,adminDept d ");
				sb.append("WHERE u.code=:code AND u.id=ud.user AND ud.dept=d.id AND u.status='0'");
				Query query=session.createQuery(sb.toString());
				query.setString("code", user);
				List depts=query.list();
				Map test=new HashMap();
				test.put("id","2ecea54adf6148ada03055d6a3e8daf7");
				test.put("name", "董事会");
				depts.add(test);
				
				test=new HashMap();
				test.put("id","2ecea54adf6148ada03055d6a3e8daf9");
				test.put("name", "会计室");
				depts.add(test);
				return depts;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

}
