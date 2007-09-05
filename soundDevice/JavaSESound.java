package soundDevice;

import java.io.*;

import misc.*;
import javax.sound.sampled.*;

public class JavaSESound extends SoundDevice {
	
	private TargetDataLine m_inLine = null;
	private SourceDataLine m_outLine = null;
	
	public JavaSESound(SoundInfo soundInfo) throws SoundDeviceException {
		super(soundInfo);
		
		AudioFormat format = new AudioFormat(
				getSoundInfo().getSampleRate(), 
				getSoundInfo().getSampleDepth(), 
				getSoundInfo().getSampleChannels(), 
				getSoundInfo().getSampleSigned(), 
				getSoundInfo().getSampleBigEndian()); 
		
		try {
			m_inLine = AudioSystem.getTargetDataLine(format);
			m_inLine.open(format, getSoundInfo().getFrameSize());
			m_inLine.start();
			m_outLine = AudioSystem.getSourceDataLine(format);
			m_outLine.open(format, getSoundInfo().getFrameSize());
			m_outLine.start();
			
			/*
			System.out.println(m_inLine.getBufferSize());
			System.out.println(m_inLine.getFramePosition());
			System.out.println(m_inLine.getLevel());			
			System.out.println(m_inLine.isActive());
			System.out.println(m_inLine.getFormat());
			System.out.println(m_inLine.getLineInfo());
			Control[] c = m_inLine.getControls();
			System.out.println(c.length);
			for (int i = 0; i < c.length; ++i) {
				System.out.println(c[i]);
			}
			
			System.out.println(m_outLine.getBufferSize());
			System.out.println(m_outLine.getFramePosition());
			System.out.println(m_outLine.getLevel());			
			System.out.println(m_outLine.isActive());
			System.out.println(m_outLine.getFormat());
			System.out.println(m_outLine.getLineInfo());
			c = m_outLine.getControls();
			System.out.println(c.length);
			for (int i = 0; i < c.length; ++i) {
				System.out.println(c[i]);
			}
			*/
		}
		catch (LineUnavailableException e) {
			throw new SoundDeviceException(e);
		}
	}
	
	public void finalize() {
		if (m_inLine != null) {
            m_inLine.stop();
            m_inLine.close();
		}
		if (m_outLine != null) {
            m_outLine.stop();
            m_outLine.close();
		}
	}
	
	public void readSample() throws SoundDeviceException {
		if (m_inLine.read(getSampleBuf(), 0, getSampleBuf().length) != getSampleBuf().length) {
			throw new SoundDeviceException("Could not read enough bytes");
		}
		
		writeToListeners();
	}
	
	public void writeSample(byte sampleBuf[]) throws SoundDeviceException {
		if (m_outLine.write(sampleBuf, 0, sampleBuf.length) != sampleBuf.length) {
			throw new SoundDeviceException("Could not write enough bytes");
		}
	}

	public void addListener(SampleListener listener) throws SoundDeviceException {
		BigPipedInputStream is;
		PipedOutputStream os;
		try {
			is = new BigPipedInputStream();
			os = new PipedOutputStream(is);
		}
		catch (IOException e) {
			throw new SoundDeviceException(e);
		}
		
		addListener(listener, is, os);
	}
	
}
