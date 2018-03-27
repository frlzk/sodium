package app.admin.dept;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;

import net.sf.xmlform.action.ActionException;
import app.action.BaseAction;

@Component
@Transactional(rollbackFor=Exception.class)
public class LoadDept extends BaseAction {

	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Map id=(Map)data.get(0);
		Map dept=(Map)getHiberanteTemplate().get("adminDept", (String)id.get("id"));
		List result=new ArrayList();
		if(dept!=null)
			result.add(dept);
		return result;
	}

}
