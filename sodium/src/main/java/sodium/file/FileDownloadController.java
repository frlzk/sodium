package sodium.file;

import sodium.RequestContext;
import sodium.engine.Permission;

/**
 * @author Liu Zhikun
 */

public interface FileDownloadController {
	public Permission checkDownload(RequestContext ctx,String fid);
}
