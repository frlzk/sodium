package app.admin.dict;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sodium.action.ActionContext;
import net.sf.xmlform.action.ActionException;
import app.action.ActionUtil;
import app.action.BaseAction;
import app.action.Hscb;
import app.hibernate.SaveTemplate;

@Component
@Transactional(rollbackFor=Exception.class)
public class SaveItem extends BaseAction {

	public List submitForm(final ActionContext context,final List data) throws ActionException {
		Hscb cb=new Hscb(){
			public Object exec(Session session) throws ActionException {
				SaveTemplate.saveEntities(context,session, "SysCommonDictionaryItem", data,SaveItem.this, "did");
				context.setResultMessage("字典保存成功");
				getHiberanteTemplate().flush();
				//context.sendEvent("dictionary.Update");
				return null;
			}
		};
		return (List)ActionUtil.exec(this.getHiberanteTemplate(), cb);
	}
	public Map beforeCreateEntity(ActionContext context, Session session, String entityName, Map entity) throws ActionException {
		entity.put("did", UUID.randomUUID().toString().replace("-", ""));
		return entity;
	}
	
}
