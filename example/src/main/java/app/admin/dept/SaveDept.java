package app.admin.dept;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.data.Status;

import app.util.DbUtil;
import app.action.BaseAction;
import app.hibernate.QueryParams;

@Component
@Transactional(rollbackFor=Exception.class)
public class SaveDept extends BaseAction  {
	@Autowired
	private LoadDept loadDept;
	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Map dept=(Map)data.get(0);
		
		if(context.getSourceInfo(dept).getStatus()==Status.NEW){
			String parentId=(String)dept.get("parent");
			String id=DbUtil.nextUuid();
			dept.put("id", id);
			dept.put("fullname",dept.get("name"));
			dept.put("level", getLevel(parentId));
			dept.put("dirty", "1");
			dept.put("isdel", "0");
			getHiberanteTemplate().save("adminDept", dept);
			insertRel(parentId,id);
		}else{
			String id=(String)dept.get("id");
			Map oldDept=(Map)getHiberanteTemplate().get("adminDept", id);
			String oldName=(String)oldDept.get("name");
			QueryParams.mergeMap(context.getSourceInfo(dept), dept, oldDept);
			String newName=(String)oldDept.get("name");
			oldDept.put("fullname",newName);
			if(!oldName.equals(newName)){
				oldDept.put("dirty", "1");
				String code=(String)oldDept.get("code");
				getHiberanteTemplate().bulkUpdate("update adminDept set dirty='1' where id in(select cid from adminDeptRel where pid='"+id+"')");
			}
			getHiberanteTemplate().update("adminDept", oldDept);
		}
		
		getHiberanteTemplate().flush();
		context.setResultMessage("保存成功");
		return loadDept.submitForm(context, Arrays.asList(dept));
	}
	private void insertRel(String par,String id){
		if(par==null||id==null||id.length()==0||par.length()==0)
			return;
		Map area=(Map)getHiberanteTemplate().get("adminDept", par);
		if(area==null)
			return;
		Map re=new HashMap();
		re.put("pid", par);
		re.put("cid", id);
		re.put("level", area.get("level"));
		getHiberanteTemplate().save("adminDeptRel", re);
		
		String pid=(String)area.get("parent");
		if(pid==null||pid.length()==0)
			return;
		insertRel(pid,id);
	}
	private Integer getLevel(String parentId){
		Integer plevel=1;
		if(parentId!=null){
			Map parent=(Map)getHiberanteTemplate().get("adminDept", parentId);
			plevel=(Integer)parent.get("level")+1;
		}
		return plevel;
	}
}
