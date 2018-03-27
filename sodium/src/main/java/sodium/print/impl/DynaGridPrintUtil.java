package sodium.print.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;





import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBoxPen;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignRectangle;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.VerticalAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.xmlform.data.impl.FieldTypeFacet;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.type.BaseTypes;
import net.sf.xmlform.type.DateTimeType;
import net.sf.xmlform.type.IType;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.json.JSONException;

import sodium.engine.Engine;
import sodium.print.impl.ColumnModel;


public class DynaGridPrintUtil {

	private final static int textWidth = 80;
	
	private final static int textHeight = 20;
	
	private final static int fontSize = 12;
	
	private final static int titleFontSize = 18;

	private final static int columnHeaderHeight = 20;
	
	private final static boolean isColumnHeaderFontBond = true;
	
	private final static int titleHeight = 40;

	private final static int detailHeight = 20;
	
	private final static int pageFooterHeight = 20;
	
	private static final String fontName = "宋体";
	
	private static final String pdfFontName = "STSong-Light";
	
	private static final String pdfEncoding = "UniGB-UCS2-H";
	final static int LABEL_MUL=18;

	public static JasperReport getDynamicJasperReport(Engine engine,List<JasperDesign> designs,String title,List columnModel,int maxLevel,int detailSize) throws JRException{
		JasperDesign design = getDynamicJasperDesign(engine,title, columnModel,maxLevel,detailSize);
		designs.add(design);
		return JasperCompileManager.compileReport(design);
	}

	private static void parseTitleText(JRDesignStaticText titleText,int totalWidth){
		titleText.setHorizontalAlignment(HorizontalAlignEnum.CENTER);
		titleText.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
		titleText.setHeight(titleHeight);
		titleText.setWidth(totalWidth);
		titleText.setX(0);
		titleText.setY(0);
	}
	
	private static JasperDesign getDynamicJasperDesign(Engine engine,String title,List columnModel,int maxLevel,int detailSize) throws JRException{
		int totalWidth = 0;
		 for(int i=0;i<columnModel.size();i++){
			 ColumnModel cm=(ColumnModel)columnModel.get(i);
			 if(cm.isHide()){
				 continue;
			 }
			 totalWidth += cm.getBoxWidth();
		 }
		
		JasperDesign design = getJasperDesign("QueryList", true,totalWidth);
		JRDesignBand titleBand = getJRDesignBand(titleHeight);
		JRDesignBand columnHeaderBand = getJRDesignBand(columnHeaderHeight);
		columnHeaderBand.setHeight(maxLevel*columnHeaderHeight);
		JRDesignBand detailBand = getJRDesignBand(detailHeight);
		JRDesignBand pageFooter = getJRDesignBand(pageFooterHeight);
		 getPageFooterTextField(engine,pageFooter,totalWidth);
		setBand(design, titleBand, columnHeaderBand, detailBand, pageFooter);
		 JRDesignStaticText titleText = getJRDesignStaticText(title, titleFontSize, true);		
		 parseTitleText(titleText,totalWidth);
		 titleBand.addElement(titleText);
		 
		 addDetailColumn(columnModel, design, columnHeaderBand, detailBand,0,0);
		 design.setPageHeight(titleHeight+columnHeaderHeight+detailSize+pageFooterHeight+130);
		return design;
	}
	private static void addDetailColumn(List columnModel, JasperDesign design,JRDesignBand columnHeaderBand, JRDesignBand detailBand,int offsetLeft,int offsetTop)throws JRException {
		int totalWidth = 0;
		 for(int i=0;i<columnModel.size();i++){
			 ColumnModel cm=(ColumnModel)columnModel.get(i);
			 if(cm.isHide()){
				 continue;
			 }
			 int left = offsetLeft+totalWidth;
			 totalWidth+=cm.getBoxWidth();
			 JRDesignStaticText staticText = getJRDesignStaticText(cm.getText(), fontSize, isColumnHeaderFontBond);
			 staticText.setWidth(cm.getBoxWidth());
			 staticText.setHeight(cm.getBoxHeight());
			 staticText.setX(left);
			 staticText.setY(offsetTop);
			 staticText.setHorizontalAlignment(HorizontalAlignEnum.CENTER);
			 setBorder(staticText.getLineBox());
			 columnHeaderBand.addElement(staticText);
			 
			 if(cm.getField()==null){
				 addDetailColumn(cm.getChildren(), design, columnHeaderBand, detailBand,left,offsetTop+cm.getBoxHeight());
				 continue;
			 }

			 JRDesignField field = new JRDesignField();
			 field.setName(cm.getName());
			 field.setValueClass(String.class);
			 design.addField(field);
			 
			 JRDesignTextField textField =getJRDesignTextField(cm.getName(), String.class);
			 textField.setWidth(cm.getBoxWidth());
			 textField.setX(left);
			 String ft=cm.getField().getType();
			 if(cm.getField().getReference()!=null||cm.getField().getTextfield()!=null
					 ||"string".equals(ft)
					 ){
				 textField.setHorizontalAlignment(HorizontalAlignEnum.LEFT);
			 }else if("yearmonth".equals(ft)
					 ||"date".equals(ft)
					 ||"datetime".equals(ft)
					 ||"time".equals(ft)
					 ){
				 textField.setHorizontalAlignment(HorizontalAlignEnum.CENTER);
			 }else{
				 textField.setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
			 }
			 setBorder(textField.getLineBox());
//			 textField.setStretchWithOverflow(true);
			 detailBand.addElement(textField);
//			 detailBand.addElement(rectangleDetail);			 
			
		 }
	}
	static void setBorder(JRLineBox box){
		setBorder(box.getBottomPen());
		setBorder(box.getTopPen());
		setBorder(box.getLeftPen());
		setBorder(box.getRightPen());
	}
	static void setBorder(JRBoxPen pen){
		pen.setLineWidth(1);
	}
	
