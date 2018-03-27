package sodium.print;

import java.io.InputStream;

public interface RenderedPage {
	public String getFileName();
	public String getContentType();
	public int getContentLength();
	public InputStream getInputStream();
}
