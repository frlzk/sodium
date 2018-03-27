package sodium.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import sodium.RequestContext;
import sodium.engine.Permission;
import sodium.file.File;
import sodium.file.FileDownloadController;
import sodium.file.FileManager;

/**
 * @author Liu Zhikun
 */

public class FileDownloadHandler extends AbstractHandler{
	private static Logger logger = LoggerFactory.getLogger(FileDownloadHandler.class);
	private FileManager fileManager;
	private FileDownloadController fileDownloadController;
	public void init(ApplicationContext app,ServletContext s){
		super.init(app,s);
		fileManager=(FileManager)getBeanByType(FileManager.class);
		fileDownloadController=(FileDownloadController) getBeanByType(FileDownloadController.class,false);
	}
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//GET
		RequestContext ctx=createRequestContext(req);
		String fid=HandlerConfiguration.getRequestPath(req, "file");
		if(fileDownloadController!=null){
			Permission res=fileDownloadController.checkDownload(ctx, fid);
			if(res!=Permission.GRANTED){
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}
		
		File file=null;
		try {
			file = fileManager.load(fid);
		} catch (Exception e) {
			logger.error("File download",e);
		}
		if(file==null){
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		byte data[]=file.getData();
		resp.setContentType(file.getMimeType());
		resp.setContentLength(data.length);
		resp.setHeader("Content-Disposition","attachment; filename=\""+encodingFileName(file.getName())+"\"");
		resp.getOutputStream().write(data);
		resp.getOutputStream().flush();
	}
	
	public static String encodingFileName(String fileName) { 
        String returnFileName = ""; 
        try {
            returnFileName = new String(fileName.getBytes("utf-8"), "ISO8859-1"); 
        } catch (UnsupportedEncodingException e) { 
            e.printStackTrace(); 
        } 
        return returnFileName; 
    } 
}
