package app.admin.dept;

import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.sf.xmlform.action.ActionException;
import sodium.action.ActionContext;
import sodium.action.ReferenceContext;

@Transactional(rollbackFor=Exception.class)
@Component
public class DeptDictionary extends DeptItems {

	public String getDisplayText(ReferenceContext refCtx,Object bean,final Object key) throws ActionException {
		if(key==null)
			return null;
		List list=query("from adminDept where id=:key","key", key.toString());
		if(list.size()>0){
			Map item=(Map)list.get(0);
			return (String)item.get("fullname");
		}
		return key.toString();
	}
	
}
