package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.xmlform.XMLFormException;
import net.sf.xmlform.data.format.DataJSONFormat;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.form.format.FormJSONFormat;
import net.sf.xmlform.format.JSONConstants;
import net.sf.xmlform.formlayout.LayoutDescriptor;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.format.FormLayoutJSONFormat;
import sodium.RequestContext;
import sodium.engine.Engine;

/**
 * @author Liu Zhikun
 */

public class XmlformHandler extends AbstractHandler{
	private static Logger logger = LoggerFactory.getLogger(FileDownloadHandler.class);
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//GET
		String formName=HandlerConfiguration.getRequestPath(req, "form");
		resp.setContentType("text/json; charset=UTF8");
		try{
			resp.getWriter().print(buildResult(formName,req));
		}catch(Exception e){
			logger.error("Query xmlform: "+formName,e);
			resp.getWriter().print(DataJSONFormat.buildError(XMLFormException.CE_FORM_DATA, e.getMessage()));
		}
	}
	protected String buildResult(String formName,HttpServletRequest req)throws Exception{
		return buildXmlFormJson(getEngine(),createRequestContext(req),formName);
	}
	static String buildXmlFormJson(Engine engine,RequestContext ctx,String formName)throws Exception{
		JSONObject obj=new JSONObject();
		obj.put(JSONConstants.VERSION, JSONConstants.VERSION_VALUE);
		JSONObject headObj=new JSONObject();
		obj.put(JSONConstants.HEAD, headObj);
		headObj.put(JSONConstants.FAULT_CODE, XMLFormException.OK);
		headObj.put(JSONConstants.FAULT_STRING, "");
		JSONObject bodyObj=new JSONObject();
		obj.put(JSONConstants.BODY, bodyObj);
		
		XMLForm form=engine.getXmlformPort().getForm(ctx, formName);
		if(form!=null){
			String formJson=new FormJSONFormat(form).getJSONString();
			JSONObject json=new JSONObject(formJson);
			bodyObj.put("form", json);
		}
		LayoutDescriptor des[]=engine.getXmlformLayoutPort().getFormLayouts(ctx, formName);
		if(des.length>0){
			FormLayout layout=engine.getXmlformLayoutPort().getFormLayout(ctx, des[0].getId());
			JSONObject json=new JSONObject(new FormLayoutJSONFormat(ctx.getLocale(),layout).getJSONString());
			bodyObj.put("layout", json);
		}
		return obj.toString();
	}

}
