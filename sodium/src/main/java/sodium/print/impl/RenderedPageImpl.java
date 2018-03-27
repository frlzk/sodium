package sodium.print.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import sodium.print.RenderedPage;

public class RenderedPageImpl implements RenderedPage {
	private String name,type;
	private byte[] data;
	public RenderedPageImpl(String name,String type,byte[] bytes){
		this.name=name;
		this.type=type;
		this.data=bytes;
	}
	
	public String getFileName() {
		return name;
	}

	public String getContentType() {
		return type;
	}
	
	public int getContentLength(){
		return data.length;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}

}
