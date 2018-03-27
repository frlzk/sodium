package sodium.print.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRTemplate;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlTemplateLoader;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import sodium.print.RenderedPage;
import sodium.servlet.ConfigListener;
import sodium.servlet.HandlerConfiguration;

public class PrintUtil {

	public final static String CONTEXT = "$ctx$";

	public final static int REPORT_OF_PDF = 0;
	public final static int REPORT_OF_HTML = 1;
	public final static int REPORT_OF_WORD = 2;
	public final static int REPORT_OF_EXCEL = 3;
	public final static int REPORT_OF_XML = 4;
	
	private final static String contentTypeOfHtml = "text/html;charset=UTF-8";
	private final static String contentTypeOfWord = "application/msword;charset=UTF-8";
	private final static String contentTypeOfExcel = "application/vnd.ms-excel;charset=UTF-8";
	private final static String contentTypeOfPdf = "application/pdf;charset=UTF-8";
	
	// get JRTemplate
	public static JRTemplate getJRTemplate(String fileName) throws JRException{
		return JRXmlTemplateLoader.load(fileName);
	}
	
	// get JasperDesign
	public static JasperDesign getJasperDesign(String fileName) throws JRException{
		return JRXmlLoader.load(fileName);
	}
	
	public static JasperDesign getJasperDesign(File file) throws JRException{
		return JRXmlLoader.load(file);
	}
	
	public static JasperDesign getJasperDesign(InputStream is) throws JRException{
		return JRXmlLoader.load(is);
	}
	
	// get JasperReport
	public static JasperReport getJasperReport(String fileName) throws JRException {
		return JasperCompileManager.compileReport(fileName);
	}
	
	public static JasperReport getJasperReport(InputStream is) throws JRException {
		return JasperCompileManager.compileReport(is);
	}
	
	public static JasperReport getJasperReport(JasperDesign jd) throws JRException {
		return JasperCompileManager.compileReport(jd);
	}

	// get JasperPrint
	public static JasperPrint getJasperPrint(String fileName, HashMap<String, Object> parameters) throws JRException{
		return getJasperPrint(getJasperReport(fileName), parameters);
	}
	
	public static JasperPrint getJasperPrint(String fileName, HashMap<String, Object> parameters, Connection con) throws JRException{
		return getJasperPrint(getJasperReport(fileName), parameters,con);
	}
	
	public static JasperPrint getJasperPrint(JasperReport jr, HashMap<String, Object> parameters) throws JRException{
		return JasperFillManager.fillReport(jr, parameters);
	}
	
	public static JasperPrint getJasperPrint(JasperReport jr, HashMap<String, Object> parameters, Connection con) throws JRException{
		return JasperFillManager.fillReport(jr, parameters,con);
	}
	
	public static JasperPrint getJasperPrint(JasperReport jr, HashMap<String, Object> parameters, JRDataSource dataSource) throws JRException{
		return JasperFillManager.fillReport(jr, parameters,dataSource);
	}

	// export to outputstream
	public static void export(String type, JasperPrint jp, OutputStream os)	throws JRException {
		JRExporter exporter = getJRExporter(type);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
		exporter.exportReport();
	}
	
	public static void export(String type, List<JasperPrint> list, OutputStream os) throws JRException {
		JRExporter exporter = getJRExporter(type);
		exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
		exporter.exportReport();
	}
	
	// export to httpservletresponse
	public static void exportToHttpServletResponse(String type, JasperPrint jp,HttpServletRequest req, HttpServletResponse response) throws IOException, JRException{
		JRExporter exporter = getJRExporter(type);
		setImageUri(exporter,req);
		setImageToSession(req,jp);
		response.setContentType(getContentType(exporter));
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
		exporter.exportReport();
	}
	
	public static void exportToHttpServletResponse(String type, List<JasperPrint> list,HttpServletRequest req, HttpServletResponse response) throws IOException, JRException{
		JRExporter exporter = getJRExporter(type);
		setImageUri(exporter,req);
		setImageToSession(req,list);
		response.setContentType(getContentType(exporter));
		exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
		exporter.exportReport();
	}
	
	public static RenderedPage renderPrint(String title,String type, List<JasperPrint> list,HttpServletRequest req) throws IOException, JRException{
		JRExporter exporter = getJRExporter(type);
		setImageUri(exporter,req);
		setImageToSession(req,list);
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
		exporter.exportReport();
		return new RenderedPageImpl(getFileName(title,type),getContentType(exporter),bos.toByteArray());
	}
	
	static private void setImageUri(JRExporter exp,HttpServletRequest req){
		exp.setParameter(JRHtmlExporterParameter.IMAGES_URI, req.getServletContext().getAttribute(HandlerConfiguration.BASE_SERVLET_PATH)+"/jasperreportsimage?image=");
	}
	static private void setImageToSession(HttpServletRequest req,List<JasperPrint> list){
		if(list.size()>0)
			setImageToSession(req,list.get(0));
		req.getSession().setAttribute(
                ImageServlet.DEFAULT_JASPER_PRINT_LIST_SESSION_ATTRIBUTE, 
                list);
	}
	static private void setImageToSession(HttpServletRequest req,JasperPrint jp){
		req.getSession().setAttribute(
                ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, 
                jp); 
	}
	// export type
	private static JRExporter getJRExporter(String type) {
		JRExporter exporter = null;
		if("html".equals(type)||"preview".equals(type)){
			exporter = new JRHtmlExporter();
//			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "img.jasperreportsImage?image=");
			exporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML,"<div style='page-break-before:always;'></div>");
		}else if("doc".equals(type)){
			exporter = new JRRtfExporter();
		}else if("xls".equals(type)){
			exporter = new JExcelApiExporter();
		}else if("xml".equals(type)){
			exporter = new JRXmlExporter();
		}else{
			exporter = new JRPdfExporter();
		}
		return exporter;
	}
	
	private static String  getFileName(String title,String type) {
		if("html".equals(type)||"preview".equals(type)){
			return null;
		}else if("doc".equals(type)){
			return title+".doc";
		}else if("xls".equals(type)){
			return title+".xls";
		}else if("xml".equals(type)){
			return title+".xml";
		}else if("pdf".equals(type)){
			return title+".pdf";
		}
		return title;
	}

	// response contenttype
	private static String getContentType(JRExporter exporter) {
		String responseContentType = contentTypeOfHtml;
		if (exporter instanceof JRRtfExporter) {
			responseContentType = contentTypeOfWord;
		} else if (exporter instanceof JRXlsExporter || exporter instanceof JExcelApiExporter) {
			responseContentType = contentTypeOfExcel;
		} else if (exporter instanceof JRPdfExporter) {
			responseContentType = contentTypeOfPdf;
		}
		return responseContentType;
	}
}
