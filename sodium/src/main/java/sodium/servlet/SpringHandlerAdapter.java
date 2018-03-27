package sodium.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import sodium.engine.Sampler;

public class SpringHandlerAdapter implements HandlerAdapter {

	public long getLastModified(HttpServletRequest req, Object handler) {
		return -1L;
	}

	public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
		SpringHandler h=(SpringHandler)handler;
		Sampler.reset();
		h.handleRequest(req, resp);
		return null;
	}

	public boolean supports(Object handler) {
		return handler instanceof SpringHandler;
	}

}
