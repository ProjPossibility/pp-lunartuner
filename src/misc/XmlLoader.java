package misc;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XmlLoader extends HashMap<String, Object> {
	static final private long serialVersionUID = 0;
	
	static private enum Types {
		String,
		Integer,
		Long,
		Double,
		Boolean
	}
	
	private String m_filename = null;
	
	public XmlLoader() { }

	public XmlLoader(String fname) {
		try {
			loadFile(fname);
		}
		catch (XmlLoaderException e) {
			ErrorDialog.show(e, "Error loading xml file " + fname);
		}
	}
	
	public int getInt(String key) {
		return ((Integer)get(key)).intValue();
	}
	
	public long getLong(String key) {
		return ((Long)get(key)).longValue();
	}
	
	public long getDouble(String key) {
		return ((Double)get(key)).longValue();
	}
	
	public String getString(String key) {
		return (String)get(key);
	}
	
	public boolean getBoolean(String key) {
		return ((Boolean)get(key)).booleanValue();
	}
	
	public void setInt(String param, int value) throws XmlLoaderException {
		setParam(param, Integer.toString(value));
	}
	
	public void setLong(String param, long value) throws XmlLoaderException {
		setParam(param, Long.toString(value));
	}
	
	public void setDouble(String param, double value) throws XmlLoaderException {
		setParam(param, Double.toString(value));
	}
	
	public void setString(String param, String value) throws XmlLoaderException {
		setParam(param, value);
	}
	
	public void setBoolean(String param, boolean value) throws XmlLoaderException {
		setParam(param, Boolean.toString(value));
	}
	
	private void setParam(String param, String value) throws XmlLoaderException {
		FileInputStream fis = null;
		FileWriter writer = null;
		
		try {
			fis = new FileInputStream(m_filename);
			
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document inFile  = parser.parse(fis);
			fis.close();
			
			Element root = inFile.getDocumentElement();
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
					throw new XmlLoaderException(e);
				}
				catch (IllegalArgumentException e) {
					throw new XmlLoaderException(e);
				}
				catch (NullPointerException e) {
					throw new XmlLoaderException(e);
				}
				finally {
				}
			}
			
			Source source = new DOMSource(inFile);
			File outFile = new File(m_filename);
			Result result = new StreamResult(outFile);
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
		}
		catch (TransformerConfigurationException e) {
			throw new XmlLoaderException(e);
		}
		catch (TransformerException e) {
			throw new XmlLoaderException(e);
		}
		catch (SAXException e) {
			throw new XmlLoaderException(e);
		}
		catch (ParserConfigurationException e) {
			throw new XmlLoaderException(e);
		}
		catch (FileNotFoundException e) {
			throw new XmlLoaderException(e);
		}
		catch (IOException e) {
			throw new XmlLoaderException(e);
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
				throw new XmlLoaderException(e);
			}
		}
	}
	
	public void loadFile(String filename) throws XmlLoaderException {
		try {
			FileInputStream fis = new FileInputStream(filename);
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
							
						case Double:
							value = new Double(node.getTextContent());
							break;
						
						case Boolean:
							value = new Boolean(node.getTextContent());
							break;
							
						default:
							value = null;
					}
					//Log.getInstance().logMessage(getClass(), "Read parameter: (" + name + ", \"" + value + "\")");
					put(name, value);
				}
				catch (NumberFormatException e) {
					throw new XmlLoaderException(e);
				}
				catch (IllegalArgumentException e) {
					throw new XmlLoaderException(e);
				}
				catch (NullPointerException e) {
					throw new XmlLoaderException(e);
				}
			}
			
			m_filename = filename;
		}
		catch (SAXException e) {
			throw new XmlLoaderException(e);
		}
		catch (ParserConfigurationException e) {
			throw new XmlLoaderException(e);
		}
		catch (IOException e) {
			throw new XmlLoaderException(e);
		}
	}
	
	static public class XmlLoaderException extends Exception {
		final static public long serialVersionUID = 0;
		
		public XmlLoaderException() {
			super();
		}
		
		public XmlLoaderException(String msg) {
			super(msg);
		}
		
		public XmlLoaderException(Exception e) {
			super(e);
		}
	}
}
