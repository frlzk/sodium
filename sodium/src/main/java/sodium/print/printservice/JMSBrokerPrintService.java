package sodium.print.printservice;


import java.io.ByteArrayOutputStream;
import java.util.Locale;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import net.sf.xmlform.util.I18NText;
import net.sf.xmlform.util.I18NTexts;
import sodium.print.PrintContext;
import sodium.print.PrintFormat;
import sodium.print.PrintService;
import sodium.print.RenderedPage;

abstract public class JMSBrokerPrintService implements PrintService {
	public static final String DEFAULT_NAME="jsmprint";
	private String name=DEFAULT_NAME;
	private I18NTexts label=new I18NTexts();
	private PrintFormat[] formats=new PrintFormat[]{PrintFormats.PDF};
	private JmsTemplate jmsTemplate;
	public JMSBrokerPrintService(){
		label.put(new I18NText(Locale.ENGLISH,"默认打印机"));
		label.put(new I18NText(Locale.CHINESE,"Default Printer"));
	}
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
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
//		context.getServletResponse().setContentType(page.getContentType());
//		context.getServletResponse().setContentLength(page.getContentLength());
//		IOUtils.copy(page.getInputStream(), context.getServletResponse().getOutputStream());
//		context.getServletResponse().getOutputStream().flush();
		final String queue=getQueueName(context);
		if(queue==null)
			return ;
		SessionCallback action=new SessionCallback(){
			public Object doInJms(Session session) throws JMSException {
				sendData(context,page,session,queue);
				return null;
			}
		};
		jmsTemplate.execute(action);
	}
	private void sendData(final PrintContext context, RenderedPage page,Session session,String queue)throws JMSException{
		Queue destination = session.createQueue(queue);
		MessageProducer producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
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
		TextMessage message = session.createTextMessage(obj.toString());
		producer.send(message);
	}
	abstract protected String getQueueName(PrintContext context);
}
