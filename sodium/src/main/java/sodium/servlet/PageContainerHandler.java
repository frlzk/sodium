package sodium.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import net.sf.xmlform.web.LocaleValidatorText;
import net.sf.xmlform.web.RequestMessageParameters;
import sodium.RequestContext;
import sodium.engine.MenuNode;
import sodium.engine.Sampler;
import sodium.engine.SessionAttributes;
import sodium.page.JsClass;
import sodium.page.PageContainerContextImpl;
import sodium.page.PageContainerRenderer;
import sodium.print.PagePrinter;
import sodium.print.PrintFormat;
import sodium.util.Util;

/**
 * @author Liu Zhikun
 */

public class PageContainerHandler extends AbstractHandler {
	private LocaleValidatorText localeValidatorText=new LocaleValidatorText();
	private PageContainerRenderer pageRenderer;
	public void init(ApplicationContext app,ServletContext s){
		super.init(app,s);
		pageRenderer=getEngine().getConfiguration().getPageContainerRenderer();
		if(pageRenderer==null){
			throw new IllegalStateException("Not configure PageRenderer.");
		}
	}
	protected PageContainerRenderer getPageRenderer(){
		return pageRenderer;
	}
	public void handleRequest(final HttpServletRequest req,final HttpServletResponse resp)
			throws IOException, ServletException {
		getEngine().tryRefresh();
		final RequestContext reqCtx=createRequestContext(req);
		try {
			int sid=Sampler.begin("processWindow");
			processPage(req,resp,reqCtx);
			Sampler.end(sid);
		} catch (Exception e) {
			throw new ServletException(e.getLocalizedMessage(),e);
		}
	}
	private void processPage(HttpServletRequest req, HttpServletResponse resp,RequestContext reqCtx)
			throws IOException {
		String pageName=HandlerConfiguration.getRequestPath(req, "window").replace("/", ".");
		int idx=pageName.indexOf(".");
		if(idx<1){
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
			
		String window=pageName.substring(0, idx);
		pageName=pageName.substring(idx+1);
		
		String pageMenu=req.getParameter("w-menu");
		String collapseMenu=req.getParameter("w-menucollapse");
		String firstPageBox[]=new String[]{null,null};
		String menuStr=(pageMenu!=null?buildUserMenu(reqCtx,pageMenu,firstPageBox,pageName,"false".equals(collapseMenu)||"f".equals(collapseMenu)||"0".equals(collapseMenu)?false:true).toString():"null");
		if(pageMenu!=null){
			if(firstPageBox[1]==null){
				if(firstPageBox[0]!=null)
					pageName=firstPageBox[0];
			}
		}
		
		JsClass page = getEngine().getJsClass(pageName);
		JSONObject pageParams[] = buildParam(req,resp,window, pageName);
		resp.setContentType("text/html; charset=UTF-8");
		resp.addHeader("Cache-Control", "no-cache");
		
		String init;
		if (page == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}else{
			init=buildInitializeScript(reqCtx, page.getName(),req.getContextPath(),pageParams[0].toString(),pageParams[1].toString(),pageParams[2].toString(),buildSessionAttributes(reqCtx),(String)req.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH),menuStr,buildePrinters(reqCtx));
		}
		String theme=this.getPageRenderer().getDefaultTheme();
		try{
			theme=pageParams[0].getString(RequestMessageParameters.KEY_THEME);
		}catch(Exception e){
			
		}
		PageContainerContextImpl ctxIml=new PageContainerContextImpl();
		ctxIml.setRequestContext(reqCtx);
		ctxIml.setWindow(window);
		ctxIml.setServletRequest(req);
		ctxIml.setServletContext(req.getServletContext());
		ctxIml.setBuildVersion(getEngine().getSetting().getBuildVersion());
		ctxIml.setSessionAttributes(getEngine().getSessionAttributes());
		ctxIml.setTheme(theme);
		printHtml(resp, getPageRenderer().createHtml(ctxIml,init));
	}

