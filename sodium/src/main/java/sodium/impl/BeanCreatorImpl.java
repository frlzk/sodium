package sodium.impl;

import java.util.Iterator;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import sodium.engine.BeanCreator;

/**
 * @author Liu Zhikun
 */

public class BeanCreatorImpl implements BeanCreator,ApplicationContextAware {
	private ApplicationContext applicationContext;
	public Object createBean(String className) throws Exception{
		Object b=null;
		if(applicationContext.containsBean(className)){
			b=applicationContext.getBean(className);
		}
		if(b!=null)
			return b;
		
		Map beans=applicationContext.getBeansOfType(Class.forName(className));
		if(beans.size()>0){
			return beans.values().iterator().next();
		}
		
		Map objs=getApplicationContext().getBeansWithAnnotation(Component.class);
		Iterator it=objs.keySet().iterator();
		while(it.hasNext()){
			String k=(String)it.next();
			Object v=objs.get(k);
			Class realClass=AopUtils.getTargetClass(v);
			if(realClass.getName().equals(className))
				return v;
		}
		
		return null;
	}
	public void setApplicationContext(ApplicationContext ac)throws BeansException {
		this.applicationContext=ac;
	}
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	
}
