package misc;

public class TunerConf extends XmlLoader {
	final static private long serialVersionUID = 0;
	final static public String CONFIG_FNAME = "config.xml";
	
	static private TunerConf m_instance = new TunerConf();
	
	private TunerConf() {
		super(CONFIG_FNAME);
	}

	static public TunerConf getInstance() {
		return m_instance;
	}
}
