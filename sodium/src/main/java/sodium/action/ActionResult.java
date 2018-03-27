package sodium.action;

import java.util.List;

import net.sf.xmlform.form.XMLForm;

/**
 * @author Liu Zhikun
 */

public interface ActionResult {
	public XMLForm getForm();
	public List getResult();
}
