package misc;

import java.util.*;
import java.io.*;

import pitchDetector.PitchAnalyzer;
import misc.XmlLoader.XmlLoaderException;

public class InstrumentInfo {
	static private InstrumentInfo m_instance = new InstrumentInfo();
	
	private TreeMap m_instruments = new TreeMap();
	
	static public InstrumentInfo getInstance() {
		return m_instance;
	}
	
	private InstrumentInfo() {
		File instDir = new File("instruments");
		if (!instDir.exists()) {
			return;
		}
		
		File[] instFiles = instDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.endsWith(".xml")) {
					return true;
				}
				else {
					return false;
				}
			}
			
		});
		
		for (int i = 0; i < instFiles.length; ++i) {
			Instrument inst = new Instrument(instFiles[i].getPath());
			if (inst.getNoteCount() > 0) {
				m_instruments.put(inst.getName(), inst);
			}
		}
	}
	
	public Instrument[] getInstruments() {
		Object[] values = m_instruments.values().toArray();
		Instrument[] instSorted = new Instrument[values.length];
		for (int i = 0; i < instSorted.length; ++i) {
			instSorted[i] = (Instrument)values[i];
		}
		return instSorted;
	}
	
	static public class Instrument {
		private String m_name = null;
		private TreeMap m_notes = new TreeMap();
		
		public Instrument() {
		   m_name = "Automatic";
			String[] pitchNames = PitchAnalyzer.getPitchNameMap();
			double[] pitchFreqs = PitchAnalyzer.getPitchFreqMap();
			for (int i = 0; i < pitchNames.length; ++i) {
				String freq = Double.toString(pitchFreqs[i]);
				int decIdx = freq.indexOf(".");
				int rndIdx = (decIdx + 3 < freq.length()) ? decIdx + 3 : freq.length();
				
				InstrumentNote note = new InstrumentNote(i, i, 
						i + ". " + pitchNames[i] + " (" + freq.substring(0, rndIdx) + " Hz)");
				m_notes.put(new Integer(i), note);
			}
		}
		
		public Instrument(String fname) {
			XmlLoader instXml = new XmlLoader();
			try {
				instXml.loadFile(fname);
			}
			catch (XmlLoaderException e) {
				ErrorDialog.show(e, "Error loading xml file " + fname);
			}
			
			int noteCount = instXml.getInt("note_count");
			m_name = instXml.getString("instrument_name");
			for (int i = 0; i < noteCount; ++i) {
				InstrumentNote note = new InstrumentNote(i, instXml.getInt("freq_" + i), instXml.getString("name_" + i));
				m_notes.put(new Integer(i), note);
			}
		}
		
		public String getName() { return m_name; }
		public int getNoteCount() { return m_notes.size(); }
		
		public InstrumentNote[] getNotes() {
			InstrumentNote[] sortedNotes = new InstrumentNote[m_notes.size()];
			Object[] values = m_notes.values().toArray();
			for (int i = 0; i < sortedNotes.length; ++i) {
				sortedNotes[i] = (InstrumentNote)values[i];
			}
			return sortedNotes;
		}
		
		public String toString() { return m_name; }
	}
	
	static public class InstrumentNote {
		private int m_index;
		private int m_freqIdx;
		private String m_name;
		
		public InstrumentNote() {
		   m_index = -1;
		   m_freqIdx = -1;
		   m_name = "";
		}
		
		public InstrumentNote(int index, int freqIdx, String name) {
			m_index = index;
			m_freqIdx = freqIdx;
			m_name = name;
		}
		
		public int getIndex() { return m_index; }
		public int getFreqIdx() { return m_freqIdx; }
		public String getName() { return m_name; }
		public String toString() { return m_name; }
	}
	
}
