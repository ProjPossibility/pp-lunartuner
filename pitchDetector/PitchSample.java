package pitchDetector;

public class PitchSample {

	private String m_pitchName = null;
	private int m_pitchNum = 0;
	private double m_pitchFreq = 0;
	private double m_pitchErrorNormalized = 0;  // How much error in % as function of 1 semitone
	private double m_pitchErrorHz = 0;  // How much error in hz
	
	public PitchSample(String name, int pitchNum) {
		m_pitchName = name;
		m_pitchNum = pitchNum;
	}
	
	public PitchSample(String name, int pitchNum, double freq, double hzError, double normalizedError) {
		m_pitchName = name;
		m_pitchFreq = freq;
		m_pitchErrorNormalized = normalizedError;
		m_pitchErrorHz = hzError;
	}
	
	public String getPitchName() {
		return m_pitchName;
	}

	public double getPitchFreq() {
		return m_pitchFreq;
	}

	public double getPitchErrorHz() {
		return m_pitchErrorHz;
	}

	public double getPitchErrorNormalized() {
		return m_pitchErrorNormalized;
	}

	public int getPitchNum() {
		return m_pitchNum;
	}
	
	public void setPitchName(String pitchName) {
		m_pitchName = pitchName;
	}

	public void setPitchFreq(double pitchFreq) {
		m_pitchFreq = pitchFreq;
	}
	
	public void setPitchErrorHz(double pitchError) {
		m_pitchErrorHz = pitchError;
	}
	
	public void setPitchErrorNormalized(double pitchError) {
		m_pitchErrorNormalized = pitchError;
	}

	public void setPitchNum(int pitchNum) {
		m_pitchNum = pitchNum;
	}
	
	public void reset() {
		m_pitchName = null;
		m_pitchFreq = 0;
		m_pitchErrorHz = 0;
		m_pitchErrorNormalized = 0;
	}
}