	private String buildSessionAttributes(RequestContext reqCtx) {
		JSONObject sb = new JSONObject();
		SessionAttributes sas=getEngine().getSessionAttributes();
		String names[]=sas.getNames(reqCtx);
		for(int i=0;i<names.length;i++) {
			String name = names[i];
			try {
				Object v=sas.getValue(reqCtx, name);
				sb.put(name, v);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	private String buildePrinters(RequestContext reqCtx){
		try {
			List names=getEngine().getPrinterProvider().getPrinters(reqCtx);
			JSONArray printers=new JSONArray();
			Iterator it=names.iterator();
			while(it.hasNext()){
				PagePrinter pp=(PagePrinter)it.next();
				PrintFormat[] pf=pp.getPrintService().getFormats();
				if(pf.length==0){
					continue;
				}else if(pf.length==1){
					JSONObject pfObj=new JSONObject();
					pfObj.put("printer", pp.getName());
					pfObj.put("service", pp.getPrintService().getName());
					pfObj.put("format", pf[0].getFormat());
					pfObj.put("label",pp.getName());
					pfObj.put("download",pp.getPrintService().isDownload());
					printers.put(pfObj);
				}else if(pf.length>1){
					JSONObject pfObj=new JSONObject();
					pfObj.put("label",pp.getName());
					JSONArray formats=new JSONArray();
					pfObj.put("children", formats);
					printers.put(pfObj);
					for(int i=0;i<pf.length;i++){
						JSONObject fObj=new JSONObject();
						fObj.put("printer", pp.getName());
						fObj.put("service", pp.getPrintService().getName());
						fObj.put("format", pf[i].getFormat());
						fObj.put("label",pf[i].getLabel().getText(reqCtx.getLocale()));
						fObj.put("download",pp.getPrintService().isDownload());
						formats.put(fObj);
					}
				}
			}
			return printers.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "[]";
	}

	private String getParameter(HttpServletRequest req, String name) {
		String value = req.getParameter(name);
		try {
			if (value == null)
				return null;
			return new String(value.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void printHtml(HttpServletResponse resp, String html)
			throws IOException {
		resp.getWriter().println(html);
	}

	private String buildInitializeScript(RequestContext ctx, String pageClass,String ctxPath,String winCfg,String pageCfg,String pageArgs,String sessionAttributes,String baseServletPath,String menuStr,String printers) {
		StringBuilder sb=new StringBuilder("");
		sb.append("{windowConfig:").append(winCfg)
		.append(",pageConfig:").append(pageCfg)
		.append(",pageParams:").append(pageArgs)
		.append(",pageClass:\"").append(pageClass).append("\"")
		.append(",contextPath:\"").append(ctxPath).append("\"")
		.append(",locale:\"").append(ctx.getLocale()).append("\"")
		.append(",baseServletPath:\"").append(baseServletPath).append("\"")
		.append(",systemName:\"").append(getEngine().getSetting().getSystemName()).append("\"")
		.append(",companyName:\"").append(getEngine().getSetting().getCompanyName()).append("\"")
		.append(",systemVersion:\"").append(getEngine().getSetting().getSystemVersion()).append("\"")
		.append(",buildVersion:\"").append(getEngine().getSetting().getBuildVersion()).append("\"")
		.append(",windowMenu:").append(menuStr)
		.append(",sessionAttributes:").append(sessionAttributes)
		.append(",pagePrinters:").append(printers)
		.append(",typeTextes:").append(localeValidatorText.buildValidatorText(ctx.getLocale()))
		.append("}");
		return sb.toString();
	}

	private JSONObject[] buildParam(HttpServletRequest req,HttpServletResponse resp,String window, String pageName) {
		JSONObject winParams = new JSONObject();
		JSONObject pageParams = new JSONObject();
		JSONObject pageArgs = new JSONObject();
		Enumeration names = req.getParameterNames();
		try {
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				if (name.startsWith("w-")) {
					winParams.put(name.substring(2), getParameter(req, name));
				} else if (name.startsWith("p-")) {
					pageParams.put(name.substring(2), getParameter(req, name));
				} else if (name.startsWith("a-")) {
					pageArgs.put(name.substring(2), getParameter(req, name));
				}
			}
			
			winParams.put("window", window);
			
			String theme=null;
			if(winParams.has(RequestMessageParameters.KEY_THEME)){
				theme=winParams.getString(RequestMessageParameters.KEY_THEME);
			}else{
				theme=getPageRenderer().getDefaultTheme();
				winParams.put(RequestMessageParameters.KEY_THEME, theme);
			}
			try{
				RequestContextUtils.getThemeResolver(req).setThemeName(req, resp, theme);
			}catch(Exception e){
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new JSONObject[] {winParams, pageParams, pageArgs };
	}

	private JSONArray buildUserMenu(RequestContext ctx,String page,String[] firstPageBox,String firstPage,boolean collapse) {
		List menus = getEngine().getMenuNodes(ctx,page==null?null:page.split(","));
		for(int i=0;i<menus.size();i++)
			getFirstPage((MenuNode)menus.get(i),firstPageBox,firstPage);
		return Util.menuNodeToJson(menus,collapse);
	}
	private void getFirstPage(MenuNode menu,String pageBox[],String fpage){
		if(menu.getPage()!=null){
			if(pageBox[0]==null){
				pageBox[0]=menu.getPage();				
			}
			if(menu.getPage().equals(fpage)){
				pageBox[1]=fpage;
				return;
			}
		}
		List menus=menu.getChildMenus();
		int size=menus.size();
		for(int i=0;i<size;i++)
			getFirstPage((MenuNode)menus.get(i),pageBox,fpage);
	}
}
