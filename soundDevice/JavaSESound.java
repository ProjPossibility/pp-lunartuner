package soundDevice;

import misc.BigPipedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.PipedOutputStream;

public class JavaSESound extends SoundDevice {
	
	private TargetDataLine m_line = null;
	
	public JavaSESound(SoundInfo soundInfo) throws SoundDeviceException {
		super(soundInfo);
		
		AudioFormat format = new AudioFormat(
				getSoundInfo().getSampleRate(), 
				getSoundInfo().getSampleDepth(), 
				getSoundInfo().getSampleChannels(), 
				getSoundInfo().getSampleSigned(), 
				getSoundInfo().getSampleBigEndian()); 
		
		try {
			m_line = AudioSystem.getTargetDataLine(format);
			m_line.open(format, getSoundInfo().getFrameSize());
			m_line.start();
		}
		catch (LineUnavailableException e) {
			throw new SoundDeviceException(e);
		}
	}
	
	public void finalize() {
		if (m_line != null) {
            m_line.stop();
            m_line.close();
		}
	}
	
	public void readSample() throws SoundDeviceException {
		if (m_line.read(getSampleBuf(), 0, getSampleBuf().length) != getSampleBuf().length) {
			throw new SoundDeviceException("Could not read enough bytes");
		}
		
		writeToListeners();
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
