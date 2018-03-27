package sodium.anchortype;

import sodium.anchoropt.ObjectOption;

/**
 * @author Liu Zhikun
 */

public class Option {
	private String name;
	private Class valueClass;
	private boolean required=false;
	public Option(String name,Class valueClass){
		OptionParser.checkSupport(valueClass);
		this.name=name;
		this.valueClass=valueClass;
	}
	public Option(String name,Class valueClass,boolean req){
		OptionParser.checkSupport(valueClass);
		this.name=name;
		this.valueClass=valueClass;
		required=req;
	}
	public String getName() {
		return name;
	}
	public Class getValueClass() {
		return valueClass;
	}
	public boolean isRequired() {
		return required;
	}
	public Object parseOpt(String str){
		return OptionParser.parseOpt(valueClass,str);
	}
	
}
