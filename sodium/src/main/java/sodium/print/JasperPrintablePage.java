package sodium.print;

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JasperPrint;
import sodium.action.PrintablePage;

/**
 * @author Liu Zhikun
 */

public class JasperPrintablePage implements PrintablePage {
	private List<JasperPrint> jasperPrint;
	public JasperPrintablePage(JasperPrint ps){
		jasperPrint=new ArrayList();
		jasperPrint.add(ps);
	}
	public JasperPrintablePage(List<JasperPrint> ps){
		jasperPrint=ps;
	}
	public List<JasperPrint> getJasperPrints() {
		return jasperPrint;
	}
}
