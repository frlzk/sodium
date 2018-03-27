package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import net.sf.xmlform.web.BatchResourceServlet;
import sodium.engine.Sampler;

/**
 * @author Liu Zhikun
 */

public class ConfigListener implements ServletContextListener{
	
	private class InnerServlet extends HttpServlet{
		SpringHandler sh;
		public InnerServlet(SpringHandler hand){
			sh=hand;
		}
		public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			Sampler.reset();
			sh.handleRequest(req, resp);
		}
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
	}

	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc=sce.getServletContext();
		ApplicationContext app=WebApplicationContextUtils.getWebApplicationContext(sc);
		HandlerConfiguration hc=createConfig(app,sc);
		
		String path=doGetServletPath(hc,sc);
		hc.setBaseServletPath(path);
		hc.setBaseServletPath(sc, hc.getBaseServletPath());
		
		addHandler(sce,"sodiumFormServlet",path+"/form/*",hc.getFormHandler());
		addHandler(sce,"sodiumActionServlet",path+"/action/*",hc.getActionHandler());
		addHandler(sce,"sodiumWindowServlet",path+"/window/*",hc.getWindowHandler());
		addHandler(sce,"sodiumJavascriptServlet",path+"/js/*",hc.getJavascriptHandler());
		addHandler(sce,"sodiumUploadServlet",path+"/upload",hc.getFileUploadHandler());
		addHandler(sce,"sodiumFileServlet",path+"/file/*",hc.getFileDownloadHandler());
		addHandler(sce,"sodiumPrintServlet",path+"/print/*",hc.getPrintHandler());
		addHandler(sce,"sodiumJasperreportsImageServlet",path+"/jasperreportsimage",hc.getJasperreportsImageHandler());
		String batchPath=hc.getBatchResourceSuffix();
		if(!batchPath.startsWith("*"))
			batchPath="*"+batchPath;
		addHandler(sce,"sodiumBatchResourceServlet",batchPath,hc.getBatchResourceHandler());
		
	}
	
	protected HandlerConfiguration createConfig(ApplicationContext app,ServletContext servletContext){
		return new DefaultHandlerConfiguration(app,servletContext);
	}
	private void addHandler(ServletContextEvent sce,String name,String mapping,SpringHandler hand){
		Dynamic dyn = sce.getServletContext().addServlet(name, new InnerServlet(hand));
		dyn.setLoadOnStartup(1);
		dyn.addMapping(mapping);
	}
	
	private String doGetServletPath(HandlerConfiguration hc,ServletContext sc){
		String path=sc.getInitParameter(HandlerConfiguration.BASE_SERVLET_PATH);
		if(path==null){
			path=hc.getBaseServletPath();
		}
		if(!path.startsWith("/")){
			path="/"+path;
		}
		if(path.endsWith("/")){
			path=path.substring(0, path.length()-1);
		}
		
		return path;
	}
	
}
