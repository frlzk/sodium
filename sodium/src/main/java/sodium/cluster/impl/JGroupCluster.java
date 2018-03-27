package sodium.cluster.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import net.sf.xmlform.type.BaseTypes;
import net.sf.xmlform.type.BooleanType;
import net.sf.xmlform.type.IType;
import net.sf.xmlform.type.IntType;
import net.sf.xmlform.type.LongType;
import net.sf.xmlform.type.ShortType;
import sodium.cluster.Cluster;
import sodium.cluster.ClusterEvent;
import sodium.cluster.ClusterEventListener;

public class JGroupCluster implements Cluster {
	private String name="jgroupCluster";
	private JChannel channel;
	@Autowired(required=false)
	private List<ClusterEventListener> listeners;
	private TaskExecutor taskExecutor;
	
	private class R implements Receiver{
		public void getState(OutputStream arg0) throws Exception {
			
		}
		public void receive(Message msg) {
			onEvent(msg);
		}
		public void setState(InputStream arg0) throws Exception {
			
		}
		public void block() {
			
		}
		public void suspect(Address arg0) {
			
		}
		public void unblock() {
			
		}
		public void viewAccepted(View arg0) {
			
		}
		
	};
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	public void init(){
		try {
			channel = new JChannel();
			channel.setReceiver(new R());
			channel.connect(name);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	public boolean isMaster() {
		if(channel==null){
			//throw new IllegalStateException("Not init");
			return false;
		}
		//System.out.println("****************8 "+channel.getAddress()+",         "+channel.getView());
		return channel.getAddress().equals(channel.getView().getMembers().get(0));
	}
	public void publishEvent(final ClusterEvent evt){
		if(taskExecutor==null)
			doPublishEvent(evt);
		else{
		 taskExecutor.execute(new Runnable(){
			 	public void run() {
			 		doPublishEvent(evt);
			}});
		}
	}
	private void doPublishEvent(ClusterEvent evt){
		try{
			JSONObject obj=new JSONObject();
			obj.put("type", evt.getType()==null?"cluster":evt.getType());
			JSONArray items=new JSONArray();
			obj.put("items", items);
			String names[]=evt.getNames();
			for(int i=0;i<names.length;i++){
				Object v=evt.getObject(names[i]);
				if(v==null){
					continue;
				}
				IType type=BaseTypes.getTypeByClass(v.getClass());
				JSONObject item=new JSONObject();
				item.put("n", names[i]);
				item.put("t", type.getName());
				item.put("v", type.objectToString(null, v));
				items.put(item);
			}
			final Message msg = new Message(null, obj.toString());
			channel.send(msg);
		}catch(Exception e){
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	private void onEvent(Message msg){
		try{
			String json=(String)msg.getObject();
			JSONObject obj=new JSONObject(json);
			ClusterEvent evt=new ClusterEvent();
			evt.setType(obj.getString("type"));
			JSONArray items=obj.getJSONArray("items");
			for(int i=0;i<items.length();i++){
				JSONObject item=items.getJSONObject(i);
				IType type=BaseTypes.getTypeByName(item.getString("t"));
				if(type.getName().equals(IntType.NAME)){
					evt.setInt(item.getString("n"), (Integer)type.stringToObject(item.getString("v")));
				}else if(type.getName().equals(LongType.NAME)){
					evt.setLong(item.getString("n"), (Long)type.stringToObject(item.getString("v")));
				}else if(type.getName().equals(ShortType.NAME)){
					evt.setShort(item.getString("n"), (Short)type.stringToObject(item.getString("v")));
				}else if(type.getName().equals(BooleanType.NAME)){
					evt.setBoolean(item.getString("n"), (Boolean)type.stringToObject(item.getString("v")));
				}else{
					evt.setString(item.getString("n"), item.getString("v"));
				}
			}
			if(listeners!=null){
				for(int i=0;i<listeners.size();i++){
					listeners.get(i).onClusterEvent(evt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
