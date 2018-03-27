package sodium.print;

import java.util.List;

import sodium.RequestContext;

public interface PrinterProvider {
	public List<PagePrinter> getPrinters(RequestContext reqCtx);
}
