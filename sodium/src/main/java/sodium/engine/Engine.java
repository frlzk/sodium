package sodium.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import sodium.RequestContext;
import sodium.action.Action;
import sodium.action.Anchor;
import sodium.action.PrintablePage;
import sodium.action.impl.ActionImpl;
import sodium.action.impl.EngineContext;
import sodium.action.impl.FormActionAdapter;
import sodium.action.impl.PrintableActionAdapter;
import sodium.action.impl.ReferenceContextImpl;
import sodium.category.CategorizedAction;
import sodium.category.CategorizedName;
import sodium.category.CategoryUtil;
import sodium.category.ForkedAction;
import sodium.category.ForkedAnchor;
import sodium.category.AdaptedAnchor;
import sodium.category.CategorizedPage;
import sodium.category.impl.CatagoryContextImpl;
import sodium.impl.ActionActionDesc;
import sodium.impl.ActionDesc;
import sodium.impl.ActionNodeImpl;
import sodium.impl.CheckActionImpl;
import sodium.impl.ForkedActionDesc;
import sodium.impl.InnerFunctionProvider;
import sodium.impl.LinkImpl;
import sodium.impl.LogSamplingRecorder;
import sodium.impl.MenuNodeImpl;
import sodium.impl.XmlformLayoutPort;
import sodium.impl.XmlformPort;
import sodium.page.JsClass;
import sodium.page.Page;
import sodium.print.PrinterProvider;
import sodium.print.impl.DefaultPrinterProvider;
import sodium.print.renderer.QueryReportBuilder;
import sodium.util.Util;
import net.sf.jasperreports.engine.JRException;
import net.sf.xmlform.XMLFormException;
import net.sf.xmlform.XMLFormPort;
import net.sf.xmlform.data.Attachment;
import net.sf.xmlform.data.DataSource;
import net.sf.xmlform.data.ResultData;
import net.sf.xmlform.data.source.JSONDataSource;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.form.Form;
import net.sf.xmlform.form.Reference;
import net.sf.xmlform.form.Subform;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.format.JSONConstants;
import net.sf.xmlform.formlayout.LayoutDescriptor;
import net.sf.xmlform.formlayout.XMLFormLayoutPort;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.util.MessageUtil;

/**
 * @author Liu Zhikun
 */

public class Engine implements ApplicationContextAware {
	final static public int ACCESS_OK=0,ACCESS_NOLOGIN=1,ACCESS_DENIED=2;
	final static public String MAIN_PAGE="main";
//	private String wsServiceNamespace="http://example.com";
//	private WSServiceImpl wsServiceImpl=new WSServiceImpl();
//	private FunctionProvider functionProvider;
	private Setting setting=null;
	private Configuration configuration;
	private ApplicationContext applicationContext;
	private EngineContext engineContext;
	private XMLFormPort xmlformPort=null;
	private XMLFormLayoutPort xmlformLayoutPort=null;
	private QueryReportBuilder queryReportBuilder;
	private static Logger logger = LoggerFactory.getLogger(Engine.class);
	private AccessController accessController;
	private PrinterProvider printerProvider;
	private SessionAttributes sessionAttributes;
	private Map pageParts=new ConcurrentHashMap();
	static private String SORT_PAD="0000000000000000000000000000000000000000000000000000".substring(0, (""+Integer.MAX_VALUE).length());
	static private int SORT_LEN=(""+Integer.MAX_VALUE).length();

//	public FunctionProvider getFunctionProvider() {
//		return functionProvider;
//	}
//
//	public void setFunctionProvider(FunctionProvider functionProvider) {
//		this.functionProvider = functionProvider;
//	}

	public PrinterProvider getPrinterProvider() {
		return printerProvider;
	}

	public void setPrinterProvider(PrinterProvider printerProvider) {
		this.printerProvider = printerProvider;
	}

	public XMLFormPort getXmlformPort() {
		return xmlformPort;
	}

	public void setXmlformPort(XMLFormPort xmlformPort) {
		this.xmlformPort = xmlformPort;
	}

	public XMLFormLayoutPort getXmlformLayoutPort() {
		return xmlformLayoutPort;
	}

	public void setXmlformLayoutPort(XMLFormLayoutPort xmlformLayoutPort) {
		this.xmlformLayoutPort = xmlformLayoutPort;
	}
	
	public AccessController getAccessController() {
		return accessController;
	}

