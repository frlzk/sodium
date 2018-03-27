package sodium.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SpringHandler {
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
