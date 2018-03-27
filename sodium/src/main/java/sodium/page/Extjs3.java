package sodium.page;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import net.sf.xmlform.web.ClassResource;

/**
 * @author Liu Zhikun
 */

public class Extjs3 implements ClassResource{
	public void outputResource(ServletContext servletContext,HttpServletRequest req, HttpServletResponse resp, String arg)throws Exception {
		new Extjs3Renderer().outputResource(servletContext, req, resp, arg);
	}
}
