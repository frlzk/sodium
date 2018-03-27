package app.action;

import java.util.Locale;

import sodium.RequestContext;
import sodium.action.Action;

public interface ReqCtx extends RequestContext{
	public boolean isLogin();
	public String getSessionId();
	public String getRemoteAddr();
	public int getRemotePort();
	public Locale getLocale();
	public void setLocale(Locale loc);
	public Object getLocal(String key);
	public void setLocal(String key,Object value);
	public Object getGlobal(String key);
	public void setGlobal(String key,Object value);
}
