package sodium.page;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import net.sf.xmlform.util.MessageUtil;
import net.sf.xmlform.web.ResourceUtil;
import sodium.servlet.HandlerConfiguration;

/**
 * @author Liu Zhikun
 */

public class DoJo1_10Renderer implements PageContainerRenderer{
	private String defaultTheme="claro";
	
	public String getDefaultTheme() {
		return defaultTheme;
	}

	public void setDefaultTheme(String defaultTheme) {
		this.defaultTheme = defaultTheme;
	}

	public void outputResource(ServletContext servletContext,HttpServletRequest req, HttpServletResponse resp, String arg)throws Exception {
		ResourceUtil.outputResource(resp,"net.sf.xmlform.web.LocaleValidatorText:evaljson.js,validation.js,expression.js,format.js,model.js,panel.js,OldNamespace.js,dojo/v1_10/RecordSetStore.js,dojo/v1_10/FormWidget.js,dojo/v1_10/Layout.js,dojo/v1_10/FormPanel.js,dojo/v1_10/ConditionTree.js");
//		ResourceUtil.outputClass(servletContext,req,resp, "net.sf.xmlform.web.LocaleValidatorText:"+arg);
		ResourceUtil.outputResource(resp, "sodium.page.Page:sodium.js");
		ResourceUtil.outputResource(resp, "sodium.page.Page:dojo/echartsform.js,dojo/page.js");
		ResourceUtil.outputResource(resp,"sodium.page.Page:pageimpl.js,page.js");
	}
	public String createHtml(PageContainerContext ctx,String initScript){
		String windowPath="/boot/dojo/"+getVersion()+"/"+ctx.getWindow();
		File file=new File(ctx.getServletContext().getRealPath(windowPath));
		if(!file.exists()){
			return "404 Not found window: "+ctx.getWindow();
		}
		StringBuilder sb=new StringBuilder(createHeadBegin(ctx));
		sb.append("<script type=\"text/javascript\">define(\"sodium/window\",[\"sodium\",\"sodium/windowConfig\"],function(sodium,cfg){var c=").append(initScript).append(";sodium.init(c);return sodium.createWindowBuilder(c,cfg);});</script>");
		sb.append("</head>");
		createBodyContent(ctx,sb);
		sb.append("</html>");
		return sb.toString();
	}
	private String createHeadBegin(PageContainerContext ctx){
		String ctxPath=getContextPath(ctx);
		StringBuilder sb=new StringBuilder();
		sb.append("<html><head>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		sb.append("<meta name=\"Build-Version\" content=\""+ctx.getBuildVersion()+"\">\n");
		sb.append("<title></title>\n");
		createStyleSheet(ctx,sb);
//		sb.append("<style type=\"text/css\">\nhtml, body { height: 100%; margin: 0; overflow: hidden; padding: 0; } \n</style>\n");
//		sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"dojo/"+getVersion()+"/profiles/profiles.css\" />");
		sb.append("<script type=\"text/javascript\">var dojoConfig = {\n");
		sb.append("async: true,tlmSiblingOfDojo: false,\n");
		sb.append("baseUrl: \""+(String)ctx.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH)+"/js\",\n");
		sb.append("packages: [\n");
		sb.append("{ name: \"dojo\", location: \""+ctxPath+"/lib/dojo/"+getVersion()+"/dojo\" },\n");
		sb.append("{ name: \"dijit\", location: \""+ctxPath+"/lib/dojo/"+getVersion()+"/dijit\" },\n");
		sb.append("{ name: \"dojox\", location: \""+ctxPath+"/lib/dojo/"+getVersion()+"/dojox\" },\n");
		sb.append("{ name: \"gridx\", location: \""+ctxPath+"/lib/dojo/"+getVersion()+"/gridx\" }\n");
		sb.append(" ]\n");
		sb.append("};</script>\n");
		createJavaScript(ctx,sb);
		return sb.toString();
	}
	public static String getContextPath(PageContainerContext ctx){
		String ctxPath=ctx.getServletRequest().getContextPath();
		if("/".equals(ctxPath))
			ctxPath="";
		return ctxPath;
	}
	public static String readFileSource(PageContainerContext ctx,String path) {
		try{
			File file=new File(ctx.getServletContext().getRealPath(path));
			if(!file.exists())
				return null;
			String str=FileUtils.readFileToString(file,"UTF-8");
			return formatMessage(ctx,str);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getLocalizedMessage(),e);
		}
	}
	private static String formatMessage(PageContainerContext ctx,String str){
		return MessageUtil.formatMessage(str,new ContainerMessageParameters(ctx));
	}
	protected void createStyleSheet(PageContainerContext ctx,StringBuilder styleSheet){
		styleSheet.append(readFileSource(ctx,"/boot/dojo/"+getVersion()+"/"+ctx.getWindow()+"/sodium.csses"));
	}
	protected void createJavaScript(PageContainerContext ctx,StringBuilder javascript){
		javascript.append(readFileSource(ctx,"/boot/dojo/"+getVersion()+"/"+ctx.getWindow()+"/sodium.jses"));
	}
	protected void createBodyContent(PageContainerContext ctx,StringBuilder html){
		String fileHtml=readFileSource(ctx,"/boot/dojo/"+getVersion()+"/"+ctx.getWindow()+"/sodium.body");
		if(fileHtml!=null)
			html.append(fileHtml);
		else{
			html.append(formatMessage(ctx,"<body class=\"${theme}\"></body>"));
		}
	}
	protected String getVersion(){
		return "1.10";
	}
}
