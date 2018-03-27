package sodium.print;

import sodium.action.PrintablePage;

/**
 * @author Liu Zhikun
 */

public interface PrintablePageRenderer {
	public boolean isSupport(PrintablePage page);
	public RenderedPage render(RenderContext pc,PrintablePage page)throws Exception;
}
