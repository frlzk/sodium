package sodium.job.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import net.sf.xmlform.util.ClassResource;
import net.sf.xmlform.util.ClassResourceVisitor;
import sodium.cluster.Cluster;
import sodium.engine.Configuration;
import sodium.job.Job;
import sodium.job.JobScheduler;

/**
 * @author Liu Zhikun
 */

public class JobSchedulerImpl implements JobScheduler,ApplicationContextAware {
	private Map jobs=new ConcurrentHashMap();
	private Map cronJobs=new ConcurrentHashMap();
	private Map delayJobs=new ConcurrentHashMap();
	private TaskExecutor taskExecutor;
	private TaskScheduler taskScheduler;
	private ApplicationContext applicationContext;
	private Configuration configuration;
	private static Logger logger=LoggerFactory.getLogger(JobSchedulerImpl.class);
	private Cluster cluster;
	private String mode="enable";//force,disable
	
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public void setApplicationContext(ApplicationContext ac) throws BeansException {
		applicationContext=ac;
	}
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	public void init(){
		loadJobs();
		submitJobs();
	}
	public boolean isAllowRun(){
		if("enable".equals(mode)){
			return cluster==null||cluster.isMaster();
		}else if("force".equals(mode)){
			return true;
		}else{
			return false;
		}
	}
	private void loadJobs() {
		ClassResourceVisitor v=new ClassResourceVisitor(){
			public void visit(ClassResource file) {
				doRefreshAction(file);
			}
		};
		try {
			configuration.findFileByExt(configuration.getPackagesToScan(),0,".job.xml",v);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	private void doRefreshAction(ClassResource file) {
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(file.getInputStream());
			Element root=document.getRootElement();
			Iterator jit=root.elementIterator("job");
			while(jit.hasNext()){
				Element me=(Element)jit.next();
				String cla=me.attributeValue("class");
				String name=me.attributeValue("name", cla);
				String cron=me.attributeValue("cron");
				String delay=me.attributeValue("delay","0");
				boolean brun=Boolean.valueOf(me.attributeValue("bootrun","true"));
				Job j=(Job)configuration.getBeanCreator().createBean(cla);
				jobs.put(name, new JobWrap(name,cron,Long.parseLong(delay),brun,j));
			}
		}catch(Exception e){
			throw new IllegalStateException(e);
		}
	}
	private void submitJobs(){
		Iterator it=jobs.keySet().iterator();
		while(it.hasNext()){
			String cla=(String)it.next();
			JobWrap ji=(JobWrap)jobs.get(cla);
			if(ji.getCron()==null&&ji.getDelay()==0){
				if((cluster==null||cluster.isMaster())&&isAllowRun())
					execute(ji.getName(),null,false);
			}else{
				submit(ji.getCron(),ji.getDelay(),ji.getName(),null);
				if(ji.isBootrun()){
					if((cluster==null||cluster.isMaster())&&isAllowRun())
						execute(ji.getName(),null,false);
				}
			}
		}
	}
	public void submit(String cron,long delay,String name,Object param){
		try{
			JobWrap job=(JobWrap)jobs.get(name);
			if(job==null){
				throw new IllegalArgumentException("Not found job: "+name);
			}
			doSubmit(cron,delay,job,param);
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
	public void doSubmit(final String cron,final long delay,final JobWrap job,Object param)throws Exception{
		if(cron==null){
			List jobList=(List)delayJobs.get(delay);
			if(jobList==null){
				Runnable target=new Runnable(){
					public void run() {
						executeDelayJob(delay);
					}
				};
				taskScheduler.scheduleWithFixedDelay(target, delay);
				jobList=new ArrayList();
				delayJobs.put(delay, jobList);
			}
			jobList.add(new Object[]{job,param});
		}else{
			List jobList=(List)cronJobs.get(cron);
			if(jobList==null){
				Runnable target=new Runnable(){
					public void run() {
						executeCronJob(cron);
					}
				};
				taskScheduler.schedule(target, new CronTrigger(cron));
				jobList=new ArrayList();
				cronJobs.put(cron, jobList);
			}
			jobList.add(new Object[]{job,param});
		}
	}
	private void executeCronJob(String cron){
		if(!isAllowRun())
			return;
		logger.info("Start execute job for cron: "+cron);
		List jobList=(List)cronJobs.get(cron);
		if(jobList==null){
			logger.error("Start execute job for cron: "+cron+",BUT NULL");
			return ;
		}
		Iterator it=jobList.iterator();
		while(it.hasNext()){
			Object[] job=(Object[])it.next();
			JobWrap j=(JobWrap)job[0];
			try{
				j.run(job[1]);
			}catch(Exception e){
				logger.info("Execute job : "+j.getName()+e.getMessage(),e);
			}
		}
		
	}
	private void executeDelayJob(long delay){
		if(!isAllowRun())
			return;
		logger.info("Start execute job for delay: "+delay);
		List jobList=(List)delayJobs.get(delay);
		if(jobList==null){
			logger.error("Start execute job for delay: "+delay+",BUT NULL");
			return ;
		}
		Iterator it=jobList.iterator();
		while(it.hasNext()){
			Object[] job=(Object[])it.next();
			JobWrap j=(JobWrap)job[0];
			try{
				j.run(job[1]);
			}catch(Exception e){
				logger.info("Execute job : "+j.getName()+e.getMessage(),e);
			}
		}
		
	}
	public void execute(String name,Object param) {
		execute(name,param,true);
	}
	public void execute(String name,Object param,boolean delay) {
		JobWrap job=(JobWrap)jobs.get(name);
		if(job==null){
			throw new IllegalArgumentException("Not found job: "+name);
		}
		doExecute(job,param,delay);
	}
	public void doExecute(final JobWrap job,final Object param,final boolean delay) {
		taskExecutor.execute(new Runnable(){
			public void run() {
				job.run(param);
			}
		});
	}
}
