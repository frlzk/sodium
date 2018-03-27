package sodium.file;

import java.util.Map;


/**
 * @author Liu Zhikun
 */

public interface FileManager {
	public void save(File file);
	public File load(String fid);
	public void remove(String fid);
	public void claim(String fid,Map infos);
}
