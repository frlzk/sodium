package sodium.print.renderer;


import sodium.action.PrintablePage;
import sodium.print.JasperPrintablePage;
import sodium.print.PrintablePageRenderer;
import sodium.print.RenderContext;
import sodium.print.RenderedPage;
import sodium.print.impl.PrintUtil;

/**
 * @author Liu Zhikun
 */

public class JasperPrintablePageRenderer implements PrintablePageRenderer {
	public boolean isSupport(PrintablePage page){
		return JasperPrintablePage.class.equals(page.getClass());
	}
	public RenderedPage render(RenderContext pc,PrintablePage page)throws Exception{
		JasperPrintablePage jpp=(JasperPrintablePage)page;
		if(jpp.getJasperPrints().size()==0)
			return null;
		return PrintUtil.renderPrint(pc.getTitle(),pc.getFormat(), jpp.getJasperPrints(),pc.getServletRequest());
	}
}
