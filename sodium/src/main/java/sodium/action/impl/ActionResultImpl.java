package sodium.action.impl;

import java.util.List;

import sodium.action.ActionResult;

import net.sf.xmlform.form.XMLForm;

/**
 * @author Liu Zhikun
 */

public class ActionResultImpl implements ActionResult {
	private XMLForm xmlform;
	private List result;
	
	public ActionResultImpl(XMLForm xmlform, List result) {
		super();
		this.xmlform = xmlform;
		this.result = result;
	}
	public XMLForm getForm() {
		return xmlform;
	}
	public List getResult() {
		return result;
	}
	
}
