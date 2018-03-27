package sodium.category;

import sodium.category.impl.CategorizedNameImpl;

/**
 * @author Liu Zhikun
 */

final public class CategoryUtil {
	static private String CATEGORY_SUBFIX=".0";
	static public String createCategoryName(String name,String category){
		return name+"."+category+CATEGORY_SUBFIX;
	}
	static public CategorizedName parseCategoryName(String categorizedName){
		CategorizedNameImpl cn=new CategorizedNameImpl();
		cn.setName(categorizedName);
		if(categorizedName.endsWith(CATEGORY_SUBFIX)){
			String str=categorizedName.substring(0,categorizedName.length()-CATEGORY_SUBFIX.length());
			int last=str.lastIndexOf(".");
			String cat=str.substring(last+1);
			str=str.substring(0,last);
			cn.setCategory(cat);
			cn.setName(str);
			return cn;
		}
		return cn;
	}
}
