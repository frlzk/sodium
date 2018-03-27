package sodium.print;

import java.io.InputStream;

import sodium.action.PrintablePage;

/**
 * @author Liu Zhikun
 */

public class InputStreamPrintablePage implements PrintablePage{
	private String mimeType;
	private String fileName;
	private int length=0;
	private InputStream inputStream;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
