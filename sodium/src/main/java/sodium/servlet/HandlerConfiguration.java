package sodium.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class HandlerConfiguration {
	final static public String BASE_SERVLET_PATH="sodiumBaseServletPath";
	final static private String BASE_SERVLET_PATH_LEN="sodiumServletPathLen";
	private SpringHandler actionHandler,
	batchResourceHandler,fileDownloadHandler,
	fileUploadHandler,
	pageContainerHandler,
	pageHandler,
	printHandler,
	xmlformHandler,
	jasperreportsImageHandler
	;
	private String baseServletPath="na11",batchResourceSuffix=".br11";
	
	public String getBaseServletPath() {
		return baseServletPath;
	}

	public void setBaseServletPath(String baseServletPath) {
		this.baseServletPath = baseServletPath;
	}

	public String getBatchResourceSuffix() {
		return batchResourceSuffix;
	}

	public void setBatchResourceSuffix(String batchResourceSuffix) {
		this.batchResourceSuffix = batchResourceSuffix;
	}

	public SpringHandler getActionHandler() {
		return actionHandler;
	}

	public void setActionHandler(SpringHandler actionHandler) {
		this.actionHandler = actionHandler;
	}

	public SpringHandler getBatchResourceHandler() {
		return batchResourceHandler;
	}

	public void setBatchResourceHandler(SpringHandler batchResourceHandler) {
		this.batchResourceHandler = batchResourceHandler;
	}

	public SpringHandler getFileDownloadHandler() {
		return fileDownloadHandler;
	}

	public void setFileDownloadHandler(SpringHandler fileDownloadHandler) {
		this.fileDownloadHandler = fileDownloadHandler;
	}

	public SpringHandler getFileUploadHandler() {
		return fileUploadHandler;
	}

	public void setFileUploadHandler(SpringHandler fileUploadHandler) {
		this.fileUploadHandler = fileUploadHandler;
	}

	public SpringHandler getWindowHandler() {
		return pageContainerHandler;
	}

	public void setWindowHandler(SpringHandler pageContainerHandler) {
		this.pageContainerHandler = pageContainerHandler;
	}

	public SpringHandler getJavascriptHandler() {
		return pageHandler;
	}

	public void setJavascriptHandler(SpringHandler pageHandler) {
		this.pageHandler = pageHandler;
	}

	public SpringHandler getPrintHandler() {
		return printHandler;
	}

	public void setPrintHandler(SpringHandler printHandler) {
		this.printHandler = printHandler;
	}

	public SpringHandler getFormHandler() {
		return xmlformHandler;
	}

	public void setFormHandler(SpringHandler xmlformHandler) {
		this.xmlformHandler = xmlformHandler;
	}

	public SpringHandler getJasperreportsImageHandler() {
		return jasperreportsImageHandler;
	}

	public void setJasperreportsImageHandler(SpringHandler jasperreportsImageHandler) {
		this.jasperreportsImageHandler = jasperreportsImageHandler;
	}
	public static String getRequestPath(HttpServletRequest req,String trim){
		int len=(Integer)req.getServletContext().getAttribute(BASE_SERVLET_PATH_LEN);
		String name=req.getRequestURI().substring(len);
		int trimLen=trim.length()+2;
		if(name.length()<=trimLen){
			return "";
		}
		return name.substring(trimLen);
	}
	
	public static void setBaseServletPath(ServletContext sc,String baseServletPath) {
		String path=baseServletPath;
		if(!path.startsWith("/")){
			path="/"+path;
		}
		if(path.endsWith("/")){
			path=path.substring(0, path.length()-1);
		}
		String ctxPath=sc.getContextPath();
		String fullPath=path;
		if(fullPath.length()>1){
			fullPath=ctxPath+path;
		}
		sc.setAttribute(BASE_SERVLET_PATH, fullPath);
		sc.setAttribute(BASE_SERVLET_PATH_LEN, fullPath.length());
	}
	
}
