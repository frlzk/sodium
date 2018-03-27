package sodium.page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.xmlform.expression.ExpressionJSONFormat;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.form.format.FormJSONFormat;
import net.sf.xmlform.format.JSONConstants;
import net.sf.xmlform.formlayout.LayoutDescriptor;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.format.FormLayoutJSONFormat;
import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sodium.RequestContext;
import sodium.action.Action;
import sodium.action.Anchor;
import sodium.action.impl.ActionImpl;
import sodium.anchoropt.ArrayOption;
import sodium.anchoropt.ObjectOption;
import sodium.anchortype.Options;
import sodium.category.CategorizedAction;
import sodium.category.CategorizedPage;
import sodium.category.impl.CatagoryContextImpl;
import sodium.engine.Configuration;
import sodium.engine.Link;
import sodium.impl.JSONRawString;
import sodium.page.Page;
import sodium.page.BuildJsContext;


/**
 * @author Liu Zhikun
 */


public class PageImpl implements Page{
	private static Logger logger = LoggerFactory.getLogger(PageImpl.class);
	static private final String METHOD_EXECUTE="execute";
	static AtomicLong seq=new AtomicLong();
	private String name;
	//private String args;
	private List anchors=new ArrayList();
	private List selfAnchors=new ArrayList();
	private I18NTexts title=new I18NTexts();
	private String forms;
	private String body="page.define(function(BasePage){alert(\"page: "+name+" not implemention\");return {};});";
	private Object pageObject;
	