	private static void getPageFooterTextField(Engine engine,JRDesignBand pageFooter,int totalWidth){
		JRDesignTextField f1 =getJRDesignTextField("", String.class);
		JRDesignTextField f2 =getJRDesignTextField("", String.class);
		JRDesignTextField f3 =getJRDesignTextField("", String.class);
		f1.setEvaluationTime(EvaluationTimeEnum.NOW);	//JREvaluationTime .now
		f2.setEvaluationTime(EvaluationTimeEnum.REPORT);							   //.report
		f3.setEvaluationTime(EvaluationTimeEnum.NOW);
		((JRDesignExpression)f1.getExpression()).setText("\"第 \"+$V{PAGE_NUMBER}+\"\"");
		((JRDesignExpression)f2.getExpression()).setText(" \"/\"+$V{PAGE_NUMBER}+\" 页\"");
		((JRDesignExpression)f3.getExpression()).setText(" \""+engine.getSetting().getCompanyName()+"\"");
		f1.setX(totalWidth-100);f1.setWidth(50);f1.setHorizontalAlignment(HorizontalAlignEnum.RIGHT);
		f2.setX(totalWidth-50);f2.setHorizontalAlignment(HorizontalAlignEnum.LEFT);
		f3.setX(totalWidth/2+80);
		pageFooter.addElement(f1);
		pageFooter.addElement(f2);
		//pageFooter.addElement(f3);
	}
	
	private static void setBand(JasperDesign jd,JRDesignBand title,JRDesignBand columnheader,JRDesignBand detail,JRDesignBand pagefooter){
		jd.setTitle(title);
		jd.setColumnHeader(columnheader);
		((JRDesignSection)jd.getDetailSection()).addBand(detail);
		jd.setPageFooter(pagefooter);
	}
	
	public static JasperDesign getJasperDesign(String name,boolean isLandscape,int totalWidth){
		int tw=totalWidth>0?totalWidth:800;
		JasperDesign design = new JasperDesign();
		design.setName(name);
		if(!isLandscape){
			design.setPageWidth(tw+42);
			design.setPageHeight(842);
			design.setOrientation(OrientationEnum.PORTRAIT);
		}else{
			design.setPageWidth(tw+42);
			design.setPageHeight(595);
			design.setOrientation(OrientationEnum.LANDSCAPE);
		}
		design.setLeftMargin(21);
		design.setRightMargin(21);
		design.setTopMargin(25);
		design.setBottomMargin(30);
		design.setColumnCount(1);
		design.setColumnWidth(tw);
		design.setColumnSpacing(0);	
		design.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
		return design;
	}

	public static JRDesignRectangle getJRDesignRectangle(int width,int height){
		JRDesignRectangle rectangle = new JRDesignRectangle();
		rectangle.setWidth(width);
		rectangle.setHeight(height);		
		return rectangle;
	}
	
