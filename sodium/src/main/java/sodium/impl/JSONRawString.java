package sodium.impl;

import org.json.JSONObject;

/**
 * @author Liu Zhikun
 */

public class JSONRawString extends JSONObject{
	private String str;
	public JSONRawString(String str){
		this.str=str;
	}
	public String toString() {
		return str;
	}

}
