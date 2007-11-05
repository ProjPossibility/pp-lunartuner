package pitchDetector;

import java.io.*;
import soundDevice.SoundDevice;
import misc.Fftw;

public class HspDetector extends PitchDetector {
	final private int PAD_FACTOR = 32;
	final private int HARMONICS = 7;
	final private int SAMPLE_AVG = 5;
	final private double FREQ_MIN = 70.0;
	final private double FREQ_MAX = 2500.0;
	
	private double[] m_sample = null;
	private double[] m_fft = null;
	private double[] m_freqScale = null;
	private double[][] m_hspData = null;
	private double[] m_hsp = null;
	private int m_samples;
	private int m_samplesPadded;
	private int m_fftLen;
	private double m_fftRes;
	private int m_fftIdxMin;
	private int m_fftIdxMax;
	
	private double m_runAvg;
	private int m_runAvgCount;
	private int m_runAvgOld;
	private int m_runAvgNew;
	private double[] m_runAvgArr;
	
	private FileWriter m_dataFile = null;
	public HspDetector(SoundDevice soundDevice) throws PitchDetectorException {
		super(soundDevice);
    	
		/*
		try {
			m_dataFile = new FileWriter("data.txt");
		}
		catch (IOException e) {
			System.out.println("Could not open data file");
		}
		*/
		
		m_samples = soundDevice.getSoundInfo().getFrameSize();
		m_samplesPadded = m_samples * PAD_FACTOR;
		m_fftLen = m_samplesPadded/2 + 1;
		
		m_sample = new double[m_samplesPadded];
		m_fft = new double[2 * m_samplesPadded + 2];
		m_freqScale = new double[m_fftLen];
		m_hspData = new double[HARMONICS][m_fftLen];
		m_hsp = new double[m_fftLen];
		
		m_runAvg = 0;
		m_runAvgOld = 0;
		m_runAvgNew = 0;
		m_runAvgArr = new double[SAMPLE_AVG];
		m_runAvgCount = 0;
		
		calcFreqScale();
		
		m_fftIdxMin = (int)(FREQ_MIN / m_fftRes);
		m_fftIdxMax = (int)(FREQ_MAX / m_fftRes);
	}
	
	private void calcFreqScale() {
		double ts = getSoundDevice().getSoundInfo().getSampleRate();
		
		for (int i = 0; i < m_fftLen; ++i) {
			m_freqScale[i] = ts * (((double)i) / ((double)m_samplesPadded));
		}
		
		m_fftRes = m_freqScale[1] - m_freqScale[0];
	}
	
	public void calcPitch() throws PitchDetectorException {		
		short[] shortAudioBuf;
		
		readSample();
		
		// Get audio buffer from parent
		shortAudioBuf = getAudioBuf();
		
		// Convert from int16 to double
		// Maybe try to increase dynamic range with scaling?
		for (int i = 0; i < m_samples; ++i) {
			m_sample[i] = shortAudioBuf[i];
		}
		
		// Calculate fft (native call) and copy into hsp calc buffer
		Fftw.fft(m_sample, m_fft);
		for (int i = 0; i < m_fftLen; ++i) {
			m_hspData[0][i] = 2.0f * Math.sqrt(m_fft[2*i] * m_fft[2*i] + m_fft[2*i+1] * m_fft[2*i+1]);
		}
		
		// Resample signal based on harmonic divisions
		for (int i = 1; i < HARMONICS; ++i) {
			int r = (int)Math.floor(((double)m_fftLen) / ((double)(i + 1)));
			for (int j = 0; j < r; ++j) {
				m_hspData[i][j] = 0;
				for (int k = 0; k < i; ++k) {
					m_hspData[i][j] += m_hspData[0][j * i + k];
				}
				m_hspData[i][j] /= i;
			}
		}
		
		// Multiply signal by resampled subsignals to amplify fundamental harmonic
		System.arraycopy(m_hspData[0], 0, m_hsp, 0, m_fftLen);
		for (int i = 1; i < HARMONICS; ++i) {
			for (int j = 0; j < m_fftIdxMax; ++j) {
				m_hsp[j] *= m_hspData[i][j];
			}
		}
		
		// Find frequency with highest magnitude to get pitch
		int maxIdx = 0;
		//try {
			for (int i = m_fftIdxMin; i < m_fftIdxMax; ++i) {
				if (m_hsp[i] > m_hsp[maxIdx]) {
					maxIdx = i;
				}
				//m_dataFile.write(m_hspData[0][i] + " ");
			}
			//m_dataFile.write("\n");
		//}
		//catch (IOException e) {
		//	System.out.println("Could not write vector");
		//	System.exit(1);
		//}
		
		//setPitch(m_freqScale[maxIdx]);
		setPitch(calcRunAvg(m_freqScale[maxIdx]));
		//System.out.println(getPitch() + "/" + m_fftRes);
	}
	
	double calcRunAvg(double curPitch) {
		
		double diff;
		// If large pitch shift restart averaging
		diff = 1.0f - ((curPitch < getPitch()) ? curPitch/getPitch() : getPitch()/curPitch);
		
		if (diff > 0.20) {
			m_runAvgCount = 0;
			m_runAvg = 0;
			m_runAvgOld = m_runAvgNew;
			//System.out.println("Starting new avg " + diff);
		}
		
		// Do running average calcs
		if (m_runAvgCount >= SAMPLE_AVG) {
			m_runAvg -= m_runAvgArr[m_runAvgOld];
			m_runAvgOld = (m_runAvgOld + 1) % SAMPLE_AVG;
		}
		
		m_runAvgArr[m_runAvgNew] = curPitch;
		m_runAvgNew = (m_runAvgNew + 1) % SAMPLE_AVG;
		
		m_runAvg += curPitch;		
		
		if (m_runAvgCount < SAMPLE_AVG) {
			++m_runAvgCount;
			return (m_runAvg/((double)m_runAvgCount));
		}
		else {
			return (m_runAvg/((double)SAMPLE_AVG));
		}
	}
	
}