	public void setAccessController(AccessController accessController) {
		this.accessController = accessController;
	}
	
	public SessionAttributes getSessionAttributes() {
		return sessionAttributes;
	}

	public void setSessionAttributes(SessionAttributes sessionAttributes) {
		this.sessionAttributes = sessionAttributes;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		if(this.printerProvider==null){
			DefaultPrinterProvider pp=new DefaultPrinterProvider();
			pp.setConfiguration(configuration);
			printerProvider=pp;
		}
	}

	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		WebApplicationContext wac=(WebApplicationContext)ac;
		applicationContext=ac;
		Map srm=ac.getBeansOfType(SamplingRecorder.class);
		if(srm.size()==0){
			LogSamplingRecorder lsr=new LogSamplingRecorder();
			lsr.setSetting(setting);
			Sampler.setSamplingRecorder(lsr);
		}else{
			Sampler.setSamplingRecorder((SamplingRecorder)srm.values().iterator().next());
		}
	}
	
//	public String getNamespace(){
//		return wsServiceNamespace;
//	}
//	
//	public void setNamespace(String ns){
//		wsServiceNamespace=ns;
//	}

	public Setting getSetting() {
		return setting;
	}

	public void setSetting(Setting setting) {
		this.setting = setting;
		Sampler.setSetting(setting);
	}
	
	public void init(){
		if(xmlformPort==null){
			XmlformPort formPort=new XmlformPort();
			formPort.setConfiguration(configuration);
			formPort.setConfigurationProvider(configuration.getXmlformConfigurationProvider());
			formPort.setSetting(setting);
			formPort.setFunctionProvider(new InnerFunctionProvider(null,sessionAttributes));
			this.setXmlformPort(formPort);
		}
		if(xmlformLayoutPort==null){
			XmlformLayoutPort layoutPort=new XmlformLayoutPort();
			layoutPort.setConfiguration(configuration);
			layoutPort.setLayoutProvider(configuration.getXmlformLayoutProvider());
			xmlformLayoutPort=layoutPort;
		}
		
//		wsServiceImpl.setNamespace(wsServiceNamespace);
//		wsServiceImpl.setXmlformPort(xmlformPort);
		
		queryReportBuilder=new QueryReportBuilder();
		engineContext=new EngineContext(this,xmlformPort,xmlformLayoutPort);
	}
	
	public void refresh(){
		configuration.refresh();
	}
	public void tryRefresh(){
		if(!setting.isDebug())
			return;
		int sid=Sampler.begin("refreshConfig");
		refresh();
		Sampler.end(sid);
	}

	public Action getAction(String name){
		return (Action)configuration.getActions().get(name);
	}
	public List getActionsByType(Class clazz){
		List result=new ArrayList();
		Map actionInstances=configuration.getActions();
		Iterator it=actionInstances.values().iterator();
		while(it.hasNext()){
			Action a=(Action)it.next();
			if(clazz.isInstance(a)){
				result.add(a);
			}
		}
		return result;
	}
	public JsClass getJsClass(String pageName){
		CategorizedName catName=CategoryUtil.parseCategoryName(pageName);
		String relName=catName.getName();
		return (JsClass)configuration.getJsClasses().get(relName);
	}
