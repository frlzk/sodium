package sodium.engine;

import java.util.List;

/**
 * @author Liu Zhikun
 */

public interface ActionNode {
	public String getName();
	public String getLabel();
	public String getRole();
	public String[] getPartners();
	public List getChildActions();
}
