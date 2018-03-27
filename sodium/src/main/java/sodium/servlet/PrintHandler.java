package sodium.servlet;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import net.sf.xmlform.XMLFormException;
import net.sf.xmlform.data.format.DataJSONFormat;
import sodium.RequestContext;
import sodium.action.PrintablePage;
import sodium.engine.Configuration;
import sodium.print.PagePrinter;
import sodium.print.PrintService;
import sodium.print.PrintablePageRenderer;
import sodium.print.RenderedPage;
import sodium.print.impl.PrintContextImpl;
import sodium.print.impl.RenderContextImpl;

/**
 * @author Liu Zhikun
 */


public class PrintHandler extends AbstractHandler {
	private static final String PARAMS_KEY = PrintHandler.class.getName()+"_param";
	private Configuration configuration=null;
	public void init(ApplicationContext app,ServletContext s){
		super.init(app,s);
//		HttpSessionListener hsl=new HttpSessionListener(){
//			public void sessionCreated(HttpSessionEvent evt) {
//				evt.getSession().setAttribute(UUID_KEY, UUID.randomUUID().toString());
//			}
//			public void sessionDestroyed(HttpSessionEvent evt) {
//				evt.getSession().getServletContext().removeAttribute((String)evt.getSession().getAttribute(UUID_KEY));
//			}
//		};
//		getServletContext().addListener(hsl);
		configuration=(Configuration)getBeanByType(Configuration.class,true);
	}
	
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//POST
		resp.setContentType("application/json; charset=UTF8");
		try{
			RequestContext context = createRequestContext(req);
			if (context.isLogin() == false){
				resp.getWriter().write(DataJSONFormat.buildError(XMLFormException.CE_SESSSION,"Session Timeout"));
				return ;
			}
			String str= req.getParameter("data");//readRequestString(req);
			if(str==null){
				str=readRequestString(req);
			}else{
				String encoding = req.getCharacterEncoding();
				if (encoding == null)
					encoding = "UTF-8";
				str=new String(str.getBytes("iso-8859-1"),encoding);
			}
			JSONObject param = new JSONObject(str);
			String printService=null;
			if(param.has("service"))
				printService=param.getString("service");
			if(printService==null){
				resp.getWriter().write(DataJSONFormat.buildError(XMLFormException.CE_FORM_DATA,"Need service argument"));
				return ;
			}
			PrintService pp=(PrintService)configuration.getPrintServices().get(printService);
			if(pp==null){
				resp.getWriter().write(DataJSONFormat.buildError(XMLFormException.CE_FORM_DATA,"Not found printer: "+printService));
				return ;
			}
			doPrint(context,req,resp,str,param,pp);
//			if(!pp.isDownload()){
//				doPrint(context,req,resp,str,param,pp);
//				return;
//			}
//			req.getSession(true).setAttribute(PARAMS_KEY,str);
//			String pid=(String)req.getSession().getAttribute(UUID_KEY);
//			getServletContext().setAttribute(pid, str);
//			Cookie cookie = new Cookie("SODIUMPRINTID", pid);
//			resp.addCookie(cookie);
//			resp.getWriter().write(DataJSONFormat.buildKeyValue("pid", pid,""));
		}catch (Exception e) {
			e.printStackTrace();
			resp.getOutputStream().write(e.getLocalizedMessage().getBytes());
		}
	}
	
//	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//		try {
//			RequestContext context = createRequestContext(req);
//			String pid=ConfigListener.getServletPath(req, "print");
//			if (pid==null&&context.isLogin() == false){
//				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized");
//				return;
//			}
//			String paramStr = (String) req.getSession(true).getAttribute(PARAMS_KEY);
//			if(paramStr==null){
//				paramStr=(String)getServletContext().getAttribute(pid);
//			}
//			if (paramStr == null) {
//				resp.getWriter().print("Print parameter has timeout.");
//				return;
//			}
//			JSONObject param = new JSONObject(paramStr);
//			String printerName=param.getString("printer");
//			PagePrinter pp=(PagePrinter)configuration.getPagePrinters().get(printerName);
//			doPrint(context,req,resp,paramStr,param,pp);
//		} catch (Exception e) {
//			e.printStackTrace();
//			resp.getOutputStream().write(e.getLocalizedMessage().getBytes());
//		}
//	}
	protected void doPrint(RequestContext context,HttpServletRequest req, HttpServletResponse resp,String paramStr,JSONObject param,PrintService pagePrinter) throws Exception {
		PrintablePage ps=getEngine().buildPrintablePage(context, paramStr);
		if(ps==null){
			printEmpty(context,req,resp);
			return;
		}
		RenderContextImpl pci=new RenderContextImpl();
		if(param.has("title"))
			pci.setTitle(param.getString("title"));
		if(param.has("format"))
			pci.setFormat(param.getString("format"));
		String printer=null;
		if(param.has("printer"))
			printer=param.getString("printer");
		pci.setServletContext(req.getServletContext());
		pci.setServletRequest(req);
		pci.setServletResponse(resp);
		
		List renderers=configuration.getPrintablePageRenderers();
		for(int i=0;i<renderers.size();i++){
			PrintablePageRenderer ppp=(PrintablePageRenderer)renderers.get(i);
			if(ppp.isSupport(ps)){
				RenderedPage rp=ppp.render(pci, ps);
				if(rp==null){
					printEmpty(context,req,resp);
					return ;
				}
				PrintContextImpl prctx=new PrintContextImpl();
				prctx.setPrinter(printer);
				prctx.setServletContext(req.getServletContext());
				prctx.setServletRequest(req);
				prctx.setServletResponse(resp);
				prctx.setRequestContext(context);
				pagePrinter.print(prctx, rp);
				return ;
			}
		}
	}
	protected void printEmpty(RequestContext context,HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
	}
}