	public PageImpl(){
		title.put(new I18NText("page"));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getForms() {
		return forms;
	}

	public void setForms(String forms) {
		this.forms = forms;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		if(body==null||body.length()==0){
			return ;
		}
		this.body = body;
	}

	public Object getPageObject() {
		return pageObject;
	}

	public void setPageObject(Object pageObject) {
		this.pageObject = pageObject;
	}

//	public String getArgs() {
//		return args;
//	}
//
//	public void setArgs(String args) {
//		this.args = args;
//	}

	public I18NTexts getTitle() {
		return title;
	}

	public void setTitle(I18NTexts title) {
		this.title = title;
	}

	public List getAnchors() {
		return anchors;
	}

	public void setAnchors(List anchors) {
		this.anchors = anchors;
	}
	
	public List getSelfAnchors() {
		return selfAnchors;
	}

	public void setSelfAnchors(List anchors) {
		this.selfAnchors = anchors;
	}

	public String buildJsClass(BuildJsContext ctx){
		return buildJsClass(ctx,this);
	}
	static public String buildJsClass(BuildJsContext ctx,PageImpl impl){
		int iniIdx=impl.body.indexOf("onCreateConfig:null");
		if(iniIdx<0){
			iniIdx=impl.body.indexOf("onCreateConfig:function");
			if(iniIdx<0){
				return impl.body;
			}
		}
		String group=ctx.getCategory();
		CatagoryContextImpl memctx=new CatagoryContextImpl(ctx.getRequestContext(),group);
		
		JSONObject exps=new JSONObject();
		JSONArray anchors=new JSONArray();
		Set actionForms=buildActions(ctx,impl,anchors,exps);
		String tempTitl=impl.title.getText(ctx.getRequestContext().getLocale());
		String tit[]=(tempTitl==null?"":tempTitl).split("/");
		String title=tit[tit.length-1];
		if(impl.getPageObject()!=null&&impl.getPageObject() instanceof CategorizedPage){
			title=((CategorizedPage)impl.getPageObject()).adaptPage(memctx, impl).getTitle();
		}
		StringBuilder sb=new StringBuilder();
		String tr="";
		sb.append(impl.body.substring(0, iniIdx));
		sb.append("_getPageCfg:function(){if(this.__pageCfg_){return this.__pageCfg_;}else{this.__pageCfg_={").append(tr);
		sb.append("name:\"").append(impl.name).append("\",").append(tr);
		sb.append("title:\"").append(title).append("\",").append(tr);
		sb.append("anchors:").append(anchors.toString()).append(",").append(tr);
		if(group!=null)
			sb.append("category:\"").append(group).append("\",").append(tr);
		sb.append("expressions:").append(exps.toString()).append(",").append(tr);
		sb.append("forms:").append(buildForms(memctx,ctx,actionForms)).append(",").append(tr);
		sb.append("layouts:").append(buildFormLayouts(memctx,ctx,actionForms)).append(",").append(tr);
		sb.append("attachments:{").append(buildAttachments(ctx)).append("}").append(tr);
		sb.append("};return this.__pageCfg_;}},getPageConfig:function(){return this._getPageCfg();},");
		sb.append(impl.body.substring(iniIdx));
		return sb.toString();
	}
	static private String buildAttachments(final BuildJsContext ctx){
		CreateAttachmentContext cac=new CreateAttachmentContext(){
			public String getJsClassName() {
				return ctx.getName();
			}
			public RequestContext getRequestContext(){
				return ctx.getRequestContext();
			}
		};
		AttachmentCreator ac=ctx.getEngine().getConfiguration().getPageAttachmentCreator();
		if(ac==null)
			return "";
		Map atts=ac.createAttachments(cac);
		if(atts==null)
			return "";
		Iterator it=atts.keySet().iterator();
		StringBuilder sb=new StringBuilder();
		while(it.hasNext()){
			if(sb.length()>0)
				sb.append(",");
			String k=(String)it.next();
			String v=(String)atts.get(k);
			sb.append("\"").append(k).append("\":").append(v);
		}
		return sb.toString();
	}
	static private String buildForms(CatagoryContextImpl memctx,BuildJsContext ctx,Set forms){
		StringBuilder sb=new StringBuilder("{");
		int i=0;
		Set hasForms=new HashSet();
		Iterator it=forms.iterator();
		while(it.hasNext()){
			Object formAction[]=(Object[])it.next();
			String formName=(String)formAction[0];
			if(hasForms.contains(formName)){
				continue;
			}
			hasForms.add(formName);
			ActionImpl am=(ActionImpl)formAction[1];
			XMLForm form=ctx.getEngine().getXmlformPort().getForm(ctx.getRequestContext(), formName);
			if(am!=null){
				CategorizedAction ca=(CategorizedAction)am.getActionInstance();
				form=ca.adaptForm(memctx,am, form);
			}
			try {
				String formJson=new FormJSONFormat(form).getJSONString();
				JSONObject json=new JSONObject(formJson);
				if(json.has(JSONConstants.FAULT_STRING)&&json.getString(JSONConstants.FAULT_STRING).length()>0){
					throw new JSONException(json.getString(JSONConstants.FAULT_STRING));
				}
				if(i>0){
					sb.append(",");
				}
				sb.append("\"").append(formName).append("\":").append(formJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		sb.append("}");
		return sb.toString();
	}
	
	static private String buildFormLayouts(CatagoryContextImpl memctx,BuildJsContext ctx,Set forms) {
		StringBuilder sb=new StringBuilder("{");
		Iterator it=forms.iterator();
		int i=0;
		Set hasForms=new HashSet();
		while(it.hasNext()){
			Object formAction[]=(Object[])it.next();
			String formName=(String)formAction[0];
			if(hasForms.contains(formName)){
				continue;
			}
			hasForms.add(formName);
			ActionImpl am=(ActionImpl)formAction[1];
			LayoutDescriptor des[]=ctx.getEngine().getXmlformLayoutPort().getFormLayouts(ctx.getRequestContext(), formName);
			try {
				if(des.length==0){
					continue;
				}
				if(i>0)
					sb.append(",");
				FormLayout layout=ctx.getEngine().getXmlformLayoutPort().getFormLayout(ctx.getRequestContext(), des[0].getId());
				if(am!=null){
					CategorizedAction ca=(CategorizedAction)am.getActionInstance();
					layout=ca.adaptLayout(memctx,am, layout);
				}
				sb.append("\"").append(formName).append("\":").append(new FormLayoutJSONFormat(ctx.getRequestContext().getLocale(),layout).getJSONString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
		sb.append("}");
		return sb.toString();
	}
	
	static private Set buildActions(BuildJsContext ctx,PageImpl impl,JSONArray actionArray,JSONObject exps){
		Set actionForms=new HashSet();
		String formName[]=new String[0];
		if(impl.forms!=null&&impl.forms.length()>0)
			formName=impl.forms.split(",");
		for(int i=0;i<formName.length;i++)
			actionForms.add(new Object[]{formName[i],null});
		
		Set hasAddActions=new HashSet();
		List actions=ctx.getEngine().getPageLinks(ctx.getRequestContext(),impl.name);
		Iterator it=actions.iterator();
		while(it.hasNext()){
			Link link=(Link)it.next();
			ActionImpl act=(ActionImpl)ctx.getEngine().getAction(link.getAction());
			if(act==null){
				throw new IllegalStateException("Not found action: "+link.getAction());
			}
			if(Link.FROM_ACTION.equals(link.getFrom())){
				addPartnerActionFormName(ctx,actionForms,act,hasAddActions);
				addFormName(actionForms,act);
			}
			if(link.hasPerm()){
				actionArray.put(linkToJson(ctx.getRequestContext().getLocale(),link,act,exps));
			}
		}
		
		return actionForms;
	}
	static private void addFormName(Set names,ActionImpl action){		
		String sourceForm=action.getSourceForm();
		if(sourceForm!=null){
			names.add(new Object[]{sourceForm,action.getActionInstance() instanceof CategorizedAction?action:null});
		}
		String resultForm=action.getResultForm();
		if(resultForm!=null){
			names.add(new Object[]{resultForm,action.getActionInstance() instanceof CategorizedAction?action:null});
		}
	}
	static private void addPartnerActionFormName(BuildJsContext ctx,Set names,Action action,Set hasAddActions){
		if(action.getPartners()==null)
			return;
		String parts[]=action.getPartners();
		for(int i=0;i<parts.length;i++){
			if(parts[i].length()==0)
				continue;
			ActionImpl act=(ActionImpl)ctx.getEngine().getAction(parts[i]);
			if(act==null){
				logger.warn("Action["+action.getName()+"] not found partner: "+parts[i]);
				continue;
			}
			if(hasAddActions.contains(act.getName()))
				continue;
			hasAddActions.add(act.getName());
			if(act!=null){
				addFormName(names,act);
			}
			addPartnerActionFormName(ctx,names,act,hasAddActions);
		}
	}
	static private JSONObject linkToJson(Locale locale,Link link,ActionImpl act,JSONObject exps){
		try {
			JSONObject obj=putObjectToJson(locale,link.getOptions());
			obj.put("label", link.getLabel());
			obj.put("icon", link.getIcon());
			obj.put("page", link.getPage());
			obj.put("order", link.getOrder());
			obj.put("action", link.getAction());
			obj.put("from", link.getFrom());
			obj.put("type", link.getType());
			obj.put("attach", link.getAttach());
			obj.put("minoccurs", act.getMinoccurs());
			obj.put("maxoccurs", act.getMaxoccurs());
			
			String enable=link.getOptions().getString(Options.ENABLE_NAME);
			if(enable!=null)
				exps.put(enable, ExpressionJSONFormat.buildJsonObject(enable));
			
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	static private JSONObject putObjectToJson(Locale locale,ObjectOption opts)throws JSONException{
		JSONObject obj=new JSONObject();
		String names[]=opts.keys();
		for(int i=0;i<names.length;i++){
			Object vo=opts.get(names[i]);
			obj.put(names[i], putObjectToJson(locale,vo));
		}
		return obj;
	}
	static private JSONArray putArrayToJson(Locale locale,ArrayOption arr)throws JSONException{
		JSONArray jsonArr=new JSONArray();
		for(int p=0;p<arr.length();p++){
			Object vo=arr.get(p);
			jsonArr.put(putObjectToJson(locale,vo));
		}
		return jsonArr;
	}
	static private Object putObjectToJson(Locale locale,Object vo)throws JSONException{
		if(vo==null)
			return null;
		if(vo instanceof String||vo instanceof Integer||vo instanceof Boolean){
			return vo;
		}else if(vo instanceof ArrayOption){
			return putArrayToJson(locale,(ArrayOption)vo);
		}else if(vo instanceof ObjectOption){
			return putObjectToJson(locale,(ObjectOption)vo);
		}else if(vo instanceof I18NTexts){
			return ((I18NTexts)vo).getText(locale);
		}else{
			throw new IllegalStateException("Not implements for:"+vo.getClass());
		}
	}
}
