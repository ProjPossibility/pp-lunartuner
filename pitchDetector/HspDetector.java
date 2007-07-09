package pitchDetector;

import soundDevice.SoundDevice;
import jfftw.*;

public class HspDetector extends PitchDetector {
	final private int PAD_FACTOR = 32;
	final private int HARMONICS = 5;
	private double[] m_audioBuf = null;
	private double[] m_fft = null;
	private double[] m_freqScale = null;
	private double[][] m_hspData = null;
	private double[] m_hsp = null;
	private int m_samples;
	private int m_samplesPadded;
	private int m_fftLen;
	private double m_fftRes;
	private RealDataArray m_fftw;
	
	public HspDetector(SoundDevice soundDevice) throws PitchDetectorException {
		super(soundDevice);
		
		m_samples = soundDevice.getSoundInfo().getFrameSize();
		m_samplesPadded = m_samples * PAD_FACTOR;
		m_fftLen = m_samplesPadded/2;
		
		m_audioBuf = new double[m_samplesPadded];
		m_freqScale = new double[m_fftLen];
		m_hspData = new double[HARMONICS][m_fftLen];
		m_hsp = new double[m_fftLen];
		
		calcFreqScale();
		
		m_fftw = new FFTWReal();
	}
	
	private void calcFreqScale() {
		double ts = getSoundDevice().getSoundInfo().getSampleRate();
		
		for (int i = 0; i < m_fftLen; ++i) {
			m_freqScale[i] = ts * (i / m_samplesPadded);
		}
		
		m_fftRes = m_freqScale[1] - m_freqScale[0];
	}
	
	public void calcPitch() throws PitchDetectorException {		
		short[] shortAudioBuf;
		
		readSample();
		shortAudioBuf = getAudioBuf();
		
		for (int i = 0; i < m_samples; ++i) {
			m_audioBuf[i] = shortAudioBuf[i];
		}
		
		fft(audioBuf(), m_fft, m_samplesPadded);
		
		System.arraycopy(m_hspData[0], 0, m_fft, 0, m_samplesPadded);
		
		for (int i = 1; i < HARMONICS; ++i) {
			int r = (int)Math.floor(((double)m_fftLen) / ((double)i));
			for (int j = 0; j < r - 1; ++j) {
				for (int k = 0; k < i - 1; ++i) {
					m_hspData[i][j] += m_hspData[0][(j - 1) * i + k];
				}
			}
		}
		
		System.arraycopy(m_hsp, 0, m_hspData[0], 0, m_fftLen);
		for (int i = 1; i < HARMONICS; ++i) {
			for (int j = 0; j < m_samplesPadded; ++j) {
				m_hsp[j] *= m_hspData[i][j];
			}
		}
		
		int maxIdx = 0;
		for (int i = 1; i < m_samplesPadded; ++i) {
			if (m_hsp[i] > m_hsp[maxIdx]) {
				maxIdx = i;
			}
		}
		
		System.out.println(m_freqScale[maxIdx]);
		
		setPitch(m_freqScale[maxIdx]);
	}
	
	void fft(double[] buf) {
		m_fft = m_fftw.oneDimensionalForward(buf);
	}
	
}
