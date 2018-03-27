package sodium.action.impl;

import net.sf.xmlform.XMLFormPort;
import net.sf.xmlform.formlayout.XMLFormLayoutPort;
import sodium.engine.Engine;


/**
 * @author Liu Zhikun
 */

public class EngineContext {
	private Engine engine;
	private XMLFormPort xmlformPort;
	private XMLFormLayoutPort xmlformLayoutPort;
	public EngineContext(Engine engine, XMLFormPort xmlformPort,XMLFormLayoutPort xmlformLayoutPort) {
		super();
		this.engine = engine;
		this.xmlformPort = xmlformPort;
		this.xmlformLayoutPort=xmlformLayoutPort;
	}
	public Engine getEngine() {
		return engine;
	}
	public XMLFormPort getXmlformPort() {
		return xmlformPort;
	}
	public XMLFormLayoutPort getXmlformLayoutPort() {
		return xmlformLayoutPort;
	}
}
