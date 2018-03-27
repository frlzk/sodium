package sodium.print.renderer;


import sodium.action.PrintablePage;
import sodium.print.ByteArrayPrintablePage;
import sodium.print.PrintablePageRenderer;
import sodium.print.RenderContext;
import sodium.print.RenderedPage;
import sodium.print.impl.RenderedPageImpl;

/**
 * @author Liu Zhikun
 */

public class ByteArrayPrintablePageRenderer implements PrintablePageRenderer {
	public boolean isSupport(PrintablePage page){
		return ByteArrayPrintablePage.class.equals(page.getClass());
	}
	public RenderedPage render(RenderContext pc,PrintablePage page)throws Exception{
		ByteArrayPrintablePage jpp=(ByteArrayPrintablePage)page;
		byte[] data=jpp.getByteArray();
		return new RenderedPageImpl(jpp.getFileName(),jpp.getMimeType(),data);
	}
}
