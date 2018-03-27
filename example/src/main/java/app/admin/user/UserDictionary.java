package app.admin.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.xmlform.action.ActionException;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import sodium.action.ReferenceContext;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;

@Component
@Transactional(rollbackFor=Exception.class)
public class UserDictionary extends BaseAction{

	public String getDisplayText(final ReferenceContext refCtx,Object bean,final Object key) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				if(key==null)
					return null;
				session.clear();
				Query q=session.createQuery("from SysUser where id=:key");
				q.setString("key", key.toString());
				List list=q.list();
				if(list.size()>0){
					Map item=(Map)list.get(0);
					return (String)item.get("name");
				}
				return key.toString();
			}
		};
		return (String)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}

	public List submitForm(ActionContext context, List data)throws ActionException {
		return new ArrayList();
	}
	
}
