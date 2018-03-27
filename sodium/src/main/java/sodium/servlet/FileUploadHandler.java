package sodium.servlet;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.xmlform.XMLFormException;
import net.sf.xmlform.XMLFormPort;
import net.sf.xmlform.format.JSONConstants;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import sodium.RequestContext;
import sodium.engine.Permission;
import sodium.file.File;
import sodium.file.FileManager;
import sodium.file.FileUploadController;

/**
 * @author Liu Zhikun
 */

public class FileUploadHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);
	static int MAX_SIZE=1024*1024*1024;
	XMLFormPort xmlformPort;
	private FileManager fileManager;
	private FileUploadController fileUploadController;
	public void init(ApplicationContext app,ServletContext s){
		super.init(app,s);
		fileManager=(FileManager)getBeanByType(FileManager.class);
		fileUploadController=(FileUploadController) getBeanByType(FileUploadController.class,false);
	}
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//POST
		RequestContext ctx=createRequestContext(req);
		JSONObject obj=new JSONObject();
		try {
			obj.put(JSONConstants.FAULT_CODE, XMLFormException.OK);
			obj.put(JSONConstants.FAULT_STRING, "");
			if(ctx.isLogin()==false){
				obj.put(JSONConstants.FAULT_CODE, XMLFormException.CE_SESSSION);
				obj.put(JSONConstants.FAULT_STRING, "Not login");
				resp.getWriter().write(obj.toString());
				return ;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		req.setCharacterEncoding("UTF-8");
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		//factory.setSizeThreshold(max_size);
		//factory.setRepository(yourTempDirectory);
		ServletFileUpload upload = new ServletFileUpload(factory);
		long max=MAX_SIZE;
		if(fileUploadController!=null)
			max=fileUploadController.getMaxFileSize(ctx);
		upload.setSizeMax(max);

		List items;
		try {
			items = upload.parseRequest(req);
		} catch (FileUploadException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
		JSONArray ids=new JSONArray();
		
		try{
			for(int i=0;i<items.size();i++){
				FileItem item = (FileItem) items.get(i);
			    if (!item.isFormField()) {
			    	String fieldName = item.getFieldName();
			        String fileName = item.getName();
			        String contentType = item.getContentType();
			        boolean isInMemory = item.isInMemory();
			        long sizeInBytes = item.getSize();
			        String id=UUID.randomUUID().toString().replace("-", "");
			        File fi=new File();
			        fi.setId(id);
			        fi.setData(item.get());
			        fi.setMimeType(item.getContentType());
			        fi.setName(item.getName());
			        if(fileUploadController!=null){
			        	Permission res=fileUploadController.checkUpload(ctx, fi);
			        	if(res!=Permission.GRANTED){
			        		obj.put(JSONConstants.FAULT_CODE,res==Permission.NOLOGIN? XMLFormException.CE_SESSSION:XMLFormException.CE_ACTION_PERMISSION);
							obj.put(JSONConstants.FAULT_STRING, res==Permission.NOLOGIN?"Not login":"No permission");
							resp.getWriter().write(obj.toString());
							return ;
			        	}
			        }
			        fileManager.save(fi);
			        try {
			        	JSONObject f=new JSONObject();
				        f.put("id",id);
				        f.put("name", item.getName());
				        f.put("type", item.getContentType());
				        ids.put(f);
					} catch (JSONException e) {
						e.printStackTrace();
					}
			    }
			    item.delete();
			}
		}catch(Exception e){
			logger.error("File upload",e);
			try {
				obj.put(JSONConstants.FAULT_CODE, XMLFormException.SE_APPLICATION);
				obj.put(JSONConstants.FAULT_STRING, e.getLocalizedMessage());
			} catch (JSONException e1) {
			}
			writeResult(req,resp,obj.toString());
			return ;
		}
		
		try {
			obj.put("files", ids);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		writeResult(req,resp,obj.toString());
	}
	protected void writeResult(HttpServletRequest req,HttpServletResponse resp,String str) throws IOException{
		String f=req.getParameter("accept");
		if(f==null||"json".equals(f)){
			resp.setContentType("application/json; charset=UTF8");  //在firefox 中有问题
			resp.getWriter().write(str);
		}else if("html".equals(f)){
			StringBuilder sb=new StringBuilder();
			sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
			sb.append("<html><head>");
			sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
			sb.append("</head><body><textarea>").append(str).append("</textarea></body></html>");
			resp.setContentType("text/html; charset=UTF8");
			resp.getWriter().write(str);
		}
	}
}
