package app.impl;


import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;

public class MSSqlDialect extends SQLServerDialect {
	public MSSqlDialect() {
		super();
		registerFunction( "locate", new SQLFunctionTemplate( Hibernate.INTEGER, "charindex(?1, ?2, ?3)" ) );
	}
}
