package pitchDetector;


import java.util.Iterator;
import java.util.Vector;

public class PitchCollection {
	private String m_name;
	private Vector<PitchSample> m_pitches = new Vector<PitchSample>();
	
	public void setName(String name) {
		m_name = name;
	}
	
	public void addPitch(PitchSample pitch) {
		m_pitches.add(pitch);
	}
	
	public String getName() {
		return m_name;
	}
	
	public Iterator getPitches() {
		return m_pitches.iterator();
	}
	
	public PitchSample getSample(String name) {
		PitchSample pitch;
		Iterator pitches = this.getPitches();
		while(pitches.hasNext()) {
			pitch = (PitchSample) pitches.next();
			if (pitch.getPitchName().equals(name)) {
				return pitch;
			}
		}
		return null;
	}
}
