package sodium.print.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.RequestContext;
import sodium.engine.Configuration;
import sodium.print.PagePrinter;
import sodium.print.PrintService;
import sodium.print.PrinterProvider;
import sodium.print.printservice.ExportPrintService;
import sodium.print.printservice.HtmlPrintService;
import sodium.print.printservice.PreviewPrintService;

public class DefaultPrinterProvider implements PrinterProvider {
	private Configuration configuration;
	
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	public List<PagePrinter> getPrinters(RequestContext reqCtx) {
		return createPrinters(reqCtx);
	}
	protected List createPrinters(RequestContext reqCtx){
		List pps=new ArrayList();
		PagePrinter pp=createPrinter(reqCtx,PreviewPrintService.DEFAULT_NAME);
		if(pp!=null)
			pps.add(pp);
		pp=createPrinter(reqCtx,HtmlPrintService.DEFAULT_NAME);
		if(pp!=null)
			pps.add(pp);
		pp=createPrinter(reqCtx,ExportPrintService.DEFAULT_NAME);
		if(pp!=null)
			pps.add(pp);
		return pps;
	}
	protected PagePrinter createPrinter(RequestContext reqCtx,String printServiceName){
		PrintService ps=(PrintService)configuration.getPrintServices().get(printServiceName);
		if(ps==null)
			return null;
		return new PagePrinter(ps.getLabel().getText(reqCtx.getLocale()),ps);
	}
}
