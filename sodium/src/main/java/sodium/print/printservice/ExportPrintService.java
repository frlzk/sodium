package sodium.print.printservice;


import java.util.Locale;

import org.apache.commons.io.IOUtils;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.print.PrintContext;
import sodium.print.PrintFormat;
import sodium.print.PrintService;
import sodium.print.RenderedPage;
import sodium.servlet.FileDownloadHandler;

public class ExportPrintService implements PrintService {
	public static final String DEFAULT_NAME="export";
	private String name=DEFAULT_NAME;
	private I18NTexts label=new I18NTexts();
	private PrintFormat[] formats=new PrintFormat[]{
			PrintFormats.PDF,
			PrintFormats.WORD,
			PrintFormats.EXCEL
			};
	public ExportPrintService(){
		label.put(new I18NText(Locale.ENGLISH,"Export"));
		label.put(new I18NText(Locale.CHINESE,"导出"));
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public I18NTexts getLabel() {
		return label;
	}
	public void setLabel(I18NTexts label) {
		this.label = label;
	}
	public PrintFormat[] getFormats(){
		return formats;
	}
	public void setFormats(PrintFormat[] formats) {
		this.formats = formats;
	}

	public boolean isDownload() {
		return true;
	}
	public void print(PrintContext context, RenderedPage page)throws Exception {
		context.getServletResponse().setContentType(page.getContentType());
		context.getServletResponse().setContentLength(page.getContentLength());
		context.getServletResponse().setHeader("Content-Disposition","attachment; filename=\""+ FileDownloadHandler.encodingFileName(page.getFileName() )+"\"");
		IOUtils.copy(page.getInputStream(), context.getServletResponse().getOutputStream());
		context.getServletResponse().getOutputStream().flush();
	}

}
