package sodium.print.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sodium.engine.Engine;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;


/**
 * @author Liu Zhikun
 */


public class FormReportUtil {
	static public JasperPrint buildPrintablePage(Engine engine,String template,Map parameters,List fieldValues){
		try{
			JasperReport jr=engine.getConfiguration().getJasperReport(template);
			if(jr==null){
				return buildErrorPrintablePage("Not found JasperReport template: "+template);
			}
			return buildPrintablePage(engine,jr,parameters,fieldValues);
		}catch(Exception e){
			e.printStackTrace();
			return buildErrorPrintablePage(e.getLocalizedMessage());
		}
		
	}
	static public JasperPrint buildPrintablePage(Engine engine,JasperReport jr,Map parameters,List fieldValues){
		if(parameters==null){
			parameters=new HashMap();
		}
		try{
			parameters.put("ORGANIZATION_NAME", engine.getSetting().getCompanyName());
			return buildJasperPrint(engine,jr,parameters,fieldValues);
		}catch(Exception e){
			e.printStackTrace();
			return buildErrorPrintablePage(e.getLocalizedMessage());
		}
	}
	static private JasperPrint buildJasperPrint(Engine engine,JasperReport jr,Map parameters,List fieldValues) throws Exception{
		if(fieldValues==null||fieldValues.size()==0){
			return JasperFillManager.fillReport(jr, parameters);
		}else{
			return JasperFillManager.fillReport(jr, parameters,new JRMapCollectionDataSource(fieldValues));
		}
	}
	static public JasperPrint buildErrorPrintablePage(String error){
		JasperDesign design = new JasperDesign();
		design.setName("sodium.JasperReportError");
		design.setPageWidth(500);
		design.setPageHeight(400);
		design.setOrientation(OrientationEnum.PORTRAIT);
		design.setLeftMargin(5);
		design.setRightMargin(5);
		design.setTopMargin(5);
		design.setBottomMargin(5);
		design.setColumnCount(1);
		design.setColumnWidth(80);
		design.setColumnSpacing(0);	
		design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
		
		JRDesignBand titleBand = new JRDesignBand();
		titleBand.setHeight(200);
		JRDesignStaticText staticText = new JRDesignStaticText();
		staticText.setText(error);
		staticText.setWidth(380);
		staticText.setHeight(200);
		titleBand.addElement(staticText);
		design.setTitle(titleBand);
		try {
			return JasperFillManager.fillReport(JasperCompileManager.compileReport(design), new HashMap());
		} catch (JRException e) {
			throw new IllegalStateException(e);
		}
	}
}