	public static JRDesignStaticText getJRDesignStaticText(String text){
		JRDesignStaticText staticText = new JRDesignStaticText();
		staticText.setFontSize(fontSize);
		//staticText.setFontName(fontName);
		staticText.setPdfFontName(pdfFontName);
		staticText.setPdfEncoding(pdfEncoding);
		staticText.setWidth(textWidth);
		staticText.setHeight(textHeight);
		staticText.setText(text);
		staticText.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
		return staticText;
	}
	
	public static JRDesignStaticText getJRDesignStaticText(String text,int fontSize){
		JRDesignStaticText staticText = getJRDesignStaticText(text);
		staticText.setFontSize(fontSize);
		return staticText;
	}
	
	public static JRDesignStaticText getJRDesignStaticText(String text,int fontSize,boolean isBond){
		JRDesignStaticText staticText = getJRDesignStaticText(text,fontSize);
		staticText.setBold(isBond);
		return staticText;
	}
	
	@SuppressWarnings("unchecked")
	public static JRDesignTextField getJRDesignTextField(String name,Class clazz){
		JRDesignTextField textField = new JRDesignTextField();
		JRDesignExpression expression = new JRDesignExpression();
		expression.setText("$F{" + name + "}");
		expression.setValueClass(clazz);
		textField.setExpression(expression);
		textField.setFontSize(fontSize);
		//textField.setFontName(fontName);
		textField.setPdfFontName(pdfFontName);
		textField.setPdfEncoding(pdfEncoding);
		textField.setHeight(textHeight);
		textField.setWidth(textWidth);
		textField.setBlankWhenNull(true);
		textField.setVerticalAlignment(VerticalAlignEnum.MIDDLE);
		return textField;
	}

	public static JRDesignBand getJRDesignBand(int height){
		JRDesignBand band = new JRDesignBand();
		band.setHeight(height);
		return band;
	}

	public static JRBeanCollectionDataSource createJRBeanCollectionDataSource(String[] columnNames,List<HashMap<String, Object>> data) throws IllegalAccessException, InstantiationException {
		List<Object> beans = new ArrayList<Object>();
		int length = columnNames.length;
		DynaProperty[] dynaProps = new DynaProperty[length];
		for (int i = 0; i < length; i++) {
			dynaProps[i] = new DynaProperty(columnNames[i], String.class);
		}
		BasicDynaClass dynaClass = new BasicDynaClass("QueryList",BasicDynaBean.class, dynaProps);
		for (HashMap<String, Object> r : data) {
			DynaBean myBean = dynaClass.newInstance();
			for (Iterator<String> it = r.keySet().iterator();it.hasNext();) {
				String k = it.next();
				if(k.contains("_")){
					continue;
				}
				myBean.set(k, r.get(k).toString());
			}
			beans.add(myBean);
		}
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(beans);
		return dataSource;
	}
	
	public static JRBeanCollectionDataSource createJRBeanCollectionDataSource(List columnModel,List data) throws IllegalAccessException, InstantiationException, JSONException {
		List<Object> beans = new ArrayList<Object>();
		int length = columnModel.size();
		DynaProperty[] dynaProps = new DynaProperty[length];
		for (int i = 0; i < length; i++) {
			dynaProps[i] = new DynaProperty(((ColumnModel)columnModel.get(i)).getName(), String.class);
		}
		BasicDynaClass dynaClass = new BasicDynaClass("TableGrid",BasicDynaBean.class, dynaProps);
		for (int i=0;i<data.size();i++) {
			Map r = (Map)data.get(i);
			DynaBean myBean = dynaClass.newInstance();
			for (int c=0;c<columnModel.size();c++) {
				ColumnModel cm=(ColumnModel)columnModel.get(c);
				String name=cm.getName();
				String text=getFieldValue(cm.getField(),r.get(name));
				myBean.set(name, text);
				int m=10;
				if(text!=null&&text.length()*m>cm.getWdith())
					cm.setWdith(text.length()*m);
			}
			beans.add(myBean);
		}
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(beans);
		return dataSource;
	}
	static public String getFieldValue(Field field,Object v){
		if( v instanceof Object[]){
			Object vv[]=(Object[])v;
			v=vv[vv.length-1];
		}
		String text="";
		if(v!=null){
			IType type=BaseTypes.getTypeByClass(v.getClass());
			if(type==null){
				throw new IllegalStateException("Not support class: "+v.getClass().getName());
			}
			text=type.objectToString(new FieldTypeFacet(Locale.getDefault(),field),v);
			if(type.getName().equals(DateTimeType.NAME)){
				text=text.replace("T", " ");
			}
		}
		return text;
	}

}
