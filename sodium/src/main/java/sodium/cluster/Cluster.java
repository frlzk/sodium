package sodium.cluster;

public interface Cluster {
	public boolean isMaster();
	public void publishEvent(ClusterEvent evt);
}
