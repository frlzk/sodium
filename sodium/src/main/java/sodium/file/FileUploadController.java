package sodium.file;

import sodium.RequestContext;
import sodium.engine.Permission;

/**
 * @author Liu Zhikun
 */

public interface FileUploadController {
	public long getMaxFileSize(RequestContext ctx);
	public Permission checkUpload(RequestContext ctx,File file);
}
