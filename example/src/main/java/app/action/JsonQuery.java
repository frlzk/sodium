package app.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.form.Form;
import net.sf.xmlform.type.BaseTypeProvider;
import net.sf.xmlform.type.BaseTypes;
import net.sf.xmlform.type.StringType;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sodium.action.ActionContext;
import app.hibernate.QueryParams;
import app.impl.RequestContextImpl;
import app.user.Login;


public class JsonQuery {
	final static String BIG_DEPT_ID="bigDeptId";
	final static String BIG_GRID_ID="bigGridId";
	private JSONObject sysJson,userJson;
	QueryParams qp=new QueryParams();
	Map extraField=new HashMap();
	Map extraFieldType=new HashMap();
	String rootDept=null;
	String rootGrid=null;
	boolean useDept=false;
	boolean useGrid=false;
	String where=null;
	String[][] jsonFields;
	String jsonTableNames[];
	Form jsonForm[];
	public JsonQuery(){
		this(new ArrayList(),null,null);
	}
	public JsonQuery(List jsons,Form form,String tableName){
		this(jsons,new String[][]{buildFields(form)},new String[]{tableName},new Form[]{form});
	}
	public JsonQuery(List jsons,String[][] fields,String tableNames[],Form form[]){
		initDefault();
		if(jsons.size()>0){
			Map json=(Map)jsons.get(0);
			parseJson((String)json.get("sys"),(String)json.get("user"));
			jsonFields=fields;
			jsonTableNames=tableNames;
			jsonForm=form;
		}
	}
	public JsonQuery(String sysJson,String userJson){
		initDefault();
		parseJson(sysJson,userJson);
	}
	public void setWorkingDept(ActionContext ctx){
		this.useDept=!useDept;
		List ds=(List)((ReqCtx)ctx.getRequestContext()).getLocal(Login.WORKING_DEPT);
		if(ds!=null&&ds.size()>0){
			rootDept=(String)ds.get(0);
		}else{
			rootDept=(String)((ReqCtx)ctx.getRequestContext()).getGlobal(RequestContextImpl.ROOT_DEPT);
		}
	}
	private void initDefault(){
		extraField.put("bigDeptId", "sdl.bigId");
		extraFieldType.put("bigDeptId", StringType.NAME);
	}
	static public Form getQueryForm(ActionContext ctx){
		return ctx.getResultForm().getRootForm();
	}
	private void parseJson(String sysJson,String userJson){
		try {
			if(sysJson!=null)
				this.sysJson=new JSONObject(sysJson);
			if(userJson!=null)
				this.userJson=new JSONObject(userJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public String getJsonCriteria(String pre,String sub) throws ActionException{
		if(where==null)
			where=buildSqlWhere(jsonFields,jsonTableNames,jsonForm);
		if(where.length()==0)
			return "";
		StringBuilder sb=new StringBuilder();
		if(pre!=null)
			sb.append(" ").append(pre).append(" ");
		sb.append("(").append(where).append(")");
		if(sub!=null)
			sb.append(" ").append(sub).append(" ");
		return sb.toString();
	}
	public void addField(String name,String fieldName,String type){
		extraField.put(name, fieldName);
		extraFieldType.put(name, StringType.NAME);
	}
	public String addParameters(String field,String op,Object value){
		return qp.add(field, op, value);
	}
	public void setParameters(Query query){
		qp.apply(query);
		//System.out.println(qp.toString());
	}
	public Query createPagingQuery(ActionContext ctx,Session session,String sql){
		int fromIdx=sql.indexOf(" FROM ");
		if(fromIdx>0){
			Query totalQuery=session.createQuery("SELECT count(*) "+sql.substring(fromIdx));
			setParameters(totalQuery);
			List list=totalQuery.list();
			long size=(Long)list.get(0);
			ctx.setTotalResults(size);
		}
		Query query=session.createQuery(sql);
		query.setFirstResult(ctx.getFirstResult());
		query.setMaxResults(ctx.getMaxResults());
		return query;
	}
	static public String[] buildFields(Form form){
		String fiekds[]=new String[form.getFields().size()];
		return (String[])form.getFields().keySet().toArray(fiekds);
	}
	private String buildSqlWhere(String[][] fields,String tableNames[],Form form[]) throws ActionException{
		Map tabs=new HashMap();
		Map forms=new HashMap();
		for(int i=0;i<tableNames.length;i++){
			for(int j=0;j<fields[i].length;j++){
				tabs.put(fields[i][j], tableNames[i]);
				forms.put(fields[i][j], form[i]);
			}
		}
		return buildSqlWhere(forms,tabs);
	}
	private String buildSqlWhere(Map formMap,Map tabNames) throws ActionException{
		StringBuilder sb=new StringBuilder();
		String sys=null;
		String user=null;
		try {
			if(sysJson!=null)
				sys = buildSqlWhere(formMap,tabNames,sysJson);
			if(userJson!=null)
				user=buildSqlWhere(formMap,tabNames,userJson);
		} catch (JSONException e) {
			e.printStackTrace();
			return sb.toString();
		}
		if(sys.length()>0){
			sb.append(" (").append(sys).append(") ");
		}
		if(user.length()>0){
			if(sb.length()>0)
				sb.append(" AND ");
			sb.append(" (").append(user).append(") ");
		}
		
//		int dsize=depts.size();
//		if(useDept==false&&dsize>0){
//			if(sb.length()>0)
//				sb.append(" AND ");
//			if(dsize==1){
//				qp.add(sb,(String)extraField.get("bigDeptId"), "=", depts.get(0));
//			}else{
//				qp.add(sb,(String)extraField.get("bigDeptId"), QueryParams.IN, depts);
//			}
//		}
		if(useDept==false&&rootDept!=null){
			if(sb.length()>0)
				sb.append(" AND ");
			qp.add(sb,(String)extraField.get("bigDeptId"), "=", rootDept);
		}
		if(useGrid==false&&rootGrid!=null){
			if(sb.length()>0)
				sb.append(" AND ");
			qp.add(sb,(String)extraField.get("bigGridId"), "=", rootGrid);
		}
		
		return sb.toString();
	}
	private String buildSqlWhere(Map formMap,Map tabNames,JSONObject part) throws JSONException, ActionException{
		String group=null;
		if(part.has("group")){
			group=part.getString("group");
		}
		if("and,or,not".indexOf(group)<0){
			group="and";
		}
		group=" "+group+" ";
		if(!part.has("items")){
			return null;
		}
		StringBuilder sb=new StringBuilder();
		JSONArray items=part.getJSONArray("items");
		for(int i=0;i<items.length();i++){
			JSONObject item=(JSONObject)items.getJSONObject(i);
			String childGroup=getJsonField(item,"group");
			String field=getJsonField(item,"field");
			String op=getJsonField(item,"op");
			String value=getJsonField(item,"value");
			if(field!=null&&op!=null&&value!=null){
				String col=null;
				String type=null;
				type=(String)extraFieldType.get(field);
				if(type!=null){
					col=(String)extraField.get(field);
					if(BIG_DEPT_ID.equals(field)){
						buildDeptValue(sb,col,value);
					}else if(BIG_GRID_ID.equals(field)){
						useGrid=true;
						qp.add(sb,col, op, buildSqlValue(type,value));
					}else{
						qp.add(sb,col, op, buildSqlValue(type,value));
					}
					continue;
				}
				Form form=(Form)formMap.get(field);
				if(form!=null){
					Field f=(Field)form.getFields().get(field);
					type=f.getType();
					col=(String)tabNames.get(field)+"."+field;
					if(sb.length()>0)
						sb.append(group);
					qp.add(sb,col, op, buildSqlValue(type,value));
					continue;
				}
				throw new ActionException("没有数据项定义: "+field);
			}else if(childGroup!=null){
				if(sb.length()>0)
					sb.append(group);
				sb.append(" ").append(buildSqlWhere(formMap,tabNames,item)).append(" ");
			}
		}
		return sb.toString();
	}
	private void buildDeptValue(StringBuilder sb,String col,String value){
		if(1==1){
			qp.add(sb,col, "=", value);
			return;
		}
	}
	private String getJsonField(JSONObject obj,String key) throws JSONException{
		String value=null;
		if(obj.has(key)){
			Object val=obj.get(key);
			if(val instanceof JSONArray){
				JSONArray ar=(JSONArray)val;
				if(ar.length()>0)
					value=ar.getString(0);
			}else
				value=obj.getString(key);
		}
		return value;
	}
	private Object buildSqlValue(String type,String value) throws ActionException{
		Object valObj=null;
		try{
			valObj=BaseTypes.getTypeByName(type).stringToObject(value);
		}catch(Exception e){
			e.printStackTrace();
			throw new ActionException("数据格式不正确!");
		}
		return valObj;
	}
	public String toString() {
		return qp.toString();
	}
	
}