package sodium.job;


/**
 * @author Liu Zhikun
 */

public interface JobScheduler {
//	public void submit(String cron,String jobname,Object param);
	public void execute(String jobname,Object param);
}
