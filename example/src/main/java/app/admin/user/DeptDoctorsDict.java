package app.admin.user;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.action.ActionUtil;
import app.action.Hscb;
import app.hibernate.QueryParams;
import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;

@Component
@Transactional(rollbackFor=Exception.class)
public class DeptDoctorsDict extends UserDictionary {

	public List submitForm(ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				String dept=(String)getSimpleParameter(data, "id");
				if(dept==null)
					return null;
				QueryParams qp=new QueryParams();
				StringBuilder sb=new StringBuilder("SELECT u FROM SysUser u,SysUserDept d WHERE u.id=d.user AND ");
				qp.add(sb, "d.dept", "=", dept);
				Query q=session.createQuery(sb.toString());
				qp.apply(q);
				List list=q.list();
				return list;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
	
}
