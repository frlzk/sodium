package sodium.action.impl;

import net.sf.xmlform.config.TypeDefinition;
import sodium.action.TypeAdapteContext;

/**
 * @author Liu Zhikun
 */

public interface TypeAdapter {
	public TypeDefinition adapte(TypeAdapteContext context,TypeDefinition form);
}
