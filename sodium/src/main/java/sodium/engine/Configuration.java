package sodium.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import sodium.action.Action;
import sodium.action.Anchor;
import sodium.action.impl.ActionImpl;
import sodium.action.impl.FormAdapter;
import sodium.action.impl.FormAdapterImpl;
import sodium.action.impl.LayoutAdapter;
import sodium.action.impl.LayoutAdapterImpl;
import sodium.action.impl.TypeAdapter;
import sodium.action.impl.TypeAdapterImpl;
import sodium.anchoropt.ObjectOption;
import sodium.anchortype.ActionAnchorType;
import sodium.anchortype.Option;
import sodium.anchortype.OptionParser;
import sodium.anchortype.PageAnchorType;
import sodium.annotation.ActionGroup;
import sodium.annotation.Opt;
import sodium.impl.BeanCreatorImpl;
import sodium.impl.InnerConfigurationProvider;
import sodium.impl.InnerGroupInfo;
import sodium.impl.InnerUtil;
import sodium.page.AttachmentCreator;
import sodium.page.Component;
import sodium.page.ComponentImpl;
import sodium.page.JsClass;
import sodium.page.Page;
import sodium.page.PageImpl;
import sodium.page.PageContainerRenderer;
import sodium.page.jsparser.Js3;
import sodium.page.jsparser.JsCompiler;
import sodium.print.PrintService;
import sodium.print.PrintablePageRenderer;
import sodium.print.printservice.ExportPrintService;
import sodium.print.printservice.HtmlPrintService;
import sodium.print.printservice.PreviewPrintService;
import sodium.print.renderer.ByteArrayPrintablePageRenderer;
import sodium.print.renderer.JasperPrintablePageRenderer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.xmlform.config.ConfigParseResult;
import net.sf.xmlform.config.ConfigurationFactory;
import net.sf.xmlform.config.ConfigurationProvider;
import net.sf.xmlform.config.FormDefinition;
import net.sf.xmlform.config.TypeDefinition;
import net.sf.xmlform.data.SourceType;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.impl.DefaultConfigurationProvider;
import net.sf.xmlform.formlayout.LayoutProvider;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.config.FormLayoutFactory;
import net.sf.xmlform.formlayout.impl.DefaultLayoutProvider;
import net.sf.xmlform.util.ClassResource;
import net.sf.xmlform.util.ClassResourceScanner;
import net.sf.xmlform.util.ClassResourceVisitor;
import net.sf.xmlform.util.FormUtils;
import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;

/**
 * @author Liu Zhikun
 */

public class Configuration implements ApplicationContextAware{
	public static final String DECLARE_BEGIN="{//BEGIN_DECLARE",DECLARE_END="}//END_DECLARE";
	private static Logger logger = LoggerFactory.getLogger(Configuration.class);
	private Map actionInstances=new HashMap();
	private Map jasperDesignResources=new HashMap();
	private Map jasperReports=new HashMap();
	private Map typeAdapters=new HashMap();
	private Map formAdapters=new HashMap();
	private Map layoutAdapters=new HashMap();
	private DefaultConfigurationProvider configurationProvider=new InnerConfigurationProvider(typeAdapters);
	private DefaultLayoutProvider layoutProvider=new DefaultLayoutProvider();
	private ConfigurationFactory configurationFactory=new ConfigurationFactory();
	private FormLayoutFactory formLayoutFactor=new FormLayoutFactory();
	private String packagesToScan[]=new String[]{};
	private PageContainerRenderer pageContainerRenderer;
	private AttachmentCreator pageAttachmentCreator;
	private BeanCreator beanCreator=null;
	private JsCompiler jsCompiler[]=new JsCompiler[]{new Js3()};
	private Map printServices=new HashMap();
	private List printablePageRenderers=new ArrayList();
	private Map actionGroups=new HashMap();
	private Map groupOrders=new HashMap();
	
	private long lastModify=0;
	private Map jsClasses=new HashMap();
	private Map pageCache=new HashMap();
	private Map components=new HashMap();
	private Map xmlEntities=new HashMap();
	private Map pageAnchorTypes=new HashMap();
	private Map actionAnchorTypes=new HashMap();
	private ApplicationContext applicationContext;
	private PageAnchorType unTypedPageAnchor=new sodium.anchortype.common.Untyped();
	private ActionAnchorType unTypedActionAnchor=new sodium.anchortype.common.Untyped();
	
	public Configuration(){
		printablePageRenderers.add(new JasperPrintablePageRenderer());
		printablePageRenderers.add(new ByteArrayPrintablePageRenderer());
		
		ExportPrintService exportPrinter=new ExportPrintService();
		printServices.put(exportPrinter.getName(), exportPrinter);
		
		PreviewPrintService previewPrinter=new PreviewPrintService();
		printServices.put(previewPrinter.getName(), previewPrinter);
		
		HtmlPrintService htmlPrintService=new HtmlPrintService();
		printServices.put(htmlPrintService.getName(), htmlPrintService);
		
		pageAnchorTypes.put("create",new sodium.anchortype.common.Create());
		pageAnchorTypes.put("view",new sodium.anchortype.common.View());
		pageAnchorTypes.put("link",new sodium.anchortype.common.Link());
		pageAnchorTypes.put("untyped",unTypedPageAnchor);
		
		actionAnchorTypes.put("save",new sodium.anchortype.common.Save());
		actionAnchorTypes.put("resetform",new sodium.anchortype.common.ResetForm());
		actionAnchorTypes.put("resetpage",new sodium.anchortype.common.ResetPage());
		actionAnchorTypes.put("query",new sodium.anchortype.common.Query());
		actionAnchorTypes.put("print",new sodium.anchortype.common.Print());
		actionAnchorTypes.put("delete",new sodium.anchortype.common.Delete());
		actionAnchorTypes.put("closepage",new sodium.anchortype.common.ClosePage());
		actionAnchorTypes.put("untyped",unTypedActionAnchor);
	}
	
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		WebApplicationContext wac=(WebApplicationContext)ac;
		applicationContext=ac;
		if(beanCreator==null){
			BeanCreatorImpl bc=new BeanCreatorImpl();
			bc.setApplicationContext(wac);
			beanCreator=bc;
		}
	}
	
	public String[] getPackagesToScan() {
		return packagesToScan;
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}
	
	public Map<String,PageAnchorType> getPageAnchorTypes() {
		return pageAnchorTypes;
	}

	public void setPageAnchorTypes(Map<String,PageAnchorType> pageAnchorTypes) {
		this.pageAnchorTypes.putAll(pageAnchorTypes);
	}

	public Map<String,ActionAnchorType> getActionAnchorTypes() {
		return actionAnchorTypes;
	}

	public void setActionAnchorTypes(Map<String,ActionAnchorType> actionAnchorTypes) {
		this.actionAnchorTypes.putAll(actionAnchorTypes);
	}
	
	public ConfigurationProvider getXmlformConfigurationProvider(){
		return configurationProvider;
	}
	
	public LayoutProvider getXmlformLayoutProvider(){
		return layoutProvider;
	}
	
	public List<PrintablePageRenderer> getPrintablePageRenderers() {
		return printablePageRenderers;
	}

	public void setPrintablePageRenderers(List<PrintablePageRenderer> pageRenderers) {
		this.printablePageRenderers = pageRenderers;
	}

	public PageContainerRenderer getPageContainerRenderer() {
		return pageContainerRenderer;
	}

	public void setPageContainerRenderer(PageContainerRenderer pageContainerRenderer) {
		this.pageContainerRenderer = pageContainerRenderer;
	}

	public AttachmentCreator getPageAttachmentCreator() {
		return pageAttachmentCreator;
	}

	public void setPageAttachmentCreator(AttachmentCreator pageAttachmentCreator) {
		this.pageAttachmentCreator = pageAttachmentCreator;
	}

	public BeanCreator getBeanCreator() {
		return beanCreator;
	}

	public void setActionCreator(BeanCreator beanCreator) {
		this.beanCreator = beanCreator;
	}

	public Map<String,PrintService> getPrintServices() {
		return printServices;
	}

	public void setPrintServices(Map<String,PrintService> printServices) {
		this.printServices = printServices;
	}

	public Map<String,Action> getActions() {
		return actionInstances;
	}
	
