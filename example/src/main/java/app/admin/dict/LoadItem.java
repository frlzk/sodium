package app.admin.dict;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;

import net.sf.xmlform.action.ActionException;
import app.action.BaseAction;

@Component
@Transactional(rollbackFor=Exception.class)
public class LoadItem extends BaseAction {
	public List submitForm(ActionContext context,final List data) throws ActionException {
		List list=query("from SysCommonDictionaryItem where group=:group ORDER BY order","group", (String)getSimpleParameter(data, "id"));
		getHiberanteTemplate().clear();
		return list;
	}

}
