package sodium.action.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sodium.action.Action;
import sodium.action.ActionContext;
import sodium.action.PrintContext;
import sodium.action.PrintablePage;
import sodium.action.ReferenceContext;
import sodium.engine.Sampler;
import net.sf.xmlform.action.ActionException;
import net.sf.xmlform.data.SourceType;
import net.sf.xmlform.util.I18NTexts;

/**
 * @author Liu Zhikun
 */

public class ActionImpl implements Action {
	private static Logger logger = LoggerFactory.getLogger(ActionImpl.class);
	private String name,desc,role,partners[]=new String[0],leaders[]=new String[0];
	private I18NTexts label;
	private List anchors=new ArrayList();
	private long maxoccurs=Long.MAX_VALUE,minoccurs=1;
	private String resultForm,sourceForm;
	private Object actionInstance;
	private Method actionMethod;
	private Method textMethod;
	private Method printMethod;
	private Action printAction;
	private String previous[]=new String[0];
	private SourceType sourceType=SourceType.FORM;

	public String getName() {
		return name;
	}
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public I18NTexts getLabel() {
		return label;
	}

	public void setLabel(I18NTexts label) {
		this.label = label;
	}

	public String[] getPartners() {
		return partners;
	}

	public void setPartners(String partners[]) {
		this.partners = partners;
	}

	public String[] getLeaders() {
		return leaders;
	}

	public void setLeaders(String[] leaders) {
		this.leaders = leaders;
	}

//	public String[] getPrevious() {
//		return previous;
//	}
//
//	public void setPrevious(String previous[]) {
//		this.previous = previous;
//	}

	public List getAnchors() {
		return anchors;
	}

	public void setAnchors(List anchors) {
		this.anchors = anchors;
	}
	
	public long getMaxoccurs() {
		return maxoccurs;
	}

	public void setMaxoccurs(long maxoccurs) {
		this.maxoccurs = maxoccurs;
	}

	public long getMinoccurs() {
		return minoccurs;
	}

	public void setMinoccurs(long minoccurs) {
		this.minoccurs = minoccurs;
	}

	public String getResultForm() {
		return resultForm;
	}

	public void setResultForm(String resultForm) {
		this.resultForm = resultForm;
	}

	public String getSourceForm() {
		return sourceForm;
	}

	public void setSourceForm(String sourceForm) {
		this.sourceForm = sourceForm;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public Object getActionInstance() {
		return actionInstance;
	}

	public void setActionInstance(Object actionInstance) {
		this.actionInstance = actionInstance;
	}

	public Method getActionMethod() {
		return actionMethod;
	}

	public void setActionMethod(Method actionMethod) {
		this.actionMethod = actionMethod;
	}
	
	public Method getTextMethod() {
		return textMethod;
	}

	public void setTextMethod(Method textMethod) {
		this.textMethod = textMethod;
	}

	public Method getPrintMethod() {
		return printMethod;
	}

	public void setPrintMethod(Method printMethod) {
		this.printMethod = printMethod;
	}

//	public Action getPrintAction() {
//		return printAction;
//	}
//
//	public void setPrintAction(Action printAction) {
//		this.printAction = printAction;
//	}

	public List execute(ActionContext context,List data)throws ActionException{
		try {
			int sid=Sampler.begin("invokeMethod");
			List result=(List)actionMethod.invoke(actionInstance,context,data);
			Sampler.end(sid);
			return result;
		} catch (IllegalArgumentException e) {
			throw actionException(e);
		} catch (IllegalAccessException e) {
			throw actionException(e);
		} catch (InvocationTargetException e) {
			throw actionException(e);
		}
	}
	
	public String getDisplayText(ReferenceContext refCtx,Object bean,Object key){
		try {
			return (String)textMethod.invoke(actionInstance,refCtx,bean,key);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public PrintablePage buildPrintablePage(PrintContext context,List data)throws ActionException{
		try {
			return (PrintablePage)printMethod.invoke(actionInstance,context,data);
		} catch (IllegalArgumentException e) {
			throw actionException(e);
		} catch (IllegalAccessException e) {
			throw actionException(e);
		} catch (InvocationTargetException e) {
			throw actionException(e);
		}
	}
	
	private ActionException actionException(Exception e){
		Throwable cause = e.getCause();
		logger.error("Execute action", cause);
		if(cause instanceof ActionException){
			return (ActionException)cause;
		}
		String err=cause.getLocalizedMessage();
		return new ActionException(err==null||err.length()==0?cause.getClass().getName():err);
	}
}
