package sodium.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import sodium.RequestContext;
import sodium.engine.Engine;
import sodium.engine.RequestContextCreator;
import sodium.engine.Sampler;

/**
 * @author Liu Zhikun
 */

abstract public class AbstractHandler implements SpringHandler{
	private ApplicationContext applicationContext;
	private Engine engine;
	private RequestContextCreator requestContextCreator;
	private ServletContext sc;
	public void init(ApplicationContext app,ServletContext s){
		applicationContext = app;
		sc=s;
		engine=(Engine) getBeanByType(Engine.class);
		requestContextCreator=(RequestContextCreator) getBeanByType(RequestContextCreator.class);
	}
	protected RequestContext createRequestContext(HttpServletRequest req){
		return requestContextCreator.createRequestContext(req);
	};
	protected Engine getEngine(){
		return engine;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	protected String readRequestString(HttpServletRequest req)
			throws UnsupportedEncodingException, IOException {
		String encoding = req.getCharacterEncoding();
		if (encoding == null)
			encoding = "UTF-8";
		InputStreamReader reader = new InputStreamReader(req.getInputStream(),
				encoding);
		int len;
		char buffer[] = new char[1024];
		len = reader.read(buffer);
		StringBuffer sb = new StringBuffer();
		while (len != -1) {
			sb.append(buffer, 0, len);
			len = reader.read(buffer);
		}
		return sb.toString();
	}
	protected Object getBeanByType(Class cal){
		return getBeanByType(cal,true);
	}
	protected List getBeansByType(Class cal){
		Map objs=applicationContext.getBeansOfType(cal);
		return new ArrayList(objs.values());
	}
	protected Object getBeanByType(Class cal,boolean thr){
		String names[]=BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext,cal);//applicationContext.getBeanNamesForType(cal);
		if(names.length==0){
			if(thr==true)
				throw new IllegalArgumentException("Not found bean type of "+cal.getName()+" in spring ApplicationContext");
			return null;
		}
		Object res= applicationContext.getBean(names[0]);
		return res;
	}
}
