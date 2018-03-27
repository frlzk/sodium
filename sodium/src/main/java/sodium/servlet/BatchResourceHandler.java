package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

public class BatchResourceHandler extends net.sf.xmlform.web.BatchResourceServlet implements SpringHandler {
	private ServletContext sc;
	public void init(ApplicationContext app,ServletContext s){
		sc=s;
	}
	public ServletContext getServletContext() {
		return sc;
	}

	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.service(req, resp);
	}

}
