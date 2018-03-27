package sodium.print;

import net.sf.xmlform.util.I18NTexts;

public interface PrintService {
	public String getName();
	public I18NTexts getLabel();
	public boolean isDownload();
	public PrintFormat[] getFormats();
	public void print(PrintContext context,RenderedPage page)throws Exception;
}
