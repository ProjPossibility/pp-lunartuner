package misc;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class TunerConf extends HashMap<String, Object> {
	static final private long serialVersionUID = 0;
	
	static private String m_filename = null;
	
	static private enum Types {
		String,
		Integer,
		Long,
		Boolean
	}
	
	static private TunerConf m_instance = new TunerConf();
	
	private TunerConf() {
	}

	static public TunerConf getInstance() {
		return m_instance;
	}

	public int getInt(String key) {
		return ((Integer)get(key)).intValue();
	}
	
	public long getLong(String key) {
		return ((Long)get(key)).longValue();
	}
	
	public String getString(String key) {
		return (String)get(key);
	}
	
	public boolean getBoolean(String key) {
		return ((Boolean)get(key)).booleanValue();
	}
	
	public void setInt(String param, int value) {
		setParam(param, Integer.toString(value));
	}
	
	public void setLong(String param, long value) {
		setParam(param, Long.toString(value));
	}
	
	public void setString(String param, String value) {
		setParam(param, value);
	}
	
	public void setBoolean(String param, boolean value) {
		setParam(param, Boolean.toString(value));
	}
	
	private void setParam(String param, String value) {
		FileInputStream fis = null;
		FileWriter writer = null;
		try {
			fis = new FileInputStream(m_filename);
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document configFile  = parser.parse(fis);
			fis.close();
			
			Element root = configFile.getDocumentElement();
			NodeList nodes = root.getChildNodes();
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = null;
				node = nodes.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE || node.getNodeName() != "property") {
					continue;
				}
				try {
					NamedNodeMap attributes = node.getAttributes();
					String name = attributes.getNamedItem("name").getNodeValue();
					
					if (name.equals(param)) {
						node.setTextContent(value);
						break;
					}
				}
				catch (NumberFormatException e) {
					ErrorDialog.show(e, "Error parsing config parameter");
					return;
				}
				catch (IllegalArgumentException e) {
					ErrorDialog.show(e, "Invalid parameter type");
					return;
				}
				catch (NullPointerException e) {
					ErrorDialog.show(e, "Null pointer exception");
					return;
				}
				finally {
				}
			}
			
			Source source = new DOMSource(configFile);
			File file = new File(m_filename);
			Result result = new StreamResult(file);
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		}
		catch (TransformerConfigurationException e) {
			ErrorDialog.show(e, "Could not write config file");
		}
		catch (TransformerException e) {
			ErrorDialog.show(e, "Could not write config file");
		}
		catch (SAXException e) {
			ErrorDialog.show(e, "SAX Exception");
		}
		catch (ParserConfigurationException e) {
			ErrorDialog.show(e, "Parser Configuration Exception");
		}
		catch (FileNotFoundException e) {
			ErrorDialog.show(e, "File Not Found Exception");
		}
		catch (IOException e) {
			ErrorDialog.show(e, "I/O Exception");
		}
		finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (writer != null) {
					writer.close();
				}
			}
			catch (IOException e) {
				ErrorDialog.show(e, "I/O Exception closing file");
			}
		}
	}
	
	public void loadConfigFile(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(filename);
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document configFile  = parser.parse(fis);
			fis.close();
			
			Element root = configFile.getDocumentElement();
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = null;
				node = nodes.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE || node.getNodeName() != "property") {
					continue;
				}
				try {
					NamedNodeMap attributes = node.getAttributes();
					String name = attributes.getNamedItem("name").getNodeValue();
					Types type = Types.valueOf(attributes.getNamedItem("type").getNodeValue());
					Object value = null;
					switch (type) {
						case String:
							value = node.getTextContent();
							break;
							
						case Integer:
							value = new Integer(node.getTextContent());
							break;
						
						case Long:
							value = new Long(node.getTextContent());
							break;
							
						case Boolean:
							value = new Boolean(node.getTextContent());
							break;
							
						default:
							value = null;
					}
					Log.getInstance().logMessage(getClass(), "Read config parameter: (" + name + ", \"" + value + "\")");
					put(name, value);
				}
				catch (NumberFormatException e) {
					ErrorDialog.show(e, "Error parsing config parameter");
				}
				catch (IllegalArgumentException e) {
					ErrorDialog.show(e, "Invalid parameter type");
				}
				catch (NullPointerException e) {
					ErrorDialog.show(e, "Invalid parameter value");
				}
			}
			
			m_filename = filename;
		}
		catch (SAXException e) {
			ErrorDialog.show(e, "SAX Exception");
		}
		catch (ParserConfigurationException e) {
			ErrorDialog.show(e, "Parse Exception");
		}
	}
}
