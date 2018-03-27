package sodium.database.sequence;

import java.util.HashMap;
import java.util.Map;

import sodium.database.Sequence;

/**
 * @author Liu Zhikun
 */

public class SequenceHolder {
	static private Sequence defaultSequence;
	static private Map sequences=new HashMap();

	static public Sequence getSequence(String name) {
		Sequence seq=(Sequence)sequences.get(name);
		if(seq==null)
			seq=defaultSequence;
		return seq;
	}

	static public void addSequence(Sequence sequence) {
		if(sequence.getName()==null||"default".equalsIgnoreCase(sequence.getName()))
			defaultSequence=sequence;
		else{
			String names[]=sequence.getName().split(",");
			for(int i=0;i<names.length;i++)
				sequences.put(names[i],sequence);			
		}
	}
	
}
