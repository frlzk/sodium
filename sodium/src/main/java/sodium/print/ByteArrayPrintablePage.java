package sodium.print;

import sodium.action.PrintablePage;

/**
 * @author Liu Zhikun
 */

public class ByteArrayPrintablePage implements PrintablePage {
	private String mimeType;
	private String fileName;
	private byte[] byteArray;

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
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
