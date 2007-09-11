package soundDevice;

import java.io.*;
import java.util.*;

public abstract class SoundDevice {
	
	private SoundInfo m_soundInfo = null;
	private byte[] m_sampleBuf = null;
	private Vector m_outStreams = null;
	private Vector m_inStreams = null;
	
	public SoundDevice(SoundInfo soundInfo) throws SoundDeviceException {
		if (soundInfo.getSampleDepth() != 8 && soundInfo.getSampleDepth() != 16) {
			throw new SoundDeviceException("Invalid sample depth");
		}
		
		setSoundInfo(soundInfo);
		
		m_outStreams = new Vector();
		m_inStreams = new Vector();
		m_sampleBuf = new byte[getSoundInfo().getFrameSize() * (getSoundInfo().getSampleDepth() / 8)];
	}
	
	public abstract void readSample() throws SoundDeviceException;
	public abstract void writeSample(byte sampleBuf[]) throws SoundDeviceException;
	
	public abstract void addListener(SampleListener listener) throws SoundDeviceException;
	
	public void removeListener(SampleListener listener) {
		for (int i = 0; i < m_inStreams.size(); ++i) {
			if (m_inStreams.elementAt(i) == listener.getAudioStream()) {
				m_inStreams.remove(i);
				m_outStreams.remove(i);
			}
		}
	}
	
	public SoundInfo getSoundInfo() {
		return m_soundInfo;
	}
	
	public void setSoundInfo(SoundInfo soundInfo) {
		m_soundInfo = soundInfo;
		
	}
	
	public int getSampleBufLength() {
		return m_sampleBuf.length;
	}
	
	protected void addListener(SampleListener listener, InputStream is, OutputStream os) throws SoundDeviceException {
		listener.setAudioStream(is);
		m_inStreams.add(is);
		m_outStreams.add(os);
	}
	
	protected void writeToListeners() throws SoundDeviceException {
		try {
			for (int i = 0; i < m_outStreams.size(); ++i) {
				((OutputStream)m_outStreams.elementAt(i)).write(m_sampleBuf, 0, m_sampleBuf.length);
			}
		} catch (IOException e) {
			throw new SoundDeviceException(e);
		}
	}
	
	protected byte[] getSampleBuf() {
		return m_sampleBuf;
	}
	
	public byte[] createTone(double f, double seconds) throws SoundDeviceException {
		if (getSoundInfo().getSampleDepth() != 16) {
			throw new SoundDeviceException("Only 16 bit tone generation currently supported");
		}
		
		double t = 0.0f;
		double dt = 1.0f / getSoundInfo().getSampleRate();
		
		int m_len = (int)Math.ceil(seconds / dt);
		m_len += m_len % getSoundInfo().getFrameSize(); // Pad buffer to be a multiple of frame size
		
		byte[] m_toneBuf = new byte[m_len];
		
		for (int i = 0; i < m_len; i += 2, t += dt) {
			// Generate the tone. Have the amplitude be 2/3 of the maximum
			short tone = (short)Math.round( Short.MAX_VALUE * Math.sin(f * 2.0f * Math.PI * t) );
			if (getSoundInfo().getSampleBigEndian()) {
				m_toneBuf[i] = (byte)((tone & 0xFF00) >> 8);
				m_toneBuf[i + 1] = (byte)(tone & 0x00FF);
			} else {
				m_toneBuf[i] = (byte)(tone & 0x00FF);
				m_toneBuf[i + 1] = (byte)((tone & 0xFF00) >> 8);
			}
		}
		
		return m_toneBuf;
	}
	
	public static class SoundDeviceException extends Exception {
		final static public long serialVersionUID = 0;
		
		public SoundDeviceException() {
			super();
		}
		
		public SoundDeviceException(String msg) {
			super(msg);
		}
		
		public SoundDeviceException(Exception e) {
			super(e);
		}
	}
	
}