//	public Map<String,List<TypeAdapter>> getTypeAdapters(){
//		return typeAdapters;
//	}
	
	public Map<String,List<FormAdapter>> getFormAdapters(){
		return formAdapters;
	}
	
	public Map<String,List<LayoutAdapter>> getLayoutAdapters(){
		return layoutAdapters;
	}
	
	public int getGroupOrder(String group){
		if(group==null)
			return sodium.action.Anchor.BASE_ORDER;
		if(group.startsWith("/"))
			group=group.substring(1);
		if(group.endsWith("/"))
			group=group.substring(0, group.length()-1);
		Integer ord=(Integer)groupOrders.get(group);
		if(ord==null)
			return sodium.action.Anchor.BASE_ORDER;
		return ord.intValue();
	}
	
	public Map<String,JsClass> getJsClasses(){
		return jsClasses;
	}
	
	public Map<String,Page> getPages() {
		return pageCache;
	}

	public void init(){
		reload();
		logger.info("Sodium configuration from "+Arrays.toString(packagesToScan)
			+" load page: "+pageCache.size()
			+" action: "+actionInstances.size()
			+" jasperReport: "+jasperDesignResources.size()
		);
	}
	
	public void reload(){
		lastModify=doRefresh(0);
	}
	public long refresh(){
		lastModify=doRefresh(lastModify);
		return lastModify;
	}
	private long doRefresh(long lastModify){
		long last=0;
		Set actionBox=new HashSet();
		refreshFragments(lastModify);
		last=Math.max(last, refreshForms(lastModify));
		last=Math.max(last, refreshSpringBeans(lastModify,actionBox));
		last=Math.max(last, refreshActions2(lastModify,actionBox));
		last=Math.max(last, refreshLayout(lastModify));
		last=Math.max(last, refreshPage(lastModify));
		last=Math.max(last, refreshComponent(lastModify));
		lastModify=Math.max(last, refreshJasperReport(lastModify));
		
		setGroupPrevious();
		
		return lastModify;
	}
	
	public long refreshActions(long last) {
		final Set actionBox=new HashSet();
		last=refreshActions2(last,actionBox);
		return last;
	}
	private long refreshActions2(long last,final Set actionBox) {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshAction(file,actionBox);
			}
		};
		long l=findFileByExt(last,".action.xml",v);
