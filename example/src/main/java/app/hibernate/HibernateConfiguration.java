package app.hibernate;

import org.hibernate.DuplicateMappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.PersistentClass;

public class HibernateConfiguration extends Configuration {
	protected class CfgMappingsImpl extends MappingsImpl{
		public void addClass(PersistentClass persistentClass) throws DuplicateMappingException {
			classes.remove(persistentClass.getEntityName());
			super.addClass(persistentClass);
		}
	}
	public Mappings createMappings() {
		return new CfgMappingsImpl();
	}
	
}
