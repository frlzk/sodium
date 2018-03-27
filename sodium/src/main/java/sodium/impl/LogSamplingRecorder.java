package sodium.impl;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sodium.engine.SamplingRecorder;
import sodium.engine.Setting;

/**
 * @author Liu Zhikun
 */

public class LogSamplingRecorder implements SamplingRecorder {
	private static Logger logger = LoggerFactory.getLogger(LogSamplingRecorder.class);
	private int samplingBase=100;
	private double samplingRate=1.00/samplingBase;
	private Random random=new Random();
	private Setting setting=null;
	
	public Setting getSetting() {
		return setting;
	}
	public void setSetting(Setting setting) {
		this.setting = setting;
	}
	public int getSamplingBase() {
		return samplingBase;
	}
	public void setSamplingBase(int samplingBase) {
		this.samplingBase = samplingBase;
		samplingRate=1.00/samplingBase;
	}
	public boolean isAllow(){
//		if(samplingBase==1&&setting!=null&&setting.isDebug())
//			return true;
		return random.nextDouble()<=samplingRate;
	}
	public void record(String stage, long mi) {
		logger.info(stage+" "+mi);
	}

}
