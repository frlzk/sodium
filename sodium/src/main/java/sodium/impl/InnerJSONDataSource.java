package sodium.impl;

import net.sf.xmlform.data.DataSourceException;
import net.sf.xmlform.data.SourceData;
import net.sf.xmlform.data.SourceParseContext;
import net.sf.xmlform.data.source.JSONDataSource;
import sodium.engine.Sampler;

/**
 * @author Liu Zhikun
 */

public class InnerJSONDataSource extends JSONDataSource{

	public InnerJSONDataSource(String jsonString) {
		super(jsonString);
	}

	public SourceData parse(SourceParseContext parseContext) throws DataSourceException {
		int sid=Sampler.begin("parseJson");
		SourceData sd=super.parse(parseContext);
		Sampler.end(sid);
		return sd;
	}
	
}
