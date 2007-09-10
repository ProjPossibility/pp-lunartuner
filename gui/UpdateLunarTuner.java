package gui;

import java.io.*;
import java.util.*;

import pitchDetector.*;
import soundDevice.*;
import soundDevice.SoundDevice.*;
import misc.*;

public class UpdateLunarTuner {
	private static UpdateLunarTuner m_instance = new UpdateLunarTuner();
	
	private double m_currentError = 0;

	private PitchAnalyzer m_pitchAnalyzer = null;
	private SoundDevice m_soundDevice = null;
	private PitchDetector m_pitchDetector = null;
	
	// For the interval notifier
	boolean m_intervalEnabled = false;
	long m_intervalLength = 10000;
	long m_intervalLastNotification = 0;

	private UpdateLunarTuner() {
		try {
			Log.getInstance().setOutputStream(new FileOutputStream(new File("log.txt"), true));
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e, "Could not open log file");
			System.exit(1);
		}
		
		System.out.println(TunerConf.getInstance());

		m_pitchAnalyzer = new PitchAnalyzer();

		try {

			// Define sampling properties
			SoundInfo si = new SoundInfo(44100.0f, 16, 1, true, false, 4096);
			// Initialize sound device
			m_soundDevice = new JavaSESound(si);
			// Initialize pitch detection engine
	
			if (TunerConf.getInstance().getString("tuner_algorithm").equals("hsp")) {
				m_pitchDetector = new HspDetector(m_soundDevice);
			}
			else
			if (TunerConf.getInstance().getString("tuner_algorithm").equals("nsdf")) {
				m_pitchDetector = new NsdfDetector(m_soundDevice);
			}
			else {
				ErrorDialog.show("Invalid tuner algorithm specified in config file.  Try either hsp or nsdf.");
				System.exit(1);
			}
		} catch (Exception e) {
			Log.getInstance().logError(getClass(), "Unknown error", e);
			System.exit(1);
		}
	}
	
	public void updateLoop() {
		// Setup stuff for analysis
		
		String noteHeard;
		String noteError;
		String noteInstructions;

		double rawSample;
		PitchSample sample;
		
		try {

			while(true) {
				// Read a sample - the pitch detector is subscribed and will
				// get a copy
		
				m_soundDevice.readSample();
				m_pitchDetector.calcPitch(); // Do the calculation
				rawSample = m_pitchDetector.getPitch(); // Retrieve the results
				sample = m_pitchAnalyzer.analyze(rawSample); // Analyze sample
		
				if (sample.getPitchFreq() > 0) {
					// Update meter
					m_currentError = sample.getPitchErrorNormalized();
					LunarTunerGui.setPitchError(m_currentError);
		
					// Update note heard and error text
					noteHeard = sample.getExplicitPitchName();
					noteError = Math.round(m_currentError) + "% error";
					LunarTunerGui.setNoteHeard(noteHeard + " with "
							+ noteError);
		
					// Update visible instructions
					if (m_pitchAnalyzer.currentTuneNoteIsSet()) {
						if (m_currentError > 10.0) {
							noteInstructions = "Tune Down";
						} else if (m_currentError < -10.0) {
							noteInstructions = "Tune Up";
						} else {
							noteInstructions = "Tuned!";
						}
						LunarTunerGui.setInstructions(noteInstructions);
						updateInterval(noteHeard, noteError,
								noteInstructions);
					} else {
						updateIntervalTimer(noteHeard, noteError);
					}
				}
			}
		}
		catch (Exception e) {
			Log.getInstance().logError(getClass(), "Unknown error", e);
			System.exit(1);
		}
	}

	static public void changeTuneNote (PitchSample tuneNote) {
		m_instance.m_pitchAnalyzer.setCurrentTuneNote(tuneNote);
		m_instance.resetIntervalTimer();
	}

	static public void playNote (PitchSample note) {
		double noteToPlay = m_instance.m_pitchAnalyzer.getPitchFreq(note.getPitchNum());
		byte[] buf;
		try {
			buf = m_instance.m_soundDevice.createTone(noteToPlay, 3);
			m_instance.m_soundDevice.writeSample(buf);
		} catch (SoundDeviceException e) {
			ErrorDialog.show(e);
		}
		
	}

	/*
	 * All the code below is related to the accessible INNTERVAL messages that
	 * appear when interval notify is enabled.
	 */

	private void resetIntervalTimer() {
		m_intervalLastNotification = new Date().getTime();
	}

	public void updateIntervalTimer(String noteHeard, String noteError) {
		if (new Date().getTime() - m_intervalLastNotification > m_intervalLength
				&& m_intervalEnabled) {
			throwMessageBox("I heard " + noteHeard + " with " + noteError);
			m_intervalLastNotification = new Date().getTime();
		}
	}

	public void setIntervalNotifyLength(long interval) {
		m_intervalLength = interval;
	}

	public void setIntervalNotifyEnabled(boolean enabled) {
		m_intervalEnabled = enabled;
		m_intervalLastNotification = new Date().getTime();
	}

	public void updateInterval(String noteHeard, String noteError,
			String noteInstructions) {
		if (new Date().getTime() - m_intervalLastNotification > m_intervalLength
				&& m_intervalEnabled) {
			m_intervalLastNotification = new Date().getTime();
			throwMessageBox(noteError + ". " + noteInstructions);
		}
	}

	public void throwMessageBox(String message) {
		// Show message box
		// Need the "stop" button on the message box to disable notify checkbox
		// And call setIntervalNotifyEnabled(false);
	}
	
	static public UpdateLunarTuner getInstance() {
	   return m_instance;
	}
	
}
