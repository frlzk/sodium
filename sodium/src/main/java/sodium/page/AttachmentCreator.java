package sodium.page;

import java.util.Map;

public interface AttachmentCreator {
	public Map<String,String> createAttachments(CreateAttachmentContext ctx);
}
