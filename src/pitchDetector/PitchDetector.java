package pitchDetector;

import soundDevice.*;
import soundDevice.SoundDevice.*;

import java.nio.ByteOrder;
import java.io.InputStream;
import java.io.IOException;

public abstract class PitchDetector implements SampleListener {
	
	final public static double PITCH_THRESHOLD_LOW = 20.0f;
	final public static double PITCH_THRESHOLD_HIGH = 1000.0f;	
	
	private SoundDevice m_soundDevice = null;
	private InputStream m_sampleis = null;
	
	private byte[] m_sampleBuf = null;
	private short[] m_audioBuf = null;
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
		
		if (m_soundDevice.getSoundInfo().getSampleDepth() == 8) {
			m_convBytes = new NoConversion();
		}
		else
		if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
			m_convBytes = new BigEndianBytesToShorts();
		}
		else {
			m_convBytes = new LittleEndianBytesToShorts();
		}
	}
	
	abstract public void calcPitch() throws PitchDetectorException;
	
	protected void setPitch(double pitch) {
		m_pitch = pitch;
	}
	
	public double getPitch() {
		return m_pitch;
	}
		
	protected void readSample() throws PitchDetectorException {
		try {
			m_sampleis.read(m_sampleBuf);
		}
		catch (IOException e) {
			throw new PitchDetectorException(e);
		}
		
		m_convBytes.convert(m_sampleBuf, m_audioBuf);
	}
	
	protected short[] getAudioBuf() {
		return m_audioBuf;
	}
	
	public void setAudioStream(InputStream is) {
		m_sampleis = is;
	}
	
	public InputStream getAudioStream() {
		return m_sampleis;
	}
	
	protected SoundDevice getSoundDevice() {
		return m_soundDevice;
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
			super(e);
		}
	}
	
	protected static class Maxima {
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
	
	private interface BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray);
	}
	
	private static class BigEndianBytesToShorts implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
			for (int i = 0, j = 0; i < byteArray.length; i += 2, ++j) {
				shortArray[j] = 
					(short)(0xFF00 & (byteArray[i] << 8) | (short)(0x00FF & byteArray[i + 1]));
			}
		}
	}
	
	private static class LittleEndianBytesToShorts implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
			for (int i = 0, j = 0; i < byteArray.length; i += 2, ++j) {
				shortArray[j] = 
					(short)(0xFF00 & (byteArray[i + 1] << 8) | (short)(0x00FF & byteArray[i]));
			}
		}
	}
	
	// This is kinda dumb but efficient
	private static class NoConversion implements BytesToShorts {
		public void convert(byte[] byteArray, short[] shortArray) {
		}
	}
	
}
