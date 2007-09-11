package pitchDetector;


/*
 *	Must run init() before using this class! 
 * 
 */

public class PitchAnalyzer {

	private static final int PITCH_RANGE = 200;
	
	private double[] m_pitchFreqMap;
	private String[] m_pitchNameMap;
	private PitchSample m_currentTuneNote = null;
	
	public PitchAnalyzer() {

		m_pitchFreqMap = new double[PITCH_RANGE];
		m_pitchNameMap = new String[PITCH_RANGE];
		
		String basePitchNameMap[] = new String[12];
		
		// Set up pitch names
		basePitchNameMap[0] = "A";
		basePitchNameMap[1] = "Bb";
		basePitchNameMap[2] = "B";
		basePitchNameMap[3] = "C";
		basePitchNameMap[4] = "C#";
		basePitchNameMap[5] = "D";
		basePitchNameMap[6] = "Eb";
		basePitchNameMap[7] = "E";
		basePitchNameMap[8] = "F";
		basePitchNameMap[9] = "F#";
		basePitchNameMap[10] = "G";
		basePitchNameMap[11] = "Ab";
		
		// Build freq to pitch map for pitches 0-PITCH_RANGE
		for(int i=0; i<PITCH_RANGE; i++) {
			// Uses standard equation for calculating pitch in pitch space
			m_pitchFreqMap[i] = 440.0 * Math.pow(2.0,((double)i-69.0)/12.0);
			// Assign name as reference to basePitchNameMap
			m_pitchNameMap[i] = basePitchNameMap[ (i + 3) % 12 ];
			
			// Debug
			System.out.println("Name: " + m_pitchNameMap[i] + " Num: " + i + " Freq: " + m_pitchFreqMap[i]);
		}
	}

	public PitchSample analyze (double sampleValue) {
		double semitone_difference, pitchErrorNormalized, pitch, pitchErrorHz;
		int nameIndex;
		
		// Find the closest note by using the standard equation for calculating pitch
		pitch = 69 + 12 * (Math.log10(sampleValue/440.0)/Math.log10(2));
		nameIndex = (int)Math.round(pitch);
		
		// Set bounds for nameIndex so that we don't have array-out-of-bounds exception
		if(nameIndex < 0 || nameIndex > PITCH_RANGE-1) {
			nameIndex = 0;
		}
		
		if(m_currentTuneNote == null) {
			pitchErrorHz = sampleValue - m_pitchFreqMap[nameIndex];
		}
		else {
			pitchErrorHz = sampleValue - m_pitchFreqMap[m_currentTuneNote.getPitchNum()];
			
		}

		if(nameIndex >= 1) {
			semitone_difference = m_pitchFreqMap[nameIndex] - m_pitchFreqMap[nameIndex-1]; 
		}
		else {
			semitone_difference = m_pitchFreqMap[nameIndex+1] - m_pitchFreqMap[nameIndex];
		}
		
		pitchErrorNormalized = 100.0 * pitchErrorHz / semitone_difference; 
		
		return new PitchSample(m_pitchNameMap[nameIndex], nameIndex, sampleValue, pitchErrorHz, pitchErrorNormalized);
	}

	public double compareHz (PitchSample a, PitchSample b) {
		return 0;
	}
	
	public double compareNormalized (PitchSample a, PitchSample b) {
		return 0;
	}
	
	public void setCurrentTuneNote (PitchSample current) {
		m_currentTuneNote = current;
	}
	
	public void resetCurrentTuneNote () {
		m_currentTuneNote = null;
	}
	
	public boolean currentTuneNoteIsSet() {
		if (m_currentTuneNote == null) {
			return false;
		}
		return true;
	}
	
	public double getPitchFreq(int pitchNum) {
		return m_pitchFreqMap[pitchNum];
	}
}