//		Iterator it=actionGroup.keySet().iterator();
//		while(it.hasNext()){
//			String key=(String)it.next();
//			Map as=(Map)actionGroup.get(key);
//			wsServiceImpl.putActions(key, as);
//		}
		return l;
	}
	private void doRefreshAction(ClassResource file,Set actionBox) {
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(file.getInputStream());
			Element root=document.getRootElement();
			
			String interfaceName=root.attributeValue("name");
			I18NTexts interfaceLabel=new I18NTexts();
			interfaceLabel.put(new I18NText(FormUtils.parseLocale(null),root.attributeValue("label")));
			interfaceLabel.putAll(InnerUtil.parseLabel(root));
			addActionGroup(new InnerGroupInfo(interfaceName,root.attributeValue("label"),root.attributeValue("previous")));
			
			String interfaceNameDot=","+interfaceName+".";
			Iterator it=root.elementIterator("action");
			while(it.hasNext()){
				Element ce=(Element)it.next();
				String name=ce.attributeValue("name");
				String method=ce.attributeValue("method",name);
				if(name==null){
					if(method==null){
						throw new IllegalArgumentException("Must provide action name or method");
					}
					name=method;
				}
				name=interfaceName+"."+name;
				if(actionBox.contains(name)){
					logger.error("Action name repeate :"+name+"");
					continue;
				}
				actionBox.add(name);
				String cla=ce.attributeValue("class");
				if(cla==null||cla.length()==0){
					throw new IllegalArgumentException("Must provide class parameter :"+name);
				}
				Object actionInst=beanCreator.createBean(cla);
				if(actionInst==null){
					throw new IllegalArgumentException("Create bean is null for : "+cla);
				}
				Class realClass=AopUtils.getTargetClass(actionInst);
				ActionImpl act=new ActionImpl();
				act.setName(name);
				act.setActionInstance(actionInst);
				String sf=ce.attributeValue("source");
				String sft=ce.attributeValue("sourcetype");
				String rf=ce.attributeValue("result");
				String ma=ce.attributeValue("max");
				String mi=ce.attributeValue("min");
				String previous=ce.attributeValue("previous");
				act.setActionMethod(getExecuteMethod(actionInst.getClass(),method));
				createPrintableAction(act,actionInst.getClass(),method,ce.attributeValue("printmethod"),ce.attributeValue("textmethod"));
				act.setRole(ce.attributeValue("role"));
				act.setLabel(concatTexts(interfaceLabel,InnerUtil.parseLabel(ce)));
				act.setSourceType(parseSourceType(sft));
				String part=ce.attributeValue("partners", null);
				if(part!=null){
					part=(","+part).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
					act.setPartners(part.split(","));
				}
				String leaders=ce.attributeValue("leaders", null);
				if(leaders!=null){
					leaders=(","+leaders).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
					act.setLeaders(leaders.split(","));
				}
				
				if(sf!=null){
					act.setSourceForm(sf);
				}
				if(rf!=null){
					act.setResultForm(rf);
				}
//				if(previous!=null){
//					previous=(","+leaders).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
//					act.setPrevious(previous.split(","));
//				}
//				if(rd!=null){
//					act.setReadonly(Boolean.parseBoolean(rd));
//				}
				sf=act.getSourceForm();
				rf=act.getResultForm();
				if(sf!=null){
					if(sf.startsWith("!")){
						act.setSourceForm(createSimpleForm(name.toLowerCase()+".Sxf",sf));
					}
				}
				if(rf!=null){
					if(rf.startsWith("!")){
						act.setResultForm(createSimpleForm(name.toLowerCase()+".Rxf",rf));
					}
//					if("s".equals(sf)){
//						act.setResultForm(act.getSourceForm());
//					}
				}
				
				if(ma!=null){
					act.setMaxoccurs(Long.parseLong(ma));
				}
				if(mi!=null){
					act.setMinoccurs(Long.parseLong(mi));
				}
				
				actionInstances.put(act.getName(), act);
				act.setAnchors(loadAnchors(act,ce));
			}
			
			doRefreshTypeDocorateAction(root);
			doRefreshFormDocorateAction(root);
			doRefreshLayoutDocorateAction(root);

		}catch(Exception e){
			logger.error("Load action: "+getResourceName(file),e);
		}
	}
	private void doRefreshTypeDocorateAction(Element root) throws Exception{
		Iterator fit=root.elementIterator("type-decorator");
		while(fit.hasNext()){
			Element ce=(Element)fit.next();
			String type=ce.attributeValue("type");
			String method=ce.attributeValue("method");
			String cla=ce.attributeValue("class");
			if(type==null||type.length()==0){
				throw new IllegalArgumentException("Must provide type parameter");
			}
			if(cla==null||cla.length()==0){
				throw new IllegalArgumentException("Must provide class parameter");
			}
			if(method==null||method.length()==0){
				throw new IllegalArgumentException("Must provide method parameter");
			}
			Object actionInst=beanCreator.createBean(cla);
			if(actionInst==null){
				throw new IllegalArgumentException("Create bean is null for : "+cla);
			}
			Class realClass=AopUtils.getTargetClass(actionInst);
			FormAdapterImpl impl=new FormAdapterImpl();
			impl.setAdapterInstance(actionInst);
			impl.setAdapterMethod(getTypeAdapterMethod(actionInst.getClass(),method));
			
			List items=(List)typeAdapters.get(type);
			if(items==null){
				items=new ArrayList(2);
				typeAdapters.put(type,items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private void doRefreshFormDocorateAction(Element root) throws Exception{
		Iterator fit=root.elementIterator("form-decorator");
		while(fit.hasNext()){
			Element ce=(Element)fit.next();
			String form=ce.attributeValue("form");
			String method=ce.attributeValue("method");
			String cla=ce.attributeValue("class");
			if(form==null||form.length()==0){
				throw new IllegalArgumentException("Must provide form parameter");
			}
			if(cla==null||cla.length()==0){
				throw new IllegalArgumentException("Must provide class parameter");
			}
			if(method==null||method.length()==0){
				throw new IllegalArgumentException("Must provide method parameter");
			}
			Object actionInst=beanCreator.createBean(cla);
			if(actionInst==null){
				throw new IllegalArgumentException("Create bean is null for : "+cla);
			}
			Class realClass=AopUtils.getTargetClass(actionInst);
			FormAdapterImpl impl=new FormAdapterImpl();
			impl.setAdapterInstance(actionInst);
			impl.setAdapterMethod(getFormAdapterMethod(actionInst.getClass(),method));
			
			List items=(List)formAdapters.get(form);
			if(items==null){
				items=new ArrayList(2);
				formAdapters.put(form,items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private void doRefreshLayoutDocorateAction(Element root) throws Exception{
		Iterator fit=root.elementIterator("layout-decorator");
		while(fit.hasNext()){
			Element ce=(Element)fit.next();
			String form=ce.attributeValue("layout");
			String method=ce.attributeValue("method");
			String cla=ce.attributeValue("class");
			if(form==null||form.length()==0){
				throw new IllegalArgumentException("Must provide form parameter");
			}
			if(cla==null||cla.length()==0){
				throw new IllegalArgumentException("Must provide class parameter");
			}
			if(method==null||method.length()==0){
				throw new IllegalArgumentException("Must provide method parameter");
			}
			Object actionInst=beanCreator.createBean(cla);
			if(actionInst==null){
				throw new IllegalArgumentException("Create bean is null for : "+cla);
			}
			Class realClass=AopUtils.getTargetClass(actionInst);
			LayoutAdapterImpl impl=new LayoutAdapterImpl();
			impl.setAdapterInstance(actionInst);
			impl.setAdapterMethod(getLayoutAdapterMethod(actionInst.getClass(),method));
			
			List items=(List)layoutAdapters.get(form);
			if(items==null){
				items=new ArrayList(2);
				layoutAdapters.put(form,items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private long refreshSpringBeans(long last,final Set actionBox) {
		final Set expireClass=new HashSet();
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				try {
					String className=file.getAbsoluteName().substring(0, file.getAbsoluteName().lastIndexOf(".")).replace("/", ".");
					expireClass.add(Class.forName(className));
				} catch (ClassNotFoundException e) {
					logger.error("Load action: "+e.getMessage(),e);
				}
			}
		};
		long newLast=findFileByExt(last,".class",v);
		
		Map objs=applicationContext.getBeansWithAnnotation(ActionGroup.class);
		Iterator it=objs.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			Object obj=objs.get(key);
			ActionGroup actionGroupAnn=(ActionGroup)applicationContext.findAnnotationOnBean(key, ActionGroup.class);
			if(actionGroupAnn==null)
				continue;
			Class realClass=AopUtils.getTargetClass(obj);
			if(!expireClass.contains(realClass))
				continue;
			Method[] methods = realClass.getDeclaredMethods();
			doRefreshSpringBeans(obj,realClass,methods,actionGroupAnn,actionBox);
		}
		
		return newLast;
	}
	private void doRefreshSpringBeans(Object proxyObj,Class realClass,Method methods[],ActionGroup actionGroupAnn,Set actionBox){
		try{
			String interfaceName=actionGroupAnn.name();
			I18NTexts interfaceLabel=(I18NTexts)OptionParser.parseOpt(I18NTexts.class, actionGroupAnn.label());
			addActionGroup(new InnerGroupInfo(interfaceName,actionGroupAnn.label(),actionGroupAnn.previous()));
			String interfaceNameDot=","+interfaceName+".";
			for(int i=0;i<methods.length;i++){
				sodium.annotation.Action actionAnn=(sodium.annotation.Action)methods[i].getAnnotation(sodium.annotation.Action.class);
				sodium.annotation.FormAction formActionAnn=(sodium.annotation.FormAction)methods[i].getAnnotation(sodium.annotation.FormAction.class);
				if(actionAnn==null)
					continue;
				String name=actionAnn.name();
				Method method=methods[i];
				if(name==null||name.length()==0){
					name=method.getName();
				}
				name=interfaceName+"."+name;
				if(actionBox.contains(name)){
					throw new IllegalArgumentException("Action name repeate :"+name);
				}
				actionBox.add(name);
				ActionImpl act=new ActionImpl();
				act.setName(name);
				act.setActionInstance(proxyObj);
//				act.setReadonly(actionAnn.readonly());
//				String previous=actionAnn.previous();
				String sf=formActionAnn==null?"":formActionAnn.source();
				String rf=formActionAnn==null?"":formActionAnn.result();
				String printName=formActionAnn==null?"":formActionAnn.printMethod();
				String textName=formActionAnn==null?"":formActionAnn.textMethod();
				String sourceType=formActionAnn==null?"form":formActionAnn.sourcetype();
				act.setSourceType(parseSourceType(sourceType));
				act.setActionMethod(getExecuteMethod(proxyObj.getClass(),method.getName()));
				createPrintableAction(act,proxyObj.getClass(),method.getName(),printName,textName);
				if(actionAnn.role()!=null&&actionAnn.role().length()>0)
					act.setRole(actionAnn.role());
				act.setLabel(concatTexts(interfaceLabel,(I18NTexts)OptionParser.parseOpt(I18NTexts.class,actionAnn.label())));
				String part=actionAnn.partners();
				if(part!=null&&part.length()>0){
					part=(","+part).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
					act.setPartners(part.split(","));
				}
				String leaders=actionAnn.leaders();
				if(leaders!=null&&leaders.length()>0){
					leaders=(","+leaders).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
					act.setLeaders(leaders.split(","));
				}
//				if(previous!=null&&previous.length()>0){
//					previous=(","+leaders).replaceAll("\\s","").replace(",!", interfaceNameDot).substring(1);
//					act.setPrevious(previous.split(","));
//				}
				
				if(sf!=null&&sf.length()>0){
					act.setSourceForm(sf);
				}
				if(rf!=null&&rf.length()>0){
					act.setResultForm(rf);
				}
				sf=act.getSourceForm();
				rf=act.getResultForm();
				if(sf!=null){
					if(sf.startsWith("!")){
						act.setSourceForm(createSimpleForm(name.toLowerCase()+".Sxf",sf));
					}
				}
				if(rf!=null){
					if(rf.startsWith("!")){
						act.setResultForm(createSimpleForm(name.toLowerCase()+".Rxf",rf));
					}
				}
				
				act.setMaxoccurs(formActionAnn==null?Integer.MAX_VALUE:formActionAnn.max());
				act.setMinoccurs(formActionAnn==null?0:formActionAnn.min());
				
				actionInstances.put(act.getName(), act);
				
				sodium.annotation.Anchor[] anchors = actionAnn.anchors();
				for(int a=0;a<anchors.length;a++){
					sodium.annotation.Anchor anchorAnn=anchors[a];
					Anchor anc=new Anchor();
					Map optVals=new HashMap();
					optVals.put("sourcetype", act.getSourceType().toString().toLowerCase());
					Opt[] opts=anchorAnn.options();
					for(int p=0;p<opts.length;p++){
						optVals.put(opts[p].name(), opts[p].value());
					}
					
					optVals.put("attach", anchorAnn.attach());
					optVals.put("icon", anchorAnn.icon());
					optVals.put("order", anchorAnn.order());
					optVals.put("page", anchorAnn.page());
					optVals.put("type", anchorAnn.type());
					optVals.put("label", anchorAnn.label());
					
					parseActionOptions(act,anc,optVals);
					act.getAnchors().add(anc);
				}
			}
			doRefreshSpringTypeDecoratorBeans(proxyObj,realClass,methods);
			doRefreshSpringFormDecoratorBeans(proxyObj,realClass,methods);
			doRefreshSpringLayoutDecoratorBeans(proxyObj,realClass,methods);
		}catch(Exception e){
			logger.error("Load action: "+e.getMessage(),e);
		}
	}
	private void doRefreshSpringTypeDecoratorBeans(Object proxyObj,Class realClass,Method methods[]){
		for(int i=0;i<methods.length;i++){
			sodium.annotation.TypeAdapter decAnn=(sodium.annotation.TypeAdapter)methods[i].getAnnotation(sodium.annotation.TypeAdapter.class);
			if(decAnn==null)
				continue;
			Method method=methods[i];
			TypeAdapterImpl impl=new TypeAdapterImpl();
			impl.setAdapterInstance(proxyObj);
			impl.setAdapterMethod(getTypeAdapterMethod(proxyObj.getClass(),method.getName()));
			List items=(List)typeAdapters.get(decAnn.value());
			if(items==null){
				items=new ArrayList();
				typeAdapters.put(decAnn.value(),items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private void doRefreshSpringFormDecoratorBeans(Object proxyObj,Class realClass,Method methods[]){
		for(int i=0;i<methods.length;i++){
			sodium.annotation.FormAdapter decAnn=(sodium.annotation.FormAdapter)methods[i].getAnnotation(sodium.annotation.FormAdapter.class);
			if(decAnn==null)
				continue;
			Method method=methods[i];
			FormAdapterImpl impl=new FormAdapterImpl();
			impl.setAdapterInstance(proxyObj);
			impl.setAdapterMethod(getFormAdapterMethod(proxyObj.getClass(),method.getName()));
			List items=(List)formAdapters.get(decAnn.value());
			if(items==null){
				items=new ArrayList();
				formAdapters.put(decAnn.value(),items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private void doRefreshSpringLayoutDecoratorBeans(Object proxyObj,Class realClass,Method methods[]){
		for(int i=0;i<methods.length;i++){
			sodium.annotation.LayoutAdapter decAnn=(sodium.annotation.LayoutAdapter)methods[i].getAnnotation(sodium.annotation.LayoutAdapter.class);
			if(decAnn==null)
				continue;
			Method method=methods[i];
			LayoutAdapterImpl impl=new LayoutAdapterImpl();
			impl.setAdapterInstance(proxyObj);
			impl.setAdapterMethod(getLayoutAdapterMethod(proxyObj.getClass(),method.getName()));
			List items=(List)layoutAdapters.get(decAnn.value());
			if(items==null){
				items=new ArrayList();
				layoutAdapters.put(decAnn.value(),items);
			}
			items.remove(impl);
			items.add(impl);
		}
	}
	private void createPrintableAction(ActionImpl action,Class clazz,String actionMethod,String printMethod,String textMethod){
		if(printMethod!=null&&printMethod.length()>0){
			try{
//				if("this".equals(printMethod)){
//					printMethod=actionMethod;
//				}
				action.setPrintMethod(clazz.getMethod(printMethod,sodium.action.PrintContext.class,List.class));
//				if(action.getPrintMethod()==null)
//					throw new NoSuchMethodException();
			}catch(NoSuchMethodException e){
				throw new IllegalArgumentException("Not found print method "+printMethod+" in "+clazz.getName());
			}
		}
		if(textMethod!=null&&textMethod.length()>0){
			try{
				action.setTextMethod(clazz.getMethod(textMethod,sodium.action.ReferenceContext.class,Object.class,Object.class));
//				if(action.getTextMethod()==null)
//					throw new NoSuchMethodException();
			}catch(NoSuchMethodException e){
				throw new IllegalArgumentException("Not found text method "+textMethod+" in "+clazz.getName());
			}
		}
	}
	private SourceType parseSourceType(String t){
		if(t==null||t.length()==0)
			return SourceType.FORM;
		try{
			return SourceType.valueOf(t.toLowerCase());
		}catch(Exception e){
			return SourceType.valueOf(t.toUpperCase());
		}
	}
	private void setGroupPrevious(){
		Map cache=new HashMap();
		Set track=new HashSet();
		Iterator ait=actionGroups.values().iterator();
		while(ait.hasNext()){
			InnerGroupInfo info=(InnerGroupInfo)ait.next();
			String label=info.getLabel();
			if(label==null||label.length()==0){
				continue;
			}
			if(label.startsWith("/"))
				label=label.substring(1);
			if(label.endsWith("/"))
				label=label.substring(0, label.length()-1);
			int order=getGroupPrevOrder(cache,track,info,0);
			Integer old=(Integer)groupOrders.get(label);
			if(old==null||old.intValue()<order)
				groupOrders.put(label,order );
		}
		cache.clear();
		track.clear();
	}
	private int getGroupPrevOrder(Map cache,Set track,InnerGroupInfo info,int level){
		Integer order=(Integer)cache.get(info.getName());
		if(order!=null)
			return order;
		if(track.contains(info.getName()))
			return sodium.action.Anchor.BASE_ORDER;
		track.add(info.getName());
		String prevStr=info.getPrevious();
		if(prevStr==null||prevStr.length()==0)
			return sodium.action.Anchor.BASE_ORDER;
		int max=sodium.action.Anchor.BASE_ORDER;
		String prevs[]=prevStr.split(",");
		for(int i=0;i<prevs.length;i++){
			String prev=prevs[i];
			InnerGroupInfo g=(InnerGroupInfo)actionGroups.get(prev);
			if(g==null)
				continue;
			int ord=getGroupPrevOrder(cache,track,g,level+1);
			if(ord>max)
				max=ord;
		}
		max++;
		cache.put(info.getName(),max);
		return max;
	}
	private void addActionGroup(InnerGroupInfo info){
		actionGroups.put(info.getName(), info);
	}
	private Method getExecuteMethod(Class cls,String name){
		try{
			return cls.getMethod(name,sodium.action.ActionContext.class,List.class);
		}catch(NoSuchMethodException e){
			throw new IllegalArgumentException("Not found method "+name+" in "+cls.getName());
		}
	}
	private Method getTypeAdapterMethod(Class cls,String name){
		try{
			return cls.getMethod(name,sodium.action.TypeAdapteContext.class,TypeDefinition.class);
		}catch(NoSuchMethodException e){
			throw new IllegalArgumentException("Not found method "+name+" in "+cls.getName());
		}
	}
	private Method getFormAdapterMethod(Class cls,String name){
		try{
			return cls.getMethod(name,sodium.action.FormAdapteContext.class,XMLForm.class);
		}catch(NoSuchMethodException e){
			throw new IllegalArgumentException("Not found method "+name+" in "+cls.getName());
		}
	}
	private Method getLayoutAdapterMethod(Class cls,String name){
		try{
			return cls.getMethod(name,sodium.action.LayoutAdapteContext.class,FormLayout.class);
		}catch(NoSuchMethodException e){
			throw new IllegalArgumentException("Not found method "+name+" in "+cls.getName());
		}
	}
	private I18NTexts concatTexts(I18NTexts from,I18NTexts to){
		if(to==null)
			return new I18NTexts();
		if(from==null||from.size()==0)
			return to;
		Iterator it=to.getLocales().iterator();
		while(it.hasNext()){
			Locale key=(Locale)it.next();
			String text=from.getText(key);
			if(text==null){
				text=from.getText(Locale.ENGLISH);
			}
			if(text!=null){
				I18NText itext=to.get(key);
				if("/".equals(itext.getText()))
					itext.setText(text);
				else
					itext.setText(text+"/"+itext.getText());
			}
		}
		return to;
	}
//	private Method getMethodByName(Class cls,String name) throws NoSuchMethodException{
//		Method ms=Util.getMethodByName(cls, name);
//		if(ms==null)
//			throw new NoSuchMethodException("Not found method "+name+" in "+cls.getName());
//		return ms;
//	}
	private String getResourceName(ClassResource resorce){
			return resorce.getAbsoluteName();
	}
	private void loadForms(Iterator fit) throws Exception{
		while(fit.hasNext()){
			Element f=(Element)fit.next();
			checkRestricName(f.attributeValue("form"));
			loadConfigPart(f.asXML());
			StringBuilder sb=new StringBuilder();
		}
	}
	private void checkRestricName(String name){
		if(name==null||name.length()==0)
			return;
		String starts[]=new String[]{"page.","source.","result.","anchor.","action.","parent."};
		String names[]=new String[]{"page","source","result","anchor","action","parent"};
		for(int i=0;i<starts.length;i++){
			if(name.startsWith(starts[i]))
				throw new IllegalArgumentException("Not allow named: "+name);
		}
		for(int i=0;i<names.length;i++){
			if(name.equals(names[i]))
				throw new IllegalArgumentException("Not allow named: "+name);
		}
	}
	private void loadConfigPart(String def) throws Exception{
		StringBuilder sb=new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<xmlform-config xmlns=\"urn:xmlform:config:1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:xmlform:config:1.0 xmlform-config-1.0.xsd\">");
		sb.append(def);
		sb.append("</xmlform-config>");
		InputSource is=new InputSource(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
		ConfigParseResult res = configurationFactory.parseConfig(is);
		configurationProvider.addFormInstances(new ArrayList(res.getForms().values()));
		configurationProvider.addTypeInstances(new ArrayList(res.getTypes().values()));
	}
	private void loadFormLayouts(Iterator it) throws Exception{
		while(it.hasNext()){
			Element f=(Element)it.next();
			StringBuilder sb=new StringBuilder();
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			sb.append("<form-layouts xmlns=\"urn:xmlform:form-layout:1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:xmlform:form-layout:1.0 xmlform-form-layout-1.0.xsd\">");
			String type=f.attributeValue("type");
			String form=f.attributeValue("form");
			if(type==null){
				f.addAttribute("id",f.attributeValue("form"));
				String xml=f.asXML();
				sb.append(xml);
			}else{
				String fieldStr=f.attributeValue("fields");
				if(fieldStr==null){
					throw new IllegalArgumentException("Not found filed attribute for layout: "+form);
				}
				String fields[]=fieldStr.split(",");
				sb.append("<form-layout id=\""+form+"\" form=\""+form+"\">\n");
				sb.append("<panel form=\""+form+"\">");
				if("grid".equals(type)){
					sb.append("<grid");
					appendAttrs(f,sb,new String[]{"hgap","vgap","halign","valign","hstretch","vstretch","minwidth","bestwidth","minheight","bestheight","mark","style",
							"cols","colwidths"});
					sb.append(">\n");
					for(int i=0;i<fields.length;i++){
						String part[]=parseField(fields[i]);
						if("#".equals(part[0])){
							sb.append("<spacer mark=\""+part[1]+"\"/>");
						}else{
							sb.append("<field name=\""+part[1]+"\"");
							if(part[2]!=null){
								sb.append(" colspan=\"").append(part[2]).append("\"");
							}
							if(part[3]!=null){
								sb.append(" rowspan=\"").append(part[3]).append("\"");
							}
							sb.append("/>");
						}
					}
					sb.append("</grid>\n");
				}else if("table".equals(type)){
					sb.append("<table");
					appendAttrs(f,sb,new String[]{"hgap","vgap","halign","valign","hstretch","vstretch","minwidth","bestwidth","minheight","bestheight","mark","style",
							"selector"});
					sb.append(">");
					StringBuilder impField=new StringBuilder();
					for(int i=0;i<fields.length;i++){
						if("(".equals(fields[i])&&(i+1)<fields.length){
							sb.append("<group><label><text>").append(fields[i+1]).append("</text></label>");
							i++;
							continue;
						}else if(")".equals(fields[i])){
							sb.append("</group>");
							continue;
						}
						String part[]=parseField(fields[i]);
						sb.append("<field name=\""+part[1]+"\"");
						sb.append("/>");
						if(!part[0].equals("-")){
							if(impField.length()>0){
								impField.append(",");
							}
							impField.append(part[1]);
						}
					}
					if("true".equals(f.attributeValue("aaaaaa"))){
						StringBuilder impForm=new StringBuilder();
						impForm.append("<form name=\"").append(form).append("Restrict\">");
						impForm.append("<import-field form=\"").append(form).append("\"><include-fields>");
						impForm.append(impField.toString());
						impForm.append("</include-fields></import-field></form>");
						loadConfigPart(impForm.toString());
					}
					sb.append("</table>");
				}
				sb.append("</panel>");
				sb.append("</form-layout>");
			}
			sb.append("</form-layouts>");
			InputSource is=new InputSource(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
			layoutProvider.addFormLayoutInstances(new ArrayList(formLayoutFactor.parseFormLayout(is).values()));
		}
	}
	private void appendAttrs(Element f,StringBuilder sb,String[] keys){
		for(int i=0;i<keys.length;i++){
			String key=keys[i];
			String selector=f.attributeValue(key);
			if(selector!=null){
				sb.append(" ").append(key).append("=\"").append(selector).append("\"");
			}
		}
	}
	private String createSimpleForm(String name,String fields){
		//!name:+m+int,sex:-m+string
		FormDefinition form=new FormDefinition();
		form.setName(name);
		form.getFields().putAll(configurationFactory.parseShorthandFields(fields.substring(1)));
		configurationProvider.addFormInstances(Collections.singletonList(form));
		return name;
	}
	private String [] parseField(String f){
		//************************ - ,filename,colspan,rowspan
		String part[]=new String[]{"","",null,null};
		if(f.startsWith("-")){
			part[0]="-";
			f=f.substring(1);
		}else if(f.startsWith("#")){
			part[0]="#";
			f=f.substring(1);
		}
		String part2[]=f.split(":");
		if(part2.length>1){
			Map vals=new HashMap();
			char sign='\0';
			StringBuilder val=new StringBuilder();
			for(int i=0;i<part2[1].length();i++){
				char c=part2[1].charAt(i);
				if(c=='c'||c=='r'){
					if(sign!='\0'&&val.length()>0){
						vals.put(sign, val.toString());
						val=new StringBuilder();
					}
					sign=c;
				}else if(sign!='\0'&&Character.isDigit(c)){
					val.append(c);
				}
			}
			if(sign!='\0'&&val.length()>0){
				vals.put(sign, val.toString());
				val=new StringBuilder();
			}
			if(vals.containsKey('c')){
				part[2]=(String)vals.get('c');
			}
			if(vals.containsKey('r')){
				part[3]=(String)vals.get('r');
			}
		}
		part[1]=part2[0];
		return part;
	}
	private List loadAnchors(Action act,Element ec){
		List trs=new ArrayList();
		Iterator it=ec.elementIterator();
		while(it.hasNext()){
			Element tele=(Element)it.next();
			Anchor t=createAnchor(act,tele);
			trs.add(t);
		}	
		
		return trs;
	}
	protected Anchor createAnchor(Action act,Element anchorElement){
		Anchor anc=new Anchor();
		String tagName=anchorElement.getName();
		Iterator ait=anchorElement.attributeIterator();
		Map optVals=new HashMap();
		optVals.put("sourcetype", act.getSourceType().toString().toLowerCase());
		while(ait.hasNext()){
			Attribute a=(Attribute)ait.next();
			String aName=a.getName();
			Object v=a.getValue();
			optVals.put(aName, v);
		}
		Iterator pit=anchorElement.elementIterator("option");
		while(pit.hasNext()){
			Element pe=(Element)pit.next();
			optVals.put(pe.attribute("name"),pe.getText());
		}
		
		anc.setLabel(InnerUtil.parseLabel(anchorElement));
		parseActionOptions(act,anc,optVals);
		
		return anc;
	}
	private void parseActionOptions(Action act,Anchor anc,Map optVals){
		String type=(String)optVals.remove(Anchor.TYPE);
		anc.setType(type);
		ActionAnchorType anchorType=(ActionAnchorType)actionAnchorTypes.get(type);
		if(anchorType==null)
			anchorType=unTypedActionAnchor;
		parseOptions(anchorType.getOptions(),anc,optVals);
		anchorType.postCreate(act, anc);
		anc.setAction(act.getName());
	}
	
	private void parseOptions(Option opts[],Anchor anc,Map optVals){
		anc.setIcon((String)optVals.remove("icon"));
		anc.setAttach((String)optVals.remove("attach"));
//		String preStr=(String)optVals.remove("previous");
//		if(preStr!=null)
//			anc.setPrevious(preStr.split(","));
		String labelStr=(String)optVals.get("label");
		if(labelStr!=null)
			anc.setLabel((I18NTexts)OptionParser.parseOpt(I18NTexts.class, labelStr));
		Object order=optVals.remove("order");
		if(order instanceof Integer)
			anc.setOrder((Integer)order);
		else if(order instanceof String){
			anc.setOrder(Integer.parseInt((String)order));
		}
		anc.setPage((String)optVals.remove("page"));
		String action=(String)optVals.remove("action");
		if(action!=null)
			anc.setAction(action);
		
		if(opts!=null){
			for(int i=0;i<opts.length;i++){
				Option opt=opts[i];
				if(opt.isRequired()&&!optVals.containsKey(opt.getName()))
					throw new IllegalArgumentException(anc.getType()+" anchor must provider option: "+opt.getName());
				String optStr=(String)optVals.remove(opt.getName());
				if(optStr==null)
					continue;
				anc.getOptions().put(opt.getName(),opt.parseOpt(optStr));
			}
		}
		Iterator it=optVals.keySet().iterator();
		while(it.hasNext()){
			String key=(String)it.next();
			Object val=optVals.get(key);
			if(val==null)
				continue;
			OptionParser.checkSupport(val.getClass());
			anc.getOptions().put(key,val);
		}
		
	}
	protected Map parseParams(String str){
		Map obj=new HashMap();
		if(str==null||str.length()==0)
			return obj;
		String lastForm=null;
		String parts[]=str.split(",");
		for(int i=0;i<parts.length;i++){
			String kv[]=parts[i].split(":");
			if(kv.length==1){
				obj.put(kv[0], kv[0]);
			}else if(kv.length==2){
				obj.put(kv[0], kv[1]);
			}
			String v=(String)obj.get(kv[0]);
			if(lastForm!=null&&v.startsWith("!")){
				obj.put(kv[0], lastForm+v);
			}else if(v.indexOf("!")>0){
				lastForm=v.substring(0, v.indexOf("!"));
			}
		}
		return obj;
	}
	
	private long refreshFragments(long last) {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshFragments2(file);
			}
		};
		return findFileByExt(last,".form.xml",v);
	}
	
	private void doRefreshFragments2(ClassResource file){
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(file.getInputStream());
			Element root=document.getRootElement();
			
			Iterator it=root.elementIterator("fragment");
			while(it.hasNext()){
				Element e=(Element)it.next();
				StringBuilder sb=new StringBuilder();
				Iterator eit=e.elementIterator();
				while(eit.hasNext()){
					Element ei=(Element)eit.next();
					sb.append(ei.asXML());
				}
				xmlEntities.put(e.attributeValue("name"), sb.toString());
			}
		}catch(Exception e){
			logger.error("Load form: "+this.getResourceName(file),e);
		}
	}
	
	private long refreshForms(long last) {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshForm2(file);
			}
		};
		return findFileByExt(last,".form.xml",v);
	}
	
	private void doRefreshForm2(ClassResource file){
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(file.getInputStream());
			Element root=document.getRootElement();
			
			loadForms(root.elementIterator("type"));
			loadForms(root.elementIterator("form"));
			loadFormLayouts(root.elementIterator("form-layout"));
		}catch(Exception e){
			logger.error("Load form: "+this.getResourceName(file),e);
		}
	}
	
	private long refreshLayout(long last) {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshLayout(file);
			}
		};
		return findFileByExt(last,".xfl.xml",v);
	}
	
	private void doRefreshLayout(ClassResource file) {
		try {
			layoutProvider.addFormLayoutInstances(new ArrayList(formLayoutFactor.parseFormLayout(new InputSource(file.getInputStream())).values()));
		} catch (Exception e) {
			logger.error("Load layout: "+getResourceName(file),e);
		}
	}
	private long refreshPage(long last) {
		long max=0;
		for(int i=0;i<jsCompiler.length;i++){
			final JsCompiler jc=jsCompiler[i];
			ClassResourceVisitor v=new ClassResourceVisitor(){
				public void visit(ClassResource file) {
					doRefreshPage(file,jc);
				}
			};
			long m=findFileByExt(last,jc.getPageFileType(),v);
			if(m>max)
				max=m;
		}
		return max;
	}
	private void doRefreshPage(ClassResource file,JsCompiler jc) {
		Page page;
		try {
			page = parsePage(new InputStreamReader(file.getInputStream(),"UTF-8"),jc);
			pageCache.put(page.getName(), page);
			jsClasses.put(page.getName(),page);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Load page: "+this.getResourceName(file),e);
		}
	}
	private Page parsePage(Reader reader,JsCompiler jc) throws Exception{
		BufferedReader br=new BufferedReader(reader);
		try {
			PageImpl page=new PageImpl();
			String line=br.readLine();
			int head=0;
			String pad="//\n";
			StringBuilder decl=new StringBuilder();
			StringBuilder body=new StringBuilder();
			while(line!=null){
				if(line.startsWith(DECLARE_BEGIN)){
					head=1;
					decl.append("{").append("\n");
					body.append(pad);
				}else if(line.startsWith(DECLARE_END)){
					head=2;
					decl.append("}").append("\n");
					parsePageHeader(page,decl.toString());
					body.append("//").append(page.getName()).append("\n");
				}else if(head==1){
					decl.append(line).append("\n");
					body.append(pad);
				}else if(head==2){
					body.append(line).append("\n");
				}
				line=br.readLine();
			}
			List ans=page.getAnchors();
			for(int i=0;i<ans.size();i++){
				Anchor an=(Anchor)ans.get(i);
				PageAnchorType type=(PageAnchorType)pageAnchorTypes.get(an.getType());
				if(type==null)
					type=unTypedPageAnchor;
				type.postCreate(page, an);
			}
			ans=page.getSelfAnchors();
			for(int i=0;i<ans.size();i++){
				Anchor an=(Anchor)ans.get(i);
				//an.setSelf(true);
				PageAnchorType type=(PageAnchorType)pageAnchorTypes.get(an.getType());
				if(type==null)
					type=unTypedPageAnchor;
				type.postCreate(page, an);
			}
			if(page.getName()==null)
				throw new Exception("Must provider page name");
			page.setBody(jc.compile(body.toString().trim()));
			return page;
		} catch (Exception e) {
			throw new Exception(e);
		}finally{
			br.close();
		}
	}
	private void parsePageHeader(PageImpl page,String head)throws Exception{
		try {
			JSONObject obj=new JSONObject(head);
			if(obj.has("forms")){
				page.setForms(obj.getString("forms"));
			}
			if(obj.has("title"))
				page.setTitle(InnerUtil.parseJsonI18NTexts(obj.get("title")));
			if(obj.has("name"))
				page.setName(obj.getString("name"));
//			if(obj.has("anchors")){
//				Object ans=obj.get("anchors");
//				if(ans instanceof JSONObject){
//					page.getAnchors().add(parsePageAnchor((JSONObject)ans));
//				}else if(ans instanceof JSONArray){
//					JSONArray ansArry=(JSONArray)ans;
//					for(int i=0;i<ansArry.length();i++){
//						page.getAnchors().add(parsePageAnchor(ansArry.getJSONObject(i)));
//					}
//				}
//			}
			parsePageAnchors(obj,"anchors",page.getAnchors());
			parsePageAnchors(obj,"selfanchors",page.getSelfAnchors());
			if(obj.has("class"))
				page.setPageObject(beanCreator.createBean(obj.getString("class")));
		} catch (JSONException e) {
			throw new Exception(e.getLocalizedMessage()+" "+head);
		}
	}
	private void parsePageAnchors(JSONObject obj,String key,List anchors) throws Exception{
		if(obj.has(key)){
			Object ans=obj.get(key);
			if(ans instanceof JSONObject){
				anchors.add(parsePageAnchor((JSONObject)ans));
			}else if(ans instanceof JSONArray){
				JSONArray ansArry=(JSONArray)ans;
				for(int i=0;i<ansArry.length();i++){
					anchors.add(parsePageAnchor(ansArry.getJSONObject(i)));
				}
			}
		}
	}
	private Anchor parsePageAnchor(JSONObject obj) throws Exception{
		Anchor anc=new Anchor();
		Map optVals=new HashMap();
		
		Iterator it=obj.keys();
		while(it.hasNext()){
			String key=(String)it.next();
			Object v=obj.get(key);
			if(v!=null)
				optVals.put(key, obj.get(key).toString());
		}
		String type=(String)optVals.remove(Anchor.TYPE);
		anc.setType(type);
		PageAnchorType anchorType=(PageAnchorType)pageAnchorTypes.get(type);
		if(anchorType==null)
			anchorType=unTypedPageAnchor;
		parseOptions(anchorType.getOptions(),anc,optVals);
		return anc;
	}
	private long refreshComponent(long last) {
		long max=0;
		for(int i=0;i<jsCompiler.length;i++){
			final JsCompiler jc=jsCompiler[i];
			ClassResourceVisitor v=new ClassResourceVisitor(){
				public void visit(ClassResource file) {
					doRefreshComponent(file,jc);
				}
			};
			long m=findFileByExt(last,jc.getComponentFileType(),v);
			if(m>max)
				max=m;
		}
		return max;
	}
	private void doRefreshComponent(ClassResource file,JsCompiler jc) {
		Component comp;
		try {
			comp = parseComponent(beanCreator,new InputStreamReader(file.getInputStream(),"UTF-8"),jc);
			components.put(comp.getName(),comp);
			jsClasses.put(comp.getName(),comp);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Load page: "+this.getResourceName(file),e);
		}
	}
	private Component parseComponent(BeanCreator beanCreater,Reader reader,JsCompiler jc) throws Exception{
		BufferedReader br=new BufferedReader(reader);
		try {
			ComponentImpl comp=new ComponentImpl();
			String line=br.readLine();
			int head=0;
			StringBuilder decl=new StringBuilder();
			StringBuilder body=new StringBuilder();
			while(line!=null){
				if(line.startsWith(DECLARE_BEGIN)){
					head=1;
					decl.append("{").append("\n");
				}else if(line.startsWith(DECLARE_END)){
					head=2;
					decl.append("}").append("\n");
					parseCompHeader(beanCreater,comp,decl.toString());
				}else if(head==1){
					decl.append(line).append("\n");
				}else if(head==2){
					body.append(line).append("\n");
				}
				line=br.readLine();
			}
			if(comp.getName()==null)
				throw new Exception("Must provider component name");
			comp.setBody(jc.compile(body.toString().trim()));
			return comp;
		} catch (Exception e) {
			throw new Exception(e);
		}finally{
			br.close();
		}
	}
	private void parseCompHeader(BeanCreator beanCreater,ComponentImpl page,String head)throws Exception{
		try {
			JSONObject obj=new JSONObject(head);
			if(obj.has("name"))
				page.setName(obj.getString("name"));
//			if(obj.has("class"))
//				page.setPageObject(beanCreater.createBean(obj.getString("class")));
		} catch (JSONException e) {
			throw new Exception(e.getLocalizedMessage()+" "+head);
		}
	}
	private long refreshJasperReport(long last) {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshJasperReport(file);
			}
		};
		return findFileByExt(last,".jrxml",v);
	}
	
	EntityResolver NoOpEntityResolver=new EntityResolver() {
	  public InputSource resolveEntity(String publicId, String systemId) {
	    return new InputSource(new StringBufferInputStream(""));
	  }
	};
	private void doRefreshJasperReport(ClassResource file){
		try{
			SAXReader reader = new SAXReader();
			reader.setEntityResolver(NoOpEntityResolver);
			Document document = reader.read(file.getInputStream());
			Element root=document.getRootElement();
			String name=root.attributeValue("name");
			if(name==null||name.length()==0){
				return ;
			}
			jasperDesignResources.put(name, file);
			jasperReports.remove(name);
		}catch(Exception e){
			logger.error("Load JasperReport: "+getResourceName(file),e);
		}
	}
	private long findFileByExt(long last,String ext,ClassResourceVisitor vis) {
		try{
			return findFileByExt(packagesToScan,last,ext,vis);
		}catch(IOException e){
			throw new IllegalStateException(e);
		}
	}
	static public long findFileByExt(String packagesToScan[],final long last,final String ext,final ClassResourceVisitor vis) throws IOException{
		final long ls[]=new long[1];
		ls[0]=last;
		ClassResourceVisitor prox=new ClassResourceVisitor(){
			public void visit(ClassResource cr) {
				if(last<cr.lastModified()&&(ext==null||cr.getName().endsWith(ext))){
					vis.visit(cr);
					if(cr.lastModified()>ls[0]){
						ls[0]=cr.lastModified();
					}
				}
			}
		};
		ClassResourceScanner.scan(packagesToScan, prox);
		return ls[0];
	}
//	public JasperDesign getJasperDesign(String name)throws Exception{
//		ClassResource res=(ClassResource)jasperDesignResources.get(name);
//		if(res==null)
//			throw new Exception("Not found report template: "+name);
//		return JRXmlLoader.load(res.getInputStream());
//	}
	public String getJasperDesignXml(String name)throws Exception{
		ClassResource res=(ClassResource)jasperDesignResources.get(name);
		if(res==null)
			return null;
		return IOUtils.toString(res.getInputStream());
	}
	public JasperReport getJasperReport(String name)throws Exception{
		JasperReport jr=(JasperReport)jasperReports.get(name);
		if(jr==null){
			ClassResource res=(ClassResource)jasperDesignResources.get(name);
			if(res==null)
				return null;
			try {
				JasperDesign jd=JRXmlLoader.load(res.getInputStream());
				jr=JasperCompileManager.compileReport(jd);
				jasperReports.put(name, jr);
				return jr;
			} catch (Exception e) {
				throw new Exception("Can't load report template: "+name+",beause "+e.getMessage(),e);
			}
		}
		return jr;
	}
}
