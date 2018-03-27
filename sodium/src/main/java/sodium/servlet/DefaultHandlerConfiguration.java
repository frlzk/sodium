package sodium.servlet;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

public class DefaultHandlerConfiguration extends HandlerConfiguration {
	public DefaultHandlerConfiguration(ApplicationContext app,ServletContext servletContext){
		ActionHandler as=new ActionHandler();
		as.init(app,servletContext);
		setActionHandler(as);
		
		BatchResourceHandler br=new BatchResourceHandler();
		//br.init(app,servletContext);
		setBatchResourceHandler(br);
		
		FileDownloadHandler fd=new FileDownloadHandler();
		fd.init(app,servletContext);
		setFileDownloadHandler(fd);
		
		FileUploadHandler fu=new FileUploadHandler();
		fu.init(app,servletContext);
		setFileUploadHandler(fu);
		
		PageContainerHandler pc=new PageContainerHandler();
		pc.init(app,servletContext);
		setWindowHandler(pc);
		
		PageHandler p=new PageHandler();
		p.init(app,servletContext);
		setJavascriptHandler(p);
		
		PrintHandler pr=new PrintHandler();
		pr.init(app,servletContext);
		setPrintHandler(pr);
		
		XmlformHandler xf=new XmlformHandler();
		xf.init(app,servletContext);
		setFormHandler(xf);
		
		JasperreportsImageHandler jri=new JasperreportsImageHandler();
		setJasperreportsImageHandler(jri);
	}
}
