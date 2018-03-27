package app.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import sodium.RequestContext;
import sodium.engine.RequestContextCreator;

public class RequestContextCreatorImpl implements RequestContextCreator,ApplicationContextAware {
	private ApplicationContext ac;
	public RequestContext createRequestContext(HttpServletRequest httpServletRequest) {
		return new RequestContextImpl(ac,httpServletRequest.getServletContext(),httpServletRequest);
	}

	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		this.ac=ac;
	}

}
