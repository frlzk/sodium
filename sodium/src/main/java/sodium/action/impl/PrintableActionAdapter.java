package sodium.action.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.xmlform.action.ActionException;
import sodium.action.PrintablePage;

/**
 * @author Liu Zhikun
 */

public class PrintableActionAdapter extends FormActionAdapter  {
	private String format;
	public PrintableActionAdapter(EngineContext engineContext, ActionImpl action, String group,String format) {
		super(engineContext, action, group);
		this.format=format;
	}
	protected List doExecute(ActionContextImpl context,List data)throws ActionException{
		List res=new ArrayList();
		PrintablePage pp=getAction().buildPrintablePage(new PrintContextImpl(context,format),data);
		if(pp!=null)
			res.add(pp);
		return res;
	}
}
