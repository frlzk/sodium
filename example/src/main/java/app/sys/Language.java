package app.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import sodium.action.ActionContext;

import net.sf.xmlform.action.ActionException;

import app.action.BaseAction;

@Component
public class Language extends BaseAction {

	public List submitForm(ActionContext ctx, List data)throws ActionException {
		List list=new ArrayList();
		Locale[] ls=Locale.getAvailableLocales();
		for(int i=0;i<ls.length;i++){
			Map lang=new HashMap();
			lang.put("language", ls[i].toString());
			lang.put("text", ls[i].getDisplayName());
			list.add(lang);
			if(i>3)
				break;
		}
		return list;
	}

}
