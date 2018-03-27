package sodium.job.impl;

import sodium.job.Job;

public class JobWrap {
	private String cron,name;
	private Job job;
	private long delay;
	private boolean bootrun;
	public JobWrap(String name,String cron,long de,boolean bootrun,Job job){
		this.name=name;
		this.cron=cron;
		this.job=job;
		this.bootrun=bootrun;
		this.delay=de;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCron(){
		return cron;
	}
	public long getDelay(){
		return delay;
	}
	public Job getJob(){
		return job;
	}
	public boolean isBootrun() {
		return bootrun;
	}
	public void setBootrun(boolean bootrun) {
		this.bootrun = bootrun;
	}
	public void run(Object param) {
		job.run(param);
	}
}
