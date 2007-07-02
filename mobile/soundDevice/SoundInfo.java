package soundDevice;

public class SoundInfo {
	private float m_sampleRate;
	private int m_sampleDepth;
	private int m_sampleChannels;
	private int m_frameSize;
	private boolean m_sampleSigned;
	private boolean m_sampleBigEndian;
	
	public SoundInfo(
			float sampleRate, 
			int sampleDepth, 
			int sampleChannels, 
			boolean sampleSigned, 
			boolean sampleBigEndian, 
			int frameSize) { 
		m_sampleRate = sampleRate;
		m_sampleDepth = sampleDepth;
		m_sampleChannels = sampleChannels;
		m_sampleSigned = sampleSigned;
		m_sampleBigEndian = sampleBigEndian;
		m_frameSize = frameSize;
	}
		
	
	public float getSampleRate() {
		return m_sampleRate;
	}

	public int getSampleDepth() {
		return m_sampleDepth;
	}

	public int getSampleChannels() {
		return m_sampleChannels;
	}

	public boolean getSampleSigned() {
		return m_sampleSigned;
	}

	public boolean getSampleBigEndian() {
		return m_sampleBigEndian;
	}

	public int getFrameSize() {
		return m_frameSize;
	}
	
}
