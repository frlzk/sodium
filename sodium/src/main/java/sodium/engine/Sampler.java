package sodium.engine;

import sodium.impl.SamplerNote;

/**
 * @author Liu Zhikun
 */

public class Sampler {
	static private Setting setting;
	static private SamplingRecorder samplingRecorder;
	static private ThreadLocal samplerNote=new ThreadLocal();
	static public void reset(){
		samplerNote.set(null);
		if(setting==null||samplingRecorder==null||!samplingRecorder.isAllow())
			return;
		samplerNote.set(new SamplerNote());
	}
	static public int begin(String stage){
		SamplerNote sn=(SamplerNote)samplerNote.get();
		if(sn==null)
			return -1;
		return sn.push(stage);
	};
	static public void end(int samplerId){
		SamplerNote sn=(SamplerNote)samplerNote.get();
		if(sn==null)
			return;
		sn.pop(samplerId);
		samplingRecorder.record(sn.getLabel(), sn.getTime());
	}
	private Sampler(){
		
	}
	static Setting getSetting() {
		return setting;
	}

	static void setSetting(Setting setting) {
		Sampler.setting = setting;
	}
	static SamplingRecorder getSamplingRecorder() {
		return samplingRecorder;
	}
	static void setSamplingRecorder(SamplingRecorder samplingRecorder) {
		Sampler.samplingRecorder = samplingRecorder;
	}
	
}
