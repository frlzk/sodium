package app.sys;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.web.context.WebApplicationContext;

import sodium.engine.Engine;
import app.action.ActionUtil;
import app.hibernate.QueryParams;
import app.impl.RequestContextImpl;
import app.impl.Role;
import app.util.DbUtil;


public class Initializers implements ApplicationContextAware{
	static String TYPE="type",KEY="key",TEXT="text";
	private SessionFactory sessionFactory;
	private Engine engine;
	private ApplicationContext applicationContext;
	public void setSessionFactory(SessionFactory sessionFactory){
		this.sessionFactory=sessionFactory;
	}
	
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		applicationContext=ctx;
	}
	
	public void init() throws DocumentException{
		HibernateTemplate ht=new HibernateTemplate();
		ht.setSessionFactory(sessionFactory);
		HibernateCallback action=new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				try {
					initRoles(session);
					if(1!=2)
					initDictionary(session);
					initRootUser(session);
					initRootDept(session);
				} catch (DocumentException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		ht.execute(action);
	}
	private void initDictionary(Session session) throws DocumentException{
		System.out.println("Begin initDictionary");
		SAXReader sr = new SAXReader();
		Document dt = sr.read(Initializers.class.getResourceAsStream("data.xml"));
		Iterator it=dt.getRootElement().elementIterator();
		String lastType=null;
		int order=1;
		while(it.hasNext()){
			Element ele=(Element)it.next();
			String type=ele.attributeValue("value");
			if(session.createQuery("from SysCommonDictionary where code='"+type+"'").list().size()>0){
				continue;
			}
			Map group=new HashMap();
			group.put("code", type);
			group.put("name", ele.attributeValue("text"));
			group.put("edit", "true".equals(ele.attributeValue("editable","false"))?"1":"0");
			session.save("SysCommonDictionary", group);
			Iterator iit=ele.elementIterator();
			order=1;
			while(iit.hasNext()){
				Element ie=(Element)iit.next();
				Map item=new HashMap();
				item.put("did", ActionUtil.nextUuid());
				item.put("group", type);
				item.put("key", ie.attributeValue("value"));
				item.put("text", ie.attributeValue("text"));
				item.put("fullText", ie.attributeValue("text"));
				item.put("stop", "0");
				item.put("order", order*100);
				session.save("SysCommonDictionaryItem", item);
				order++;
			}
		}
		System.out.println("End initDictionary");
	}
	private void initRootUser(Session session){
		Query query=session.createQuery("from SysUser where id=:id");
		query.setString("id", RequestContextImpl.ROOT_ID);
		List roots=query.list();
		if(roots.size()>0)
			return;
		Map user=new HashMap();
		user.put("id", RequestContextImpl.ROOT_ID);
		user.put("code", RequestContextImpl.ROOT_NAME);
		user.put("name", "超级用户");
		user.put("password", DbUtil.md5("123456"));
		user.put("status","0");
		session.save("SysUser", user);
	}
	private void initRoles(Session session){
		if(!(applicationContext instanceof WebApplicationContext)){
			return;
		}
		WebApplicationContext wac=(WebApplicationContext)applicationContext;
		Map roles=new HashMap();
		Iterator rit=session.createQuery("from adminRole").list().iterator();
		while(rit.hasNext()){
			Map role=(Map)rit.next();
			Role r=new Role();
			String rid=(String)role.get("id");
			r.setId(rid);
			r.setName((String)role.get("name"));
			roles.put(rid, r);
			Set actions=new HashSet();
			r.setActions(actions);
			StringBuilder sb=new StringBuilder("from adminRoleAction where ");
			QueryParams qp=new QueryParams();
			qp.add(sb,"role", "=", rid);
			sb.append(" and ");
			qp.add(sb,"isbottom", "=", "1");
			Query q=session.createQuery(sb.toString());
			qp.apply(q);
			Iterator ait=q.list().iterator();
			while(ait.hasNext()){
				Map act=(Map)ait.next();
				actions.add(act.get("action"));
			}
		}
		wac.getServletContext().setAttribute(RequestContextImpl.GLOBAL_ROLES, roles);
	}
	private void initRootDept(Session session){
		if(!(applicationContext instanceof WebApplicationContext)){
			return;
		}
		Object[][] depts=new Object[][]{
				new Object[]{"cd8991775f7653f55aada32fb30cbf61","TopDept","TopDept","1","","",1,null,"1","0",1},
				new Object[]{"cd8991775f7653f55aada32fb30cbf62","Senod Dept A","Second Dept A","1","","",2,"cd8991775f7653f55aada32fb30cbf61","1","0",1},
				new Object[]{"cd8991775f7653f55aada32fb30cbf63","Senod Dept B","Second Dept B","1","","",2,"cd8991775f7653f55aada32fb30cbf61","1","0",1},
				new Object[]{"cd8991775f7653f55aada32fb30cbf64","Senod Dept C","Second Dept C","1","","",2,"cd8991775f7653f55aada32fb30cbf61","1","0",1},
				new Object[]{"cd8991775f7653f55aada32fb30cbf65","Senod Dept B1","Second Dept B1","1","","",3,"cd8991775f7653f55aada32fb30cbf63","1","0",1},
				new Object[]{"cd8991775f7653f55aada32fb30cbf66","Senod Dept B2","Second Dept B2","1","","",3,"cd8991775f7653f55aada32fb30cbf63","1","0",1}
		};
		for(int i=0;i<depts.length;i++){
			Object[] p=depts[i];
			Map map=new HashMap();
	       	map.put("id",p[0]);
			map.put("name",p[1]);
			map.put("fullname",p[2]);
			map.put("kind",p[3]);
			map.put("note",p[4]);
			map.put("py",p[5]);
			map.put("level",p[6]);
			map.put("parent",p[7]);
			map.put("dirty",p[8]);
			map.put("isdel",p[9]);
			map.put("orderno",p[10]);
			session.save("adminDept",map);
		}
		
		WebApplicationContext wac=(WebApplicationContext)applicationContext;
		List deptList=session.createQuery("from adminDept where parent is null").list();
		if(deptList.size()>0){
			Map dept=(Map)deptList.get(0);
			wac.getServletContext().setAttribute(RequestContextImpl.ROOT_DEPT, dept.get("id"));
		}
	}
}
