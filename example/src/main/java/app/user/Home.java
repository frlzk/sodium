package app.user;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.action.BaseAction;
import net.sf.xmlform.action.ActionException;
import sodium.action.ActionContext;

@Component
@Transactional(rollbackFor=Exception.class)
public class Home extends BaseAction {

	public List submitForm(ActionContext ctx,final List data)throws ActionException {
		return null;
	}

}
