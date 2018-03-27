package app.admin.dict;

import java.util.List;

import net.sf.xmlform.action.ActionException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import app.action.BaseAction;

@Component
@Transactional(rollbackFor=Exception.class)
public class DictGroups extends BaseAction {
	public List submitForm(ActionContext context, List data) throws ActionException {
		List list=query("from SysCommonDictionary where edit='1' ORDER BY name");
		return list;
	}

}
