package sodium.engine;

import java.util.List;

/**
 * @author Liu Zhikun
 */

public interface MenuNode {
	public String getLabel();
	public String getPage();
	public List getChildMenus();
}
