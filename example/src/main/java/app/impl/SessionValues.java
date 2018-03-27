package app.impl;


import app.action.ReqCtx;
import net.sf.xmlform.expression.StrValue;
import net.sf.xmlform.expression.Value;
import sodium.RequestContext;
import sodium.engine.SessionAttributes;

public class SessionValues implements SessionAttributes {

	public String[] getNames(RequestContext reqCtx) {
		return new String[]{"id","name"};
	}

	public Object getValue(RequestContext reqCtx, String key) {
		ReqCtx rtx=(ReqCtx)reqCtx;
		return (String)rtx.getLocal("user-"+key);
	}

}
