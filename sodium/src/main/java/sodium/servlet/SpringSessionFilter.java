package sodium.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class SpringSessionFilter implements Filter,ApplicationContextAware {
	private WebApplicationContext applicationContext;
	private String configLocation;
	private SessionRepositoryFilter sessionRepositoryFilter;
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		applicationContext=(WebApplicationContext)ac;
	}
	
	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
	@PostConstruct
	public void init(){
		if(configLocation==null||configLocation.length()==0)
			return;
		XmlWebApplicationContext webApp=new XmlWebApplicationContext();
		webApp.setServletContext(applicationContext.getServletContext());
		webApp.setConfigLocations(configLocation);
		webApp.setParent(applicationContext);
		webApp.refresh();
		Map srfs=webApp.getBeansOfType(SessionRepositoryFilter.class);
		if(srfs.size()==0)
			throw new IllegalArgumentException("Not found bean: "+SessionRepositoryFilter.class.getName());
		sessionRepositoryFilter=(SessionRepositoryFilter)srfs.values().iterator().next();
	}
	public void init(FilterConfig config) throws ServletException {
		
	}
	
	public void destroy() {
		
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)throws IOException, ServletException {
		if(sessionRepositoryFilter!=null){
			sessionRepositoryFilter.doFilter(req, resp, chain);
		}else
			chain.doFilter(req, resp);
	}
}
