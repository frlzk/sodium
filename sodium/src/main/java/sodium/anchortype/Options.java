package sodium.anchortype;

import net.sf.xmlform.util.I18NTexts;
import sodium.anchoropt.ArrayOption;
import sodium.anchoropt.ObjectOption;

/**
 * @author Liu Zhikun
 */

final public class Options {
	public static final String SOURCE_NAME="source";
	public static final String SOURCESCOPE_NAME="sourcescope";// all single multiple empty any
	public static final String SOURCE2_NAME="source2";
	public static final String CONSTSOURCE_NAME="constsource";
	public static final String MARK_NAME="mark";
	public static final String RESULT_NAME="result";
	public static final String STYLE_NAME="style";
	public static final String REFRESH_NAME="refresh";
	public static final String CASCADE_NAME="cascade";
	public static final String CONFIRM_NAME="confirm";
	public static final String TRIGGER_NAME="trigger";//no
	public static final String ENABLE_NAME="enable";
	public static final String VFOLLOWE_NAME="vfollowe";// visible follow enable vfe
//	public static final String SHOWMESSAGE_NAME="showmessage";
	
	public static final Option SOURCE=new Option(SOURCE_NAME,ArrayOption.class);
	public static final Option SOURCESCOPE=new Option(SOURCESCOPE_NAME,String.class);
	public static final Option SOURCE2=new Option(SOURCE2_NAME,ArrayOption.class);
	public static final Option CONSTSOURCE=new Option(CONSTSOURCE_NAME,ObjectOption.class);
	public static final Option MARK=new Option(MARK_NAME,String.class);
	public static final Option RESULT=new Option(RESULT_NAME,String.class);
	public static final Option STYLE=new Option(STYLE_NAME,ObjectOption.class);
	public static final Option REFRESH=new Option(REFRESH_NAME,ArrayOption.class);
	public static final Option CASCADE=new Option(CASCADE_NAME,ArrayOption.class);
	public static final Option CONFIRM=new Option(CONFIRM_NAME,I18NTexts.class);
	public static final Option TRIGGER=new Option(TRIGGER_NAME,ArrayOption.class);
	public static final Option ENABLE=new Option(ENABLE_NAME,String.class);
	public static final Option VFOLLOWE=new Option(VFOLLOWE_NAME,String.class);
//	public static final Option SHOWMESSAGE=new Option(SHOWMESSAGE_NAME,String.class);
	public static final Option clone(Option opt,boolean req){
		return new Option(opt.getName(),opt.getValueClass(),req);
	}
}
