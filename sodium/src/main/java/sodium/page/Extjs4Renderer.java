package sodium.page;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.xmlform.web.ResourceUtil;
import sodium.servlet.ConfigListener;
import sodium.servlet.FileUploadHandler;
import sodium.servlet.HandlerConfiguration;

/**
 * @author Liu Zhikun
 */

public class Extjs4Renderer implements PageContainerRenderer{
	private String html;
	public Extjs4Renderer(){
		StringBuilder sb=new StringBuilder();
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
		sb.append("<html><head>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<title></title>");
		html=sb.toString();
	}
	public String getDefaultTheme(){
		return "default";
	}
	public void outputResource(ServletContext servletContext,HttpServletRequest req, HttpServletResponse resp, String arg)throws Exception {
		ResourceUtil.outputInline(resp, "require.config({baseUrl:'"+(String)req.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH)+"/js'});");
		ResourceUtil.outputResource(resp,"net.sf.xmlform.web.LocaleValidatorText:evaljson.js,validation.js,expression.js,format.js,model.js,panel.js,OldNamespace.js,extjs/v4/FormPanel.js,extjs/v4/FormWidget.js,extjs/v4/RowEditor.js");
		ResourceUtil.outputInline(resp, "net.sf.xmlform.extjs.FileUploadUrl=\""+(String)req.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH)+"/upload\";");
//		ResourceUtil.outputClass(servletContext,req,resp, "net.sf.xmlform.web.LocaleValidatorText:"+arg);
		ResourceUtil.outputResource(resp, "sodium.page.Page:sodium.js");
		ResourceUtil.outputResource(resp, "sodium.page.Page:extjs4/extjspage.js,extjs4/echartsform.js");
		ResourceUtil.outputInline(resp,"sodium.page.BasePageImpl=sodium.page.BaseExtJsPage;");
		ResourceUtil.outputResource(resp,"sodium.page.Page:pageimpl.js,page.js");
		ResourceUtil.outputResource(resp,"sodium.page.Page:lib/lie.min.js");//与 require.js 重突
	}
	public String createHtml(PageContainerContext ctx,String initScript){
		StringBuilder sb=new StringBuilder(html);
		createStyleSheet(ctx,sb);
		createJavaScript(ctx,sb);
		sb.append("<script type=\"text/javascript\">define(\"sodium/window\",[\"sodium\",\"sodium/windowConfig\"],function(sodium,cfg){var c=").append(initScript).append(";sodium.init(c);return sodium.createWindowBuilder(c,cfg);});</script>");
		sb.append("</head>");
		createBodyContent(ctx,sb);
		sb.append("</html>");
		return sb.toString();
	}
	protected void createStyleSheet(PageContainerContext ctx,StringBuilder styleSheet){
		styleSheet.append(DoJo1_8Renderer.readFileSource(ctx,"/boot/extjs/4/"+ctx.getWindow()+"/"+"sodium.csses"));
	}
	protected void createJavaScript(PageContainerContext ctx,StringBuilder javascript){
		javascript.append(DoJo1_8Renderer.readFileSource(ctx,"/boot/extjs/4/"+ctx.getWindow()+"/"+"sodium.jses"));
	}
	protected void createBodyContent(PageContainerContext ctx,StringBuilder html){
		String fileHtml=DoJo1_8Renderer.readFileSource(ctx,"/boot/extjs/4/"+ctx.getWindow()+"/"+"sodium.body");
		if(fileHtml!=null)
			html.append(fileHtml);
		else
			html.append("<body></body>");
	}
}