//	public String executeAction(final RequestContext context,final String actionName,final String data){
//		if ("reference".equals(actionName)) {
//			try {
//				JSONArray array = new JSONArray(data);
//				for (int i = 0; i < array.length(); i++) {
//					JSONObject unit = array.getJSONObject(i);
//					String result = (String) doExecuteAction(context,unit.getString("action"), unit.getJSONObject("data").toString());
//					unit.put("data", new JSONObject(result));
//				}
//				return array.toString();
//			} catch (JSONException e) {
//				e.printStackTrace();
//				return e.getLocalizedMessage();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return e.getLocalizedMessage();
//			}
//		} else {
//			return doExecuteAction(context, actionName, data);
//		}
//	}
//	private String doExecuteAction(final RequestContext context,final String actionName,final String data){
//		final CategorizedName catName=CategoryUtil.parseCategoryName(actionName);
//		final ActionImpl action =(ActionImpl)getAction(catName.getName());
//		if(action==null){
//			return DataJSONFormat.buildError(XMLFormException.CE_ACTION_UNKNOWN, "Unknown action: "+ actionName);
//		}
//		if(action.getActionInstance() instanceof CategorizedAction && catName.getCategory()==null){
//			return DataJSONFormat.buildError(XMLFormException.CE_ACTION_PERMISSION, "Must provide action category: "+ actionName);
//		}
//		try{
//			int asid=Sampler.begin(actionName);
//			int esid=Sampler.begin("execute");
//			final DataResult result=doExecuteAction(context,catName.getCategory(),actionName,action,data);
//			Sampler.end(esid);
//			int rsid=Sampler.begin("toJson");
//			DataJSONFormat jsonResult = new DataJSONFormat(result);
//			String json= jsonResult.getJSONString().toString();
//			Sampler.end(rsid);
//			Sampler.end(asid);
//			return json;
//		}catch(XMLFormException e){
//			DataJSONFormat jsonResult = new DataJSONFormat(e);
//			return jsonResult.getJSONString().toString();
//		}catch(Exception e){
//			return DataJSONFormat.buildError(XMLFormException.SE_APPLICATION,e.getLocalizedMessage());
//		}
//	}
//	private DataResult doExecuteAction(RequestContext context, String category,String actionName,ActionImpl action, String data) {
//		checkPermission(context,actionName, action);
//		FormActionAdapter sfa = new FormActionAdapter(engineContext,action,category,true);
//		DataResult dr = getXmlformPort().submit(context, sfa, new InnerJSONDataSource(data));
//		return dr;
//	}
	public ResultData executeAction(final RequestContext context,final String actionName,final DataSource data){
		final CategorizedName catName=CategoryUtil.parseCategoryName(actionName);
		final ActionImpl action =(ActionImpl)getAction(catName.getName());
		if(action==null){
			throw new XMLFormException(XMLFormException.CE_ACTION_UNKNOWN, "Unknown action: "+ actionName);
		}
		if(action.getActionInstance() instanceof CategorizedAction && catName.getCategory()==null){
			throw new XMLFormException(XMLFormException.CE_ACTION_PERMISSION, "Must provide action category: "+ actionName);
		}
		try{
			int esid=Sampler.begin("execute");
			checkPermission(context,actionName, action);
			FormActionAdapter sfa = new FormActionAdapter(engineContext,action,catName.getCategory());
			ResultData result = getXmlformPort().submit(context, sfa, data);
			Sampler.end(esid);
			return result;
		}catch(XMLFormException e){
			throw e;
		}catch(Exception e){
			throw new XMLFormException(XMLFormException.SE_APPLICATION,e.getLocalizedMessage());
		}
	}
	public void resolveReference(RequestContext context,XMLForm form,List list){
		if(form==null||list==null||list.size()==0)
			return;
		int rsid=Sampler.begin("resolveReference");
		resolveReference(context,form,form.getRootForm(),null,list);
		Sampler.end(rsid);
	}
	private void resolveReference(RequestContext context,XMLForm xmlform,Form form,Object parent,List list){
		if(list==null||form==null)
			return;
		List refFields=new ArrayList();
		Iterator it=form.getFields().values().iterator();
		while(it.hasNext()){
			Field field=(Field)it.next();
			if(field.getReference()!=null||field.getTextfield()!=null){
				Object ref=field.getReference()!=null?(createReferenceContextImpl(context,parent,field.getReference())):null;
				refFields.add(new Object[]{field,ref});
			}
		}
		for(int i=0;i<list.size();i++){
			Map bean=(Map)list.get(i);
			if(bean==null)
				continue;
			Map oldBean=new HashMap(bean);
			for(int f=0;f<refFields.size();f++){
				Object fs[]=(Object[])refFields.get(f);
				Field field=(Field)fs[0];
				if(field.getTextfield()!=null){
					bean.put(field.getName(), new Object[]{oldBean.get(field.getName()),oldBean.get(field.getTextfield())});
				}else{
					Reference ref = field.getReference();
					ReferenceContextImpl refCtx=(ReferenceContextImpl)fs[1];
					ActionImpl dict=(ActionImpl)getAction(ref.getAction());
					if(dict==null){
						throw new IllegalArgumentException("Not found dictionary action: "+ref.getAction());
					}
					if(dict.getTextMethod()==null){
						throw new IllegalArgumentException("Not set textMethod for action: "+ref.getAction());
					}
					Object value=oldBean.get(field.getName());
					bean.put(field.getName(), new Object[]{value,dict.getDisplayText(refCtx,oldBean,value)});
				}
			}
			Iterator sit=form.getSubforms().values().iterator();
			while(sit.hasNext()){
				Subform sf=(Subform)sit.next();
				List items=(List)bean.get(sf.getName());
				resolveReference(context,xmlform,(Form)xmlform.getForms().get(sf.getForm()),oldBean,items);
			}
		}
	}
	private ReferenceContextImpl createReferenceContextImpl(RequestContext context,Object parent,Reference ref){
		ReferenceContextImpl rci=new ReferenceContextImpl(context,ref);
		rci.setParent(parent);
		return rci;
	}
	public PrintablePage buildPrintablePage(final RequestContext context,final String data){
		JSONObject param=null;
		String an=null;
		try {
			param = new JSONObject(data);
			an=param.getString("action");
		} catch (JSONException e1) {
			e1.printStackTrace();
			throw new IllegalArgumentException(e1.getLocalizedMessage());
		}
		final JSONObject params=param;
		final String actionName=an;
		final CategorizedName catName=CategoryUtil.parseCategoryName(actionName);
		final ActionImpl action =(ActionImpl) getAction(catName.getName());
		if(action==null){
			return null;
		}
		try{
			checkPermission(context, actionName, action);
			return buildPrintablePage2(context,catName,actionName,action,params);
		}catch(XMLFormException e){
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}catch(Exception e){
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
	}
	private PrintablePage buildPrintablePage2(RequestContext context,CategorizedName catName,String actionName,ActionImpl action, JSONObject param) throws JSONException,IOException, JRException {
		int pageSize=20;
		String format="html";
		if(param.has("format"))
			format=param.getString("format");
		if(param.has("pagesize")){
			pageSize=Integer.parseInt(param.getString("pagesize"));
		}
		if (action.getPrintMethod()!=null )  {
			List printPages = buildPrintablePage3(context, action,catName.getCategory(),format, param.getString("data"));
			if(printPages.size()>0)
				return (PrintablePage)printPages.get(0);
		} else{
			FormActionAdapter sfa = new FormActionAdapter(engineContext,action,catName.getCategory());
			ResultData dr = getXmlformPort().submit(context, sfa, new JSONDataSource(param.getString("data")));
			resolveReference(context, dr.getForm(), dr.getData());
			if(dr.getForm()!=null){
				FormLayout layout=null;
				Attachment att=dr.getAttachments().getAttachment(JSONConstants.ATTACHMENT_FORMLAYOUT);
				if(att==null){
					LayoutDescriptor des[]=xmlformLayoutPort.getFormLayouts(context, dr.getForm().getName());
					if(des==null||des.length==0){
						throw new JRException("Not found form layout: "+dr.getForm().getName());
					}
					layout=xmlformLayoutPort.getFormLayout(context, des[0].getId());
				}else{
					layout=(FormLayout)att.getData();
				}
				return queryReportBuilder.buildPrintablePage(this,context,dr.getForm(),layout, param.getString("title"),format,pageSize,dr.getData());
			}
		}
		return null;
	}

	private List buildPrintablePage3(RequestContext context,ActionImpl action,String group,String format,String data) {
		FormActionAdapter ppa = new PrintableActionAdapter(engineContext,action,group,format);
		ResultData dr = getXmlformPort().submit(context, ppa, new JSONDataSource(data));
		return dr.getData();
	}
	private void checkPermission(RequestContext context,String actionName, Action action) {
		if (action == null)
			throw new XMLFormException(
					XMLFormException.CE_ACTION_UNKNOWN, MessageUtil.getMessage(context.getLocale(), Configuration.class, "perm.not_found_action")+": "+ actionName);
		Permission chk=checkAccess(context,actionName,action);
		if (chk==Permission.NOLOGIN) {
			throw new XMLFormException(XMLFormException.CE_SESSSION, MessageUtil.getMessage(context.getLocale(), Configuration.class, "perm.not_login")+": " + actionName);
		}
		if (chk==Permission.DENIED) {
			throw new XMLFormException(
					XMLFormException.CE_ACTION_PERMISSION, MessageUtil.getMessage(context.getLocale(), Configuration.class, "perm.not_have_permission")+": " + actionName);
		}
	}
	public List getPageLinks(RequestContext ctx,String pageName){
		List links=getPageLinks2(ctx,pageName,false);
		sortPageLinks(links);
		return links;
	}
	private List getPageLinks1(RequestContext ctx,String pageNames[],boolean fork){
		if(pageNames==null||pageNames.length==0)
			return new ArrayList(0);
		List links=null;
		for(int i=0;i<pageNames.length;i++){
			List ks=getPageLinks2(ctx,pageNames[i],fork);
			if(links==null)
				links=ks;
			else
				links.addAll(ks);
		}
		sortPageLinks(links);
		return links;
	}
	private void sortPageLinks(List links){
		Comparator comparator=new Comparator(){
			public int compare(Object o1, Object o2) {
				Link p1=(Link)o1;
				Link p2=(Link)o2;
				int res=p1.getOrder()-p2.getOrder();
				if(res==0){
					String l1=p1.getLabel();
					String l2=p2.getLabel();
					if(l1!=null&&l2!=null){
						return l1.compareTo(l2);
					}
					return res;
				}
				return res;
			}
		};
		Comparator comparator2=new Comparator(){
			public int compare(Object o1, Object o2) {
				Link p1=(Link)o1;
				Link p2=(Link)o2;
				String s1=SORT_PAD+p1.getOrder();
				String s2=SORT_PAD+p2.getOrder();
				s1=s1.substring(s1.length()-SORT_LEN)+(p1.getLabel()==null?"":p1.getLabel());
				s2=s2.substring(s2.length()-SORT_LEN)+(p2.getLabel()==null?"":p2.getLabel());
				return s1.compareTo(s2);
			}
		};
		try{
			Collections.sort(links, comparator);
		}catch(IllegalArgumentException e){
			Collections.sort(links, comparator2);
		}
	}
	private List getPageLinks2(RequestContext ctx,String pageName,boolean fork){
		if(pageName==null||pageName.length()==0)
			return new ArrayList(0);
		CategorizedName catName=CategoryUtil.parseCategoryName(pageName);
		String relPageName=catName.getName();
		String group=catName.getCategory();
		CatagoryContextImpl mctx=new CatagoryContextImpl(ctx,group);
		List result=new ArrayList();
		Map confActions=configuration.getActions();
		Collection sourceActions=configuration.getActions().values();
		Collection sourcePages=configuration.getPages().values();
		PagePart part=(PagePart)pageParts.get(relPageName);
		boolean newPart=(part==null);
		if(setting.isDebug()==false&&part!=null){
			sourceActions=part.actions;
			sourcePages=part.pages;
		}
		Map partActions=null;
		Map partPages=null;
		if(newPart){
			partActions=new HashMap();
			partPages=new HashMap();
		}
		
		Page relPage=(Page)configuration.getPages().get(relPageName);
		if(relPage!=null){
			List selfAnchors=relPage.getSelfAnchors();
			for(int r=0;r<selfAnchors.size();r++){
				Anchor selfAnchor=(Anchor)selfAnchors.get(r);
				Page page=(Page)configuration.getPages().get(selfAnchor.getPage());
				String actionName=selfAnchor.getAction();
				if(actionName!=null){
					Action action=(Action)confActions.get(selfAnchor.getAction());
					if(action==null){
						logger.warn("Not exist action: "+selfAnchor.getAction());
						continue;
					}
					if(checkAccess(ctx,action.getName(),action)!=Permission.GRANTED){
						continue;
					}
				}else{
					Iterator lit=page.getAnchors().iterator();
					while(lit.hasNext()){
						Anchor a=(Anchor)lit.next();
						Action action=(Action)confActions.get(a.getAction());
						if(action==null){
							logger.warn("Not exist action: "+a.getAction());
							continue;
						}
						if(checkAccess(ctx,action.getName(),action)==Permission.GRANTED){
							actionName=action.getName();
							break;
						}
					}
					if(actionName==null)
						continue;
				}
				if(selfAnchor.getLabel()==null)
					selfAnchor.setLabel(page.getTitle());
				addPageAnchor(ctx.getLocale(),page.getName(),selfAnchor,actionName,result);
			}
		}
		
		Iterator pit=sourcePages.iterator();
		while(pit.hasNext()){
			Page page=(Page)pit.next();
			CategorizedPage pageMember=null;
			if(page.getPageObject()!=null){
				if(page.getPageObject() instanceof CategorizedPage){
					pageMember=(CategorizedPage)page.getPageObject();
				}
			}
			Iterator lit=page.getAnchors().iterator();
			while(lit.hasNext()){
				Anchor pa=(Anchor)lit.next();
				if(relPageName.equals(pa.getPage())){
					Action action=(Action)confActions.get(pa.getAction());
					if(action==null){
						logger.warn("Not exist action: "+pa.getAction());
						continue;
					}
					if(newPart){
						partPages.put(page.getName(), page);
					}
					if(pa.getLabel()==null)
						pa.setLabel(page.getTitle());
					if(pageMember!=null){
						if(fork==true){
							ForkedAnchor desc[]=pageMember.forkAnchor(mctx,page, pa);
							for(int d=0;d<desc.length;d++){
								if(checkAccess(ctx,CategoryUtil.createCategoryName(action.getName(),desc[d].getCategory()),action)!=Permission.GRANTED){
									continue;
								}
								addPageAnchorDesc(ctx.getLocale(),page.getName(),pa,desc[d].getLabel(),desc[d].getCategory(),desc[d].getOrder(),result);
							}
						}else if(pageMember!=null){
							if(checkAccess(ctx,CategoryUtil.createCategoryName(action.getName(),mctx.getCategory()),action)!=Permission.GRANTED){
								continue;
							}
							AdaptedAnchor aa = pageMember.adaptAnchor(mctx,page, pa);
							addPageAnchorDesc(ctx.getLocale(),page.getName(),pa,aa.getLabel(),mctx.getCategory(),pa.getOrder(),result);
						}
					}else{
						if(checkAccess(ctx,action.getName(),action)!=Permission.GRANTED){
							continue;
						}
						addPageAnchor(ctx.getLocale(),page.getName(),pa,result);
					}
				}
			}
		}
		
		Iterator ait=sourceActions.iterator();
		while(ait.hasNext()){
			ActionImpl action=(ActionImpl)ait.next();
			CategorizedAction actionMember=null;
			if(group!=null&&action.getActionInstance() instanceof CategorizedAction){
				actionMember=(CategorizedAction)action.getActionInstance();
			}
			Iterator lit=action.getAnchors().iterator();
			while(lit.hasNext()){
				Anchor pa=(Anchor)lit.next();
				if(relPageName.equals(pa.getPage())){
					if(pa.getLabel()==null)
						pa.setLabel(null);
					if(actionMember!=null){
						boolean perm=checkAccess(ctx,CategoryUtil.createCategoryName(action.getName(),mctx.getCategory()),action)==Permission.GRANTED;
						addActionAnchor(ctx.getLocale(),perm,newAnchorFromAction(pa,actionMember.adaptAnchor(mctx,action, pa)),result);
					}else{
						boolean perm=checkAccess(ctx,action.getName(),action)==Permission.GRANTED;
						addActionAnchor(ctx.getLocale(),perm,pa,result);
					}
					if(newPart){
						partActions.put(action.getName(), action);
					}
				}
			}
		}
		
		if(newPart&&(configuration.getPages().get(relPageName)!=null||partActions.size()>0||partPages.size()>0)){
			part=new PagePart();
			part.actions=partActions.values();
			part.pages=partPages.values();
			pageParts.put(relPageName,part);
		}
		
		return result;
	}
	private void addPageAnchorDesc(Locale local,String pageName,Anchor anchor,String label,String cate,int order,List result){
		try {
			if(cate!=null&&anchor.getPage()!=null){
				pageName=CategoryUtil.createCategoryName(pageName,cate);
			}
			Anchor na=(Anchor) anchor.clone();
			na.setLabel(Util.asI18NTexts(label));
			na.setOrder(order);
			addPageAnchor(local,pageName,na,result);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
		
	}
	private void addPageAnchor(Locale local,String pageName,Anchor anchor,List result){
		LinkImpl link=new LinkImpl(Link.FROM_PAGE,anchor,anchor.getLabel().getText(local),pageName,anchor.getAction(),true);
		result.add(link);
	}
	private void addPageAnchor(Locale local,String pageName,Anchor anchor,String action,List result){
		LinkImpl link=new LinkImpl(Link.FROM_PAGE,anchor,anchor.getLabel().getText(local),pageName,action,true);
		result.add(link);
	}
	private void addActionAnchor(Locale local,boolean perm,Anchor anchor,List result){
		String label=null;
		if(anchor.getLabel()!=null){
			label=anchor.getLabel().getText(local);
		}
		LinkImpl link=new LinkImpl(Link.FROM_ACTION,anchor,label,null,anchor.getAction(),perm);
		result.add(link);
	}
	private Anchor newAnchorFromAction(Anchor anchor,AdaptedAnchor desc){
		try {
			Anchor na=(Anchor) anchor.clone();
			na.setLabel(Util.asI18NTexts(desc.getLabel()));
			na.setOrder(desc.getOrder());
			return na;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	protected Permission checkAccess(RequestContext ctx,String actionName,Action act){
		if(accessController!=null){
			return accessController.checkAccess(ctx,new CheckActionImpl(actionName,act));
		}
		return Permission.GRANTED;
	}
	public List getActionNodes(final RequestContext ctx,Set roles){
		int roleSize=roles==null?0:roles.size();
		CatagoryContextImpl cci=new CatagoryContextImpl(ctx,null);
		ActionNodeImpl root=new ActionNodeImpl();
		root.setName("root");
		Map actions=configuration.getActions();
		List actionList=new ArrayList(actions.values());
		
		Map partBox=new HashMap();
		Iterator it=actionList.iterator();
		while(it.hasNext()){
			ActionImpl a=(ActionImpl)it.next();
			addActionNodeLeaders(partBox,a.getName(),a.getLeaders());
		}
		Map partners=new HashMap();
		it=actionList.iterator();
		while(it.hasNext()){
			ActionImpl a=(ActionImpl)it.next();
			List box=new ArrayList();
			partners.put(a.getName(),box);				
			addActionNodeParts(box,a.getPartners(),partBox,(List)partBox.get(a.getName()));
		}
		
		it=actionList.iterator();
		Set paths=new HashSet();
		while(it.hasNext()){
			ActionImpl a=(ActionImpl)it.next();
			if(a.getLabel()!=null&&a.getLabel().size()>0&&(roleSize==0||roles.contains(a.getRole()))){
				if(a.getActionInstance() instanceof CategorizedAction){
					ForkedAction[] fas=((CategorizedAction)a.getActionInstance()).forkAction(cci,a);
					for(int f=0;f<fas.length;f++){
						addActionItem(root, paths,partners,new ForkedActionDesc(this,a,fas[f]));
					}
				}else
					addActionItem(root, paths,partners,new ActionActionDesc(a,a.getLabel().getText(ctx.getLocale())));
			}
		}
		root.sort();
		return root.getChildActions();
	}
	private void addActionItem(ActionNodeImpl root, Set paths,Map extParts,ActionDesc act) {
		if(paths.contains(act.getLabel())){
			throw new IllegalStateException("Repeat action path: "+act.getName()+" "+act.getLabel());
		}
		paths.add(act.getLabel());
		root.addItem(this.getConfiguration(),extParts,act);
	}
	private void addActionNodeLeaders(Map partMap,String name,String leads[]){
		if(leads==null||leads.length==0)
			return;
		for(int i=0;i<leads.length;i++){
			String part=leads[i];
			List box=(List)partMap.get(part);
			if(box==null){
				box=new ArrayList();
				partMap.put(part, box);
			}
			if(box.contains(name)){
				continue;
			}
			box.add(name);
			ActionImpl a=(ActionImpl)getAction(part);
			if(a==null)
				continue;
			addActionNodeLeaders(partMap,name,a.getLeaders());
		}
	}
	private void addActionNodeParts(List box,String parts[],Map partBox,List partList){
		if(parts!=null){
			for(int i=0;i<parts.length;i++){
				String part=parts[i];
				if(box.contains(part)){
					continue;
				}
				box.add(part);
				ActionImpl a=(ActionImpl)getAction(part);
				if(a==null)
					continue;
				addActionNodeParts(box,a.getPartners(),partBox,(List)partBox.get(a.getName()));
			}
		}
		if(partList==null)
			return;
		for(int i=0;i<partList.size();i++){
			String part=(String)partList.get(i);
			if(box.contains(part)){
				continue;
			}
			box.add(part);
			ActionImpl a=(ActionImpl)getAction(part);
			if(a==null)
				continue;
			addActionNodeParts(box,a.getPartners(),partBox,(List)partBox.get(a.getName()));
		}
	}
	public List getMenuNodes(RequestContext ctx,String mainPage[]){
		List links = getPageLinks1(ctx, mainPage,true);
		MenuNodeImpl root = new MenuNodeImpl();
		root.setLabel("root");
		Iterator it = links.iterator();
		while (it.hasNext()) {
			Link link = (Link) it.next();
			if (link.getPage() != null)
				root.addItem(configuration, link);
		}
		return root.getChildMenus();
	}
	private class PagePart{
		Collection pages;
		Collection actions;
	}
}
