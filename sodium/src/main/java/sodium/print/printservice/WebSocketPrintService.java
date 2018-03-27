package sodium.print.printservice;


import java.io.ByteArrayOutputStream;
import java.util.Locale;

import javax.jms.JMSException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.print.PrintContext;
import sodium.print.PrintFormat;
import sodium.print.PrintService;
import sodium.print.RenderedPage;

abstract public class WebSocketPrintService implements PrintService {
	public static final String DEFAULT_NAME="wsprint";
	private String name=DEFAULT_NAME;
	private I18NTexts label=new I18NTexts();
	private PrintFormat[] formats=new PrintFormat[]{PrintFormats.PDF};
	public WebSocketPrintService(){
		label.put(new I18NText(Locale.ENGLISH,"默认打印机"));
		label.put(new I18NText(Locale.CHINESE,"Default Printer"));
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public I18NTexts getLabel() {
		return label;
	}
	public void setLabel(I18NTexts label) {
		this.label = label;
	}
	public boolean isDownload() {
		return false;
	}
	public PrintFormat[] getFormats(){
		return formats;
	}
	public void print(final PrintContext context,final RenderedPage page)throws Exception {
		JSONObject obj=new JSONObject();
		try{
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			IOUtils.copy(page.getInputStream(), bos);
			obj.put("printer",context.getPrinter());
			obj.put("fileName",page.getFileName());
			obj.put("contentLength",page.getContentLength());
			obj.put("contentType",page.getContentType());
			obj.put("content",Base64.encodeBase64String(bos.toByteArray()));
		}catch(Exception e){
			throw new JMSException(e.getMessage());
		}
		sendPrintData(context,obj.toString());
	}
	abstract protected void sendPrintData(PrintContext context,String data);
}
