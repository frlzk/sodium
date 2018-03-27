package sodium.print.renderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.validator.routines.BigDecimalValidator;

import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Font;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.component.Table;
import sodium.RequestContext;
import sodium.print.ByteArrayPrintablePage;
import sodium.print.impl.ColumnModel;
import sodium.print.impl.DynaGridPrintUtil;

public class ExcelReportBuilder {
	static BigDecimalValidator slv=new BigDecimalValidator();
	static public ByteArrayPrintablePage buildPrintablePage(RequestContext context,XMLForm xmlform,FormLayout layout,String title,List dataObj) {
		Table table=QueryReportBuilder.getFirstTable(layout);
		List cols=new ArrayList();
		List cms=new ArrayList();
		int level=0;
		if(table!=null){
			XMLForm xform=xmlform;
			level=QueryReportBuilder.getTableCols(xform.getRootForm(),table.getColumns(),cols,cms,1);
			QueryReportBuilder.resetColumnWidth(cols);
		}
		for(int i=0;i<cols.size();i++){
			ColumnModel cm=(ColumnModel)cols.get(i);
			cm.setBoxHeight(level);
		}
		byte byteArray[]=null;
		try {
			byteArray = buildReportExcel(cols,cms,title,dataObj,level);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		ByteArrayPrintablePage bap=new ByteArrayPrintablePage();
		bap.setFileName(title+".xls");
		bap.setMimeType("application/vnd.ms-excel");
		bap.setByteArray(byteArray);
		return bap;
	}
	static private byte[] buildReportExcel(List tableCols,List names,String title, List data,int maxLevel)throws Exception{
		int baseFontSize=8;
		int maxCols=0,titleRow=0;
		for(int i=0;i<tableCols.size();i++){
			ColumnModel tec=(ColumnModel)tableCols.get(i);
			maxCols+=tec.getColSpan();
//			if(titleRow<tec.getMaxRows())
//				titleRow=tec.getMaxRows();
		}
		ByteArrayOutputStream bas=new ByteArrayOutputStream();
		WritableWorkbook wb=Workbook.createWorkbook(bas);
		WritableSheet sheet=wb.createSheet(title, 0);
		
		WritableFont titleWf=new WritableFont(Font.ARIAL,baseFontSize+4);//,WritableFont.BOLD
		WritableCellFormat titleWcf=new WritableCellFormat(titleWf);
		titleWcf.setAlignment(Alignment.CENTRE);
		titleWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		WritableCell titleCell=new Label(0,0,title,titleWcf);
		sheet.addCell(titleCell);
		sheet.mergeCells(0, 0, maxCols-1, 0);
//		addExcelHeader(sheet,maxCols,(String[][])extra.get(ExportUtil.EXTRA_PAGEHEADER));
		
		List fieldList=new ArrayList();
		addExcelColumn(tableCols,sheet,0,sheet.getRows(),fieldList,baseFontSize);
		
		WritableFont dataWf=new WritableFont(Font.ARIAL,baseFontSize);
		WritableCellFormat centerWcf=new WritableCellFormat(dataWf);
		centerWcf.setAlignment(Alignment.CENTRE);
		centerWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		centerWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		centerWcf.setWrap(true);
		
		WritableCellFormat leftWcf=new WritableCellFormat(dataWf);
		leftWcf.setAlignment(Alignment.LEFT);
		leftWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		leftWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		leftWcf.setWrap(true);
		
		WritableCellFormat rightWcf=new WritableCellFormat(dataWf);
		rightWcf.setAlignment(Alignment.RIGHT);
		rightWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		rightWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		rightWcf.setWrap(true);
		
		int begin=sheet.getRows();
		SheetSettings setting=sheet.getSettings();
		setting.setPrintTitlesRow(0, begin-1);
//		String orien=(String)extra.get(ExportUtil.EXTRA_PRINTORIEN);
//		if(ExportUtil.OPIEN_LAND.equals(orien)){
//			setting.setOrientation(PageOrientation.LANDSCAPE);
//		}else if(ExportUtil.ORIEN_PORT.equals(orien)){
//			setting.setOrientation(PageOrientation.PORTRAIT);
//		}
		
		int dataRows=data.size();
		Field[] fields=(Field[])fieldList.toArray(new Field[fieldList.size()]);
		int maxWidths[]=new int[fields.length];
		int minWidths[]=new int[fields.length];
		Map maxMap=new HashMap();
		Map minMap=new HashMap();
//		if(extra.containsKey(ExportUtil.EXTRA_MAXWIDTHS)){
//			maxMap=(Map)extra.get(ExportUtil.EXTRA_MAXWIDTHS);
//		}
//		if(extra.containsKey(ExportUtil.EXTRA_MINWIDTHS)){
//			minMap=(Map)extra.get(ExportUtil.EXTRA_MINWIDTHS);
//		}
		for(int i=0;i<fields.length;i++){
			String fn=fields[i].getName();
			maxWidths[i]=1000000;
			minWidths[i]=0;
			if(maxMap.containsKey(fn)){
				maxWidths[i]=Integer.parseInt(maxMap.get(fn).toString());
			}
			if(minMap.containsKey(fn)){
				minWidths[i]=Integer.parseInt(minMap.get(fn).toString());
			}
		}
		for(int r=0;r<dataRows;r++){
			Map map=(Map)data.get(r);
			for(int f=0;f<fields.length;f++){
				WritableCellFormat ccf=rightWcf;
				String ft=fields[f].getType();
				 if(fields[f].getReference()!=null||fields[f].getTextfield()!=null
						 ||"string".equals(ft)
						 ){
					ccf=leftWcf;
				 }else if("yearmonth".equals(ft)
						 ||"date".equals(ft)
						 ||"datetime".equals(ft)
						 ||"time".equals(ft)
						 ){
					ccf=centerWcf;
				 }
				String v=DynaGridPrintUtil.getFieldValue(fields[f],map.get(fields[f].getName()));
				WritableCell dataCell=null;
				if(v!=null&&slv.isValid(v.replace('E', 'W'),"###.##")&&("decimal".equals(ft)
						 ||"double".equals(ft)
						 ||"float".equals(ft)
						 ||"integer".equals(ft)
						 ||"int".equals(ft)
						 ||"long".equals(ft)
						 ||"short".equals(ft))
						&&(fields[f].getReference()==null&&fields[f].getTextfield()==null)
						){
//					try{
						dataCell=new jxl.write.Number(f,begin+r,new BigDecimal(v).doubleValue());
//					}catch(Exception e){
//						dataCell=new Label(f,begin+r,v,ccf);
//					}
				}else{
					dataCell=new Label(f,begin+r,v,ccf);
				}
				sheet.addCell(dataCell);
				int width=0;
				if("string".equals(ft)){
					width=v.length()*2;
				}else{
					width=v.length()*1;
				}
				if(width>maxWidths[f]){
					width=maxWidths[f];
				}
				if(width<minWidths[f]){
					width=minWidths[f];
				}
				if(sheet.getColumnWidth(f)<width){
					sheet.setColumnView(f, width);
				}
			}
		}
		
		//addExcelHeader(sheet,fieldList.size(),(String[][])extra.get(ExportUtil.EXTRA_SUMMARY));
		
		//addExcelFooter(sheet,extra);
//		sheet.getSettings().getFooter().getLeft().append("\n\r");
//		Contents footCenter = sheet.getSettings().getFooter().getCentre();
//		footCenter.append("\n\r第");
//		footCenter.appendPageNumber();
//		footCenter.append("页 共");
//		footCenter.appendTotalPages();;
//		footCenter.append("页");
//		sheet.getSettings().getFooter().getRight().append("\n\r");
		
		wb.write();
		wb.close();
		return bas.toByteArray();
	}
	private static void addExcelColumn(List columnModel, WritableSheet sheet,int offsetLeft,int offsetTop,List fields,int fontSize)throws Exception {
		WritableFont colWf=new WritableFont(Font.ARIAL,fontSize+2);//,WritableFont.BOLD
		WritableCellFormat colWcf=new WritableCellFormat(colWf);
		colWcf.setAlignment(Alignment.CENTRE);
		colWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		colWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		colWcf.setWrap(true);
		
		for(int i=0,c=offsetLeft;i<columnModel.size();i++){
			ColumnModel cm=(ColumnModel)columnModel.get(i);
			if(cm.isHide()){
				continue;
			}
			int left = c;
			WritableCell colCell=new Label(left,offsetTop,cm.getText(),colWcf);
			sheet.addCell(colCell);
			if(cm.getColSpan()>1||cm.getRowSpan()>1){
				sheet.mergeCells(left,offsetTop, left+cm.getColSpan()-1, offsetTop+cm.getRowSpan()-1);
			}
	
			if(cm.getChildren().size()>0){
				addExcelColumn(cm.getChildren(), sheet,left,offsetTop+cm.getRowSpan(),fields,fontSize);
			}
			if(cm.getField()!=null){
				sheet.setColumnView(left,5);//cm.getText().length()*2+3);
				fields.add(cm.getField());
			}
			c+=cm.getColSpan();
		}
	}
	static private void addExcelFooter(WritableSheet sheet,Map extra){
		if(1==1)
			return ;
		String f[][]=null;//(String[][])extra.get(ExportUtil.EXTRA_PAGEFOOTER);
		if(f==null)
			return;
		StringBuilder leftsb=new StringBuilder();
		StringBuilder centersb=new StringBuilder();
		StringBuilder rightsb=new StringBuilder();
		for(int i=0;i<f.length;i++){
			String row[]=f[i];
			if(row.length>=1){
				if(i>0)
					leftsb.append("\n\r");
				leftsb.append(row[0]);
			}
			if(row.length>=2){
				if(i>0)
					centersb.append("\n\r");
				centersb.append(row[1]);
			}
			if(row.length>=3){
				if(i>0)
					rightsb.append("\n\r");
				rightsb.append(row[2]);
			}
		}
		sheet.getSettings().getFooter().getLeft().append(leftsb.toString());
		sheet.getSettings().getFooter().getCentre().append(centersb.toString());
		sheet.getSettings().getFooter().getRight().append(rightsb.toString());
	}
	static private void addExcelHeader(WritableSheet sheet,int cols,String f[][])throws Exception{
		if(f==null)
			return;
		WritableFont dataWf=new WritableFont(Font.ARIAL,12);
		WritableCellFormat centerWcf=new WritableCellFormat(dataWf);
		centerWcf.setAlignment(Alignment.CENTRE);
		centerWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		//centerWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		
		WritableCellFormat leftWcf=new WritableCellFormat(dataWf);
		leftWcf.setAlignment(Alignment.LEFT);
		leftWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		//leftWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		
		WritableCellFormat rightWcf=new WritableCellFormat(dataWf);
		rightWcf.setAlignment(Alignment.RIGHT);
		rightWcf.setVerticalAlignment(VerticalAlignment.CENTRE);
		//rightWcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		WritableCellFormat wcf[]=new WritableCellFormat[]{leftWcf,centerWcf,rightWcf};
		int begin=sheet.getRows();
		int span=cols/3;
		for(int i=0;i<f.length;i++){
			String[] row=f[i];
			for(int r=0,idx=0;r<row.length&&r<3;r++){
				WritableCell dataCell=new Label(idx,begin,row[r],wcf[r]);
				sheet.addCell(dataCell);
				if(span>1){
					sheet.mergeCells(idx, begin, idx+span-1, begin);
				}
				idx+=span;
			}
		}
	}
}
