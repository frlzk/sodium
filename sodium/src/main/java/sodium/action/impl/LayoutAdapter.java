package sodium.action.impl;

import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.action.LayoutAdapteContext;

public interface LayoutAdapter {
	public FormLayout adapte(LayoutAdapteContext context,FormLayout layout);
}
