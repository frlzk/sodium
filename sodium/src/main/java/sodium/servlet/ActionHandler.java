package sodium.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.xmlform.XMLFormException;
import net.sf.xmlform.data.ResultData;
import net.sf.xmlform.data.format.DataJSONFormat;
import net.sf.xmlform.format.JSONConstants;
import sodium.RequestContext;
import sodium.engine.Sampler;
import sodium.impl.InnerJSONDataSource;


/**
 * @author Liu Zhikun
 */


public class ActionHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(ActionHandler.class);
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//POST
		String accept=req.getHeader("Accept");
		boolean isCompact=true;
		if(accept!=null&&(accept.contains(JSONConstants.MIME_FLAT)))
			isCompact=false;
		
		resp.setContentType("application/json; charset=UTF8");
		RequestContext context = createRequestContext(req);
		String actionName =HandlerConfiguration.getRequestPath(req, "action").replace("/", ".");
		int sid=Sampler.begin("processAction");
		getEngine().tryRefresh();
		long startTime=System.currentTimeMillis();
		String reqStr = readRequestString(req);
		String resultStr=null;
		try{
			if ("reference".equals(actionName)) {
				JSONArray array = new JSONArray(reqStr);
				for (int i = 0; i < array.length(); i++) {
					JSONObject unit = array.getJSONObject(i);
					String json=null;
					try{
						json=executeJson(context,unit.getString("action"),unit.getJSONObject("data").toString(),isCompact);
					}catch(XMLFormException e){
						DataJSONFormat jsonResult = new DataJSONFormat(e);
						json=getEngine().getXmlformPort().formatData(context, jsonResult);
					} catch (Exception e) {
						json= DataJSONFormat.buildError(XMLFormException.SE_APPLICATION, e.getLocalizedMessage());
					}
					unit.put("data", new JSONObject(json));
				}
				resultStr=array.toString();
			} else {
				resultStr= executeJson(context,actionName,reqStr,isCompact);
			}
		}catch(XMLFormException e){
			DataJSONFormat jsonResult = new DataJSONFormat(e);
			resultStr=getEngine().getXmlformPort().formatData(context, jsonResult);
		} catch (Exception e) {
			if(logger.isDebugEnabled()){
				logger.debug("Request action: "+actionName,e);
			}
			resultStr= DataJSONFormat.buildError(XMLFormException.SE_APPLICATION, e.getLocalizedMessage());
		}
		
		resp.getWriter().print(resultStr);
		long endTime=System.currentTimeMillis();
		Sampler.end(sid);
		if(logger.isDebugEnabled()){
			logger.debug("Request action: "+actionName+ " "+(endTime-startTime)+" " + reqStr+"\nResult data: " + resultStr);
		}
	}
	private String executeJson(RequestContext context,String actionName,String data,boolean isCompact){
		int asid=Sampler.begin(actionName);
		ResultData dataResult=getEngine().executeAction(context, actionName, new InnerJSONDataSource(data));
		getEngine().resolveReference(context, dataResult.getForm(), dataResult.getData());
		DataJSONFormat jsonResult = new DataJSONFormat(dataResult,isCompact);
		Sampler.end(asid);
		return getEngine().getXmlformPort().formatData(context, jsonResult);
	}
}
