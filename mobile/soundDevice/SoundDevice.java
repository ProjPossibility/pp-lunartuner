package soundDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

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
	
	public abstract void addListener(SampleListener listener) throws SoundDeviceException;

	public void removeListener(SampleListener listener) {
		for (int i = 0; i < m_inStreams.size(); ++i) {
			if (m_inStreams.elementAt(i) == listener.getAudioStream()) {
				m_inStreams.removeElementAt(i);
				m_outStreams.removeElementAt(i);
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
		m_inStreams.addElement(is);
		m_outStreams.addElement(os);
	}
		
	protected void writeToListeners() throws SoundDeviceException {
		try {
			for (int i = 0; i < m_outStreams.size(); ++i) {
				((OutputStream)m_outStreams.elementAt(i)).write(m_sampleBuf, 0, m_sampleBuf.length);
			}
		}
		catch (IOException e) {
			throw new SoundDeviceException(e);
		}
	}
	
	protected byte[] getSampleBuf() {
		return m_sampleBuf;
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
			super(e.getMessage());
		}
	}
	
}
