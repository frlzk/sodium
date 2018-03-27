package sodium.page;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Liu Zhikun
 */


final public class PageContainerRenderers {
	static private PageContainerRenderer ext3=new Extjs3Renderer();
	static private PageContainerRenderer ext4=new Extjs4Renderer();
	static private PageContainerRenderer dojo1_8=new DoJo1_8Renderer();
	static private PageContainerRenderer dojo1_10=new DoJo1_10Renderer();
	static private Map renderers=new HashMap();
	static{
		renderers.put("extjs3", ext3);
		renderers.put("extjs4", ext4);
		renderers.put("dojo1_8", dojo1_8);
		renderers.put("dojo1_10", dojo1_10);
	}
	static public PageContainerRenderer extjs3(){
		return ext3;
	}
	static public PageContainerRenderer extjs4(){
		return ext4;
	}
	static public PageContainerRenderer dojo1_8(){
		return dojo1_8;
	}
	static public PageContainerRenderer getPageRendererByName(String name){
		return (PageContainerRenderer)renderers.get(name);
	}
}
