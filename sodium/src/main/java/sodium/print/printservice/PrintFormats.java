package sodium.print.printservice;

import java.util.Locale;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.print.PrintFormat;

public class PrintFormats {
	static public final PrintFormat HTML=createFormat("html",Locale.ENGLISH,"预览");
	static public final PrintFormat PDF=createFormat("pdf",Locale.ENGLISH,"PDF");
	static public final PrintFormat WORD=createFormat("doc",Locale.ENGLISH,"WORD");
	static public final PrintFormat EXCEL=createFormat("xls",Locale.ENGLISH,"EXCEL");
	static private PrintFormat createFormat(String f,Object... lables){
		PrintFormat pf=new PrintFormat();
		pf.setFormat(f);
		I18NTexts i18n=new I18NTexts();
		pf.setLabel(i18n);
		for(int i=0;i<lables.length;i+=2){
			i18n.put(new I18NText((Locale)lables[i],(String)lables[i+1]));
		}
		return pf;
	}
}
