package sodium.print.renderer;

import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;

import sodium.action.PrintablePage;
import sodium.print.InputStreamPrintablePage;
import sodium.print.PrintablePageRenderer;
import sodium.print.RenderContext;
import sodium.print.RenderedPage;
import sodium.print.impl.RenderedPageImpl;

/**
 * @author Liu Zhikun
 */

public class InputStreamPrintablePageRenderer implements PrintablePageRenderer {
	public boolean isSupport(PrintablePage page){
		return InputStreamPrintablePage.class.equals(page.getClass());
	}
	public RenderedPage render(RenderContext pc,PrintablePage page)throws Exception{
		InputStreamPrintablePage jpp=(InputStreamPrintablePage)page;
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		IOUtils.copy(jpp.getInputStream(),bos);
		return new RenderedPageImpl(jpp.getFileName(),jpp.getMimeType(),bos.toByteArray());
	}
}
