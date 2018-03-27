package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sodium.RequestContext;
import sodium.category.CategorizedName;
import sodium.category.CategoryUtil;
import sodium.engine.Sampler;
import sodium.page.JsClass;
import sodium.page.BuildJsContextImpl;

/**
 * @author Liu Zhikun
 */

public class PageHandler extends AbstractHandler {
	public void handleRequest(final HttpServletRequest req,final HttpServletResponse resp)
			throws IOException, ServletException {
		getEngine().tryRefresh();
		String pageName =HandlerConfiguration.getRequestPath(req, "js");
		pageName = pageName.substring(0, pageName.lastIndexOf(".")).replace("/", ".");
		if(pageName.startsWith("xmlform."))
			throw new ServletException("Can not prefix is xmlform");
		final RequestContext reqCtx=createRequestContext(req);
		int sid=Sampler.begin("processPage");
		try {
			processPage(req,resp,reqCtx,pageName);
		} catch (Exception e) {e.printStackTrace();
			throw new ServletException(e.getLocalizedMessage(),e);
		}
		Sampler.end(sid);
	}
	private void processPage(HttpServletRequest req, HttpServletResponse resp,RequestContext reqCtx,String pageName)
			throws IOException {
		BuildJsContextImpl ctx = new BuildJsContextImpl();
		ctx.setFormPort(getEngine().getXmlformPort());
		ctx.setLayoutPort(getEngine().getXmlformLayoutPort());
		ctx.setEngine(getEngine());
		ctx.setRequestContext(reqCtx);
		JsClass page = getEngine().getJsClass(pageName);
		resp.addHeader("Cache-Control", "no-cache");
		if (page != null) {
			resp.setContentType("text/javascript; charset=UTF-8");
			CategorizedName catName=CategoryUtil.parseCategoryName(pageName);
			ctx.setName(catName.getName());
			ctx.setCategory(catName.getCategory());
			resp.getWriter().println(page.buildJsClass(ctx));
//			resp.getWriter().println(";sodium.page._islogin=");
//			resp.getWriter().println(reqCtx.isLogin());
//			resp.getWriter().println(";");
		}else{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
