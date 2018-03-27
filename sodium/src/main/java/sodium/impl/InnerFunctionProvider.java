package sodium.impl;

import net.sf.xmlform.expression.Value;
import net.sf.xmlform.expression.fun.Fun;
import net.sf.xmlform.expression.fun.FunctionProvider;
import sodium.RequestContext;
import sodium.engine.SessionAttributes;

public class InnerFunctionProvider implements FunctionProvider {
	private UserFunction uf;
	private FunctionProvider fp;
	public InnerFunctionProvider(FunctionProvider fp,SessionAttributes sessionAttributes){
		this.fp=fp;
		uf=new UserFunction(sessionAttributes);
	}
	public Fun getFunction(String name) {
		if(UserFunction.NAME.equals(name))
			return uf;
		if(fp==null)
			return null;
		return fp.getFunction(name);
	}

}
