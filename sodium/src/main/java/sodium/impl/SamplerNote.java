package sodium.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author Liu Zhikun
 */

public class SamplerNote {
	private static AtomicLong seq=new AtomicLong(0);
	private List stages=new ArrayList(10);
	private List times=new ArrayList(10);
	private List lengths=new ArrayList(10);
	private StringBuilder lables=new StringBuilder(System.currentTimeMillis()+"-"+seq.getAndIncrement());
	private long last=0;
	private String label="";
	public int push(String stage){
		stages.add(stage);
		times.add(System.currentTimeMillis());
		lengths.add(lables.length());
		if(lengths.size()==0){
			lables.append(stage);
		}else{
			lables.append("/").append(stage);
		}
		return stages.size()-1;
	}
	public void pop(int id){
		if(id<0||id>stages.size()-1)
			return;
		while(true){
			if((stages.size()-1)==id)
				break;
			int idx=stages.size()-1;
			stages.remove(idx);
			times.remove(idx);
			lengths.remove(idx);
		}
		int idx=stages.size()-1;
		long time=(Long)times.get(idx);
		last=System.currentTimeMillis()-time;
		int sbLength=(Integer)lengths.get(idx);
		label=lables.toString();
		lables.setLength(sbLength);
		stages.remove(idx);
		times.remove(idx);
		lengths.remove(idx);
	}
	public long getTime(){
		return last;
	}
	public String getLabel(){
		return label;
	}
}
