package sodium.database;

/**
 * @author Liu Zhikun
 */

public interface Sequence {
	public String getName();
	public long getNextVal();
}
