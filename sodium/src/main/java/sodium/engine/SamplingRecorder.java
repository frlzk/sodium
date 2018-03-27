package sodium.engine;

/**
 * @author Liu Zhikun
 */

public interface SamplingRecorder {
	public boolean isAllow();
	public void record(String stage,long mi);
}
