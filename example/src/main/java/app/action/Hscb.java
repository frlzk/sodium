package app.action;

import org.hibernate.Session;

import net.sf.xmlform.action.ActionException;

public interface Hscb {
	public Object exec(Session session)throws ActionException;
}
