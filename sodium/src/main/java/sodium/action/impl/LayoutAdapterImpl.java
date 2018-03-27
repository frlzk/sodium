package sodium.action.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.xmlform.formlayout.component.FormLayout;
import sodium.action.LayoutAdapteContext;

public class LayoutAdapterImpl implements LayoutAdapter {
	private Object adapterInstance;
	private Method adapterMethod;
	
	public Object getAdapterInstance() {
		return adapterInstance;
	}

	public void setAdapterInstance(Object adapterInstance) {
		this.adapterInstance = adapterInstance;
	}

	public Method getAdapterMethod() {
		return adapterMethod;
	}

	public void setAdapterMethod(Method adapterMethod) {
		this.adapterMethod = adapterMethod;
	}
	public FormLayout adapte(LayoutAdapteContext context,FormLayout layout){
		try {
			return (FormLayout)adapterMethod.invoke(adapterInstance,context,layout);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adapterInstance == null) ? 0 : adapterInstance.hashCode());
		result = prime * result + ((adapterMethod == null) ? 0 : adapterMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LayoutAdapterImpl other = (LayoutAdapterImpl) obj;
		if (adapterInstance == null) {
			if (other.adapterInstance != null)
				return false;
		} else if (!adapterInstance.equals(other.adapterInstance))
			return false;
		if (adapterMethod == null) {
			if (other.adapterMethod != null)
				return false;
		} else if (!adapterMethod.equals(other.adapterMethod))
			return false;
		return true;
	}
	
	
}
