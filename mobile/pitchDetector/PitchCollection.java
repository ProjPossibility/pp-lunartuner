package pitchDetector;


import java.util.Vector;

public class PitchCollection {
	private String m_name;
	private Vector m_pitches = new Vector();
	
	public void setName(String name) {
		m_name = name;
	}
	
	public void addPitch(PitchSample pitch) {
		m_pitches.addElement(pitch);
	}
	
	public String getName() {
		return m_name;
	}
	
	public Vector getPitches() {
		return m_pitches;
	}
	
	public PitchSample getSample(String name) {
		PitchSample pitch;
		for(int i = 0; i < m_pitches.size(); i++) {
			pitch = (PitchSample) m_pitches.elementAt(i);
			if (pitch.getPitchName().equals(name)) {
				return pitch;
			}
		}
		return null;
	}
}
