package sodium.engine;

import org.hibernate.cfg.Environment;

/**
 * @author Liu Zhikun
 */

public class Setting {
	private boolean debug=false;
	private int samplingBase=100;
	private double samplingRate=1.00/100.00;
	private String companyName="",systemName="",systemVersion="",buildVersion="";
	public boolean isDebug(){
		return debug;
	}
	public void setDebug(boolean d){
		debug=d;
	}
	public int getSamplingBase() {
		return samplingBase;
	}
	public void setSamplingBase(int samplingBase) {
		this.samplingBase = samplingBase;
		samplingRate=1.00/samplingBase;
	}
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getSystemVersion() {
		return systemVersion;
	}

	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}
}
