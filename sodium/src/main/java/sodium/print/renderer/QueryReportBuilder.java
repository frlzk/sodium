package sodium.print.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sodium.RequestContext;
import sodium.action.PrintablePage;
import sodium.engine.Engine;
import sodium.print.JasperPrintablePage;
import sodium.print.impl.ColumnModel;
import sodium.print.impl.DynaGridPrintUtil;
import sodium.print.impl.PrintUtil;
import sodium.print.printservice.PrintFormats;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.xmlform.form.Field;
import net.sf.xmlform.form.Form;
import net.sf.xmlform.form.XMLForm;
import net.sf.xmlform.formlayout.component.Block;
import net.sf.xmlform.formlayout.component.Column;
import net.sf.xmlform.formlayout.component.ColumnGroup;
import net.sf.xmlform.formlayout.component.FieldColumn;
import net.sf.xmlform.formlayout.component.FormLayout;
import net.sf.xmlform.formlayout.component.Panel;
import net.sf.xmlform.formlayout.component.Table;

/**
 * @author Liu Zhikun
 */

public class QueryReportBuilder {
	public PrintablePage buildPrintablePage(Engine engine,RequestContext context,XMLForm xmlform,FormLayout layout,String title,String format,int pageSize,List dataObj) {
		if(PrintFormats.EXCEL.getFormat().equals(format)){
			return ExcelReportBuilder.buildPrintablePage(context,xmlform,layout,title,dataObj);
		}
		Table table=getFirstTable(layout);
		List cols=new ArrayList();
		List cms=new ArrayList();
		int level=0;
		if(table!=null){
			XMLForm xform=xmlform;
			level=getTableCols(xform.getRootForm(),table.getColumns(),cols,cms,1);
			resetColumnWidth(cols);
		}
		for(int i=0;i<cols.size();i++){
			ColumnModel cm=(ColumnModel)cols.get(i);
			cm.setBoxHeight(level);
		}
		JasperPrint jp=buildReportC(engine,cols,cms,title,dataObj,level,pageSize);
		if(jp==null)
			return null;
		return new JasperPrintablePage(jp);
	}
	private JasperPrint buildReportC(Engine engine,List tableCols,List cms,String title, List data,int maxLevel,int pageSize){
		try {
			pageSize=(pageSize==0?data.size()*20:pageSize*20);
			JRBeanCollectionDataSource ds=DynaGridPrintUtil.createJRBeanCollectionDataSource(cms, data);
			JasperReport report = DynaGridPrintUtil.getDynamicJasperReport(engine,new ArrayList<JasperDesign>(),title, tableCols,maxLevel,pageSize);
			return PrintUtil.getJasperPrint(report, new HashMap<String, Object>(),ds);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			
		}
	}
	static Table getFirstTable(FormLayout layout){
		return getTable(layout,layout.getRootPanel());
	}
	static Table getTable(FormLayout layout,Panel formPanel){
		Block block=formPanel.getBlock();
		if(block instanceof Table){
			return (Table)block;
		}
		return null;
	}
	static int getTableCols(Form form,List cols,List colMods,List cms,int l){
		int level=l;
		for(int i=0;i<cols.size();i++){
			Column c=(Column)cols.get(i);
			ColumnModel cm=null;
			if(c instanceof FieldColumn){
				FieldColumn fc=(FieldColumn)c;
				Field field=(Field)form.getFields().get(fc.getName());
				cm = new ColumnModel();
				if(fc.getWidth()>-1){
					cm.setWdith(fc.getWidth());
				}
				cm.setName(field.getName());
				cm.setText(field.getLabel());
				cm.setField(field);
				colMods.add(cm);
				cms.add(cm);
			}else if(c instanceof ColumnGroup){
				ColumnGroup cg=(ColumnGroup)c;
				cm = new ColumnModel();
				if(cg.getWidth()>-1){
					cm.setWdith(cg.getWidth());
				}
				cm.setText(cg.getLabel());
				colMods.add(cm);
				if(cg.getColumns().size()>0){
					int l2=getTableCols(form,cg.getColumns(),cm.getChildren(),cms,l+1);
					if(l2>level)
						level=l2;
				}
			}
			String label=cm.getText();
			if(label!=null){
				int m=ColumnModel.LABEL_NUL;
				if(label!=null&&label.length()*m>cm.getWdith())
					cm.setWdith(label.length()*m);
			}
		}
		return level;
	}
	static void resetColumnWidth(List cols){
		for(int i=0;i<cols.size();i++){
			ColumnModel cm=(ColumnModel)cols.get(i);
			int bw=cm.getBoxWidth(),w=cm.getWdith();
			List clist=cm.getChildren();
			if(w>bw){
				if(clist.size()>0){
					ColumnModel c=(ColumnModel)clist.get(clist.size()-1);
					c.setWdith(c.getWdith()+(w-bw));
				}
			}
			resetColumnWidth(clist);
		}
	}
}
