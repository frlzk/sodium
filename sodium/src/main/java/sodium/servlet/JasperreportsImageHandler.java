package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.j2ee.servlets.ImageServlet;

public class JasperreportsImageHandler extends ImageServlet implements SpringHandler {

	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.service(req, resp);
	}

}
