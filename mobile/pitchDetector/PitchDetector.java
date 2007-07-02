package pitchDetector;

import soundDevice.*;
import soundDevice.SoundDevice.*;

//import java.nio.ByteOrder;
import java.io.InputStream;
import java.io.IOException;
//import java.util.LinkedList;
//import java.util.ListIterator;
import java.util.Vector;

public class PitchDetector implements SampleListener {
	
	final private static double PITCH_THRESHOLD_LOW = 20.0f;
	final private static double PITCH_THRESHOLD_HIGH = 1000.0f;	
	
	private SoundDevice m_soundDevice = null;
	private InputStream m_sampleis = null;
	
	private byte[] m_sampleBuf = null;
	private short[] m_audioBuf = null;
	private double[] m_nsdf = null; 
	private double[] m_dnsdf = null;
	private Vector m_maxima = null;
	private BytesToShorts m_convBytes = null;
	private double m_pitch;
	
	public PitchDetector(SoundDevice soundDevice) throws PitchDetectorException {
		m_soundDevice = soundDevice;
		
		try {
			m_soundDevice.addListener(this);
		}
		catch (SoundDeviceException e) {
			throw new PitchDetectorException(e);
		}
		
		int frameSize = m_soundDevice.getSoundInfo().getFrameSize();
		m_sampleBuf = new byte[m_soundDevice.getSampleBufLength()];
		m_audioBuf = new short[frameSize];
		m_nsdf = new double[frameSize];
		m_dnsdf = new double[frameSize];
		m_maxima = new Vector();
		
		if (m_soundDevice.getSoundInfo().getSampleDepth() == 8) {
			m_convBytes = new NoConversion();
		}
		else
		//if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
			m_convBytes = new BigEndianBytesToShorts();
		//}
		//else {
		//	m_convBytes = new LittleEndianBytesToShorts();
		//}
	}
	
	public double getPitch() {
		return m_pitch;
	}
	
	public void calcPitch() throws PitchDetectorException {
		getSample();
		
		double acf;
		double sdf;
		
		for (int tau = 0; tau < m_audioBuf.length; ++tau) {
			acf = 0;
			sdf = 0;
			for (int j = 0; j < m_audioBuf.length - tau; ++j) {
				acf += m_audioBuf[j] * m_audioBuf[j + tau];
				sdf += m_audioBuf[j] * m_audioBuf[j] + m_audioBuf[j + tau] * m_audioBuf[j + tau];
			}
			
			if (sdf == 0) {
				m_nsdf[tau] = 0;
			}
			else {
				m_nsdf[tau] = (2.0f * acf) / sdf;
			}
			
			if (tau > 1) {
				m_dnsdf[tau] = m_nsdf[tau] - m_nsdf[tau - 1];
			}	
		}
		
		m_maxima.removeAllElements();
		double highestPeak = 0;
		for (int i = 0; i < m_dnsdf.length - 1; ++i) {
			if (m_dnsdf[i] > 0 && m_dnsdf[i + 1] < 0) {
				m_maxima.addElement(new Maxima(i, m_nsdf[i]));
				if (m_nsdf[i] > highestPeak) {
					highestPeak = m_nsdf[i];
				}
			}
		}
		
		m_pitch = -1;
		for (int i = 0; i < m_maxima.size(); i++) {
			Maxima m = (Maxima)m_maxima.elementAt(i);
			if (m.getValue() > 0.9 * highestPeak) {
				m_pitch = m_soundDevice.getSoundInfo().getSampleRate() / m.getIndex();
				break;
			}
		}		
		
		if (m_pitch < PITCH_THRESHOLD_LOW || m_pitch > PITCH_THRESHOLD_HIGH) {
			m_pitch = -1;
		}
	}
	
	public void getSample() throws PitchDetectorException {
		try {
			m_sampleis.read(m_sampleBuf);
		}
		catch (IOException e) {
			throw new PitchDetectorException(e);
		}
		
		m_convBytes.convert(m_sampleBuf, m_audioBuf);
	}
	
	public void setAudioStream(InputStream is) {
		m_sampleis = is;
	}
	
	public InputStream getAudioStream() {
		return m_sampleis;
	}
		
	private static class Maxima {
		private int m_index;
		private double m_value;
		
		public Maxima(int index, double value) {
			m_index = index;
			m_value = value;
		}
		
		public int getIndex() {
			return m_index;
		}
		
		public double getValue() {
			return m_value;
		}
	}
	
	public interface BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray);
	}
	
	public static class BigEndianBytesToShorts implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
			for (int i = 0, j = 0; i < byteArray.length; i += 2, ++j) {
				shortArray[j] = 
					(short)(0xFF00 & (byteArray[i] << 8) | (short)(0x00FF & byteArray[i + 1]));
			}
		}
	}

	public static class LittleEndianBytesToShorts implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
			for (int i = 0, j = 0; i < byteArray.length; i += 2, ++j) {
				shortArray[j] = 
					(short)(0xFF00 & (byteArray[i + 1] << 8) | (short)(0x00FF & byteArray[i]));
			}
		}
	}
	
	// This is kinda dumb but efficient
	public static class NoConversion implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
		}
	}
	
	public static class PitchDetectorException extends Exception {
		final static public long serialVersionUID = 0;
		
		public PitchDetectorException() {
			super();
		}
		
		public PitchDetectorException(String msg) {
			super(msg);
		}
		
		public PitchDetectorException(Exception e) {
			this(e.getMessage());
		}
	}
	
}
