package sodium.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.xmlform.expression.ExpressionContext;
import net.sf.xmlform.expression.Factor;
import net.sf.xmlform.expression.NullValue;
import net.sf.xmlform.expression.NumericValue;
import net.sf.xmlform.expression.StrValue;
import net.sf.xmlform.expression.Value;
import net.sf.xmlform.expression.fun.Fun;
import net.sf.xmlform.expression.fun.FunHelper;
import sodium.RequestContext;
import sodium.engine.SessionAttributes;

public class UserFunction implements Fun {
	final public static String NAME="user";
	private SessionAttributes sessionAttributes;
	public UserFunction(SessionAttributes sessionAttributes){
		this.sessionAttributes=sessionAttributes;
	}
	public String getName() {
		return NAME;
	}
	
	public Value execute(ExpressionContext context, Factor factors[]) {
		FunHelper.checkArgumentSize(NAME, factors, 1);
		Value values[]=FunHelper.evalFactorsToValues(context, factors);
		String name=values[0].toString();
		Object v=sessionAttributes.getValue((RequestContext)context.getPastport(),name);
		if(v==null)
			return NullValue.NULL_VALUE;
		if(isStr(v)){
			return new StrValue(v.toString());
		}
		return new NumericValue(new BigDecimal(v.toString()));
	}
	private boolean isStr(Object v){
		if(v instanceof Long || v instanceof Short|| v instanceof Integer|| v instanceof Byte){
			return false;
		}
		return true;
	}
}
