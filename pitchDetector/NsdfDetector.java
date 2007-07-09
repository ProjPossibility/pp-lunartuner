package pitchDetector;

import soundDevice.SoundDevice;
import java.util.LinkedList;
import java.util.ListIterator;

public class NsdfDetector extends PitchDetector {
	
	private double[] m_nsdf = null; 
	private double[] m_dnsdf = null;
	private LinkedList m_maxima = null;

	public NsdfDetector(SoundDevice soundDevice) throws PitchDetectorException {
		super(soundDevice);
		
		int frameSize = getSoundDevice().getSoundInfo().getFrameSize();
		m_nsdf = new double[frameSize];
		m_dnsdf = new double[frameSize];
		m_maxima = new LinkedList();
	}
	
	public void calcPitch() throws PitchDetectorException {		
		double acf;
		double sdf;
		short[] audioBuf = null;
		
		readSample();
		audioBuf = getAudioBuf();
		
		for (int tau = 0; tau < audioBuf.length; ++tau) {
			acf = 0;
			sdf = 0;
			for (int j = 0; j < audioBuf.length - tau; ++j) {
				acf += audioBuf[j] * audioBuf[j + tau];
				sdf += audioBuf[j] * audioBuf[j] + audioBuf[j + tau] * audioBuf[j + tau];
			}
			
			if (sdf == 0) {
				m_nsdf[tau] = 0;
			}
			else {
				m_nsdf[tau] = (2.0f * acf) / sdf;
			}
			
			if (tau > 1) {
				m_dnsdf[tau] = m_nsdf[tau] - m_nsdf[tau - 1];
			}	
		}
		
		m_maxima.clear();
		double highestPeak = 0;
		for (int i = 0; i < m_dnsdf.length - 1; ++i) {
			if (m_dnsdf[i] > 0 && m_dnsdf[i + 1] < 0) {
				m_maxima.addLast(new Maxima(i, m_nsdf[i]));
				if (m_nsdf[i] > highestPeak) {
					highestPeak = m_nsdf[i];
				}
			}
		}
		
		setPitch(-1);
		ListIterator i = m_maxima.listIterator();
		while (i.hasNext()) {
			Maxima m = (Maxima)i.next();
			if (m.getValue() > 0.9 * highestPeak) {
				setPitch(getSoundDevice().getSoundInfo().getSampleRate() / m.getIndex());
				break;
			}
		}		
		
		if (getPitch() < PITCH_THRESHOLD_LOW || getPitch() > PITCH_THRESHOLD_HIGH) {
			setPitch(-1);
		}
	}
	
}
