package sodium.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

public class SpringHandlerMapping extends AbstractHandlerMapping {
	private HandlerConfiguration servletConfiguration;
	private Map handMap=new HashMap();
	
	protected void initServletContext(ServletContext servletContext) {
		super.initServletContext(servletContext);
		servletConfiguration=createConfig(this.getApplicationContext(),servletContext);
		servletConfiguration.setBaseServletPath(servletContext, servletConfiguration.getBaseServletPath());
		applyConfig();
	}

	public String getBaseServletPath(){
		return servletConfiguration.getBaseServletPath();
	}
	
	public void setBaseServletPath(String baseServletPath) {
		servletConfiguration.setBaseServletPath(baseServletPath);
		servletConfiguration.setBaseServletPath(this.getServletContext(), baseServletPath);
	}

	protected Object getHandlerInternal(HttpServletRequest req) throws Exception {
		String path=req.getRequestURI();
		String base=(String)this.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH);
		if(path.endsWith(servletConfiguration.getBatchResourceSuffix())){
			return servletConfiguration.getBatchResourceHandler();
		}else if(path.startsWith(base)){
			String type=path.substring(base.length()+1);
			int idx=type.indexOf("/");
			if(idx>=0)
				type=type.substring(0, idx);
			return handMap.get(type);
		}
		return null;
	}
	
	protected HandlerConfiguration createConfig(ApplicationContext app,ServletContext servletContext){
		return new DefaultHandlerConfiguration(app,servletContext);
	}
	private void applyConfig(){
		HandlerConfiguration sc=this.servletConfiguration;
		handMap.put("action", sc.getActionHandler());
		handMap.put("file", sc.getFileDownloadHandler());
		handMap.put("upload", sc.getFileUploadHandler());
		handMap.put("window", sc.getWindowHandler());
		handMap.put("js", sc.getJavascriptHandler());
		handMap.put("print", sc.getPrintHandler());
		handMap.put("form", sc.getFormHandler());
		handMap.put("jasperreportsimage", sc.getJasperreportsImageHandler());
	}
}
