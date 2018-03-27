package sodium.print;

import net.sf.xmlform.util.I18NTexts;

public class PagePrinter {
	private String name;
	private PrintService printService;
	public PagePrinter(String name,PrintService printService) {
		super();
		this.name = name;
		this.printService = printService;
	}
	public String getName() {
		return name;
	}
	public PrintService getPrintService() {
		return printService;
	}
}
