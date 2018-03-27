package sodium.action.impl;

import net.sf.xmlform.form.XMLForm;
import sodium.action.FormAdapteContext;

public interface FormAdapter {
	public XMLForm adapte(FormAdapteContext context,XMLForm form);
}
