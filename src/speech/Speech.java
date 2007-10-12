package speech;

import com.sun.speech.freetts.*;

import misc.*;

public class Speech {
	
	static private Speech m_instance = new Speech();
	
	private VoiceManager m_voiceMgr = null;
	private Voice[] m_voices = null;
	private Voice m_voice = null;
	private int m_voiceIdx;
	private String m_sentence = null;
	
	private Speech() {
		m_voiceMgr = VoiceManager.getInstance();
		m_voices = m_voiceMgr.getVoices();
		
		if (m_voices.length == 0) {
			ErrorDialog.show("No FreeTTS voices available");
			System.exit(1);
		}
		
		int lastGeneralIdx = -1;
		m_voiceIdx = -1;
		for (int i = 0; i < m_voices.length; i++) {
			System.out.println(m_voices[i].getName() + " (" + m_voices[i].getDomain() + " domain)");
			if (m_voices[i].getDomain() == "general") {
				lastGeneralIdx = i;
			}
			if (m_voices[i].getName() == "kevin16") {
				m_voiceIdx = i;
			}
		}
		
		if (m_voiceIdx < 0) {
			if (lastGeneralIdx < 0) {
				ErrorDialog.show("No usable voices found");
				System.exit(1);
			}
			else {
				m_voiceIdx = lastGeneralIdx;
			}
		}
		
		m_voice = m_voices[m_voiceIdx];
		
		System.out.println("Using voice " + m_voice.getName());
		
		if (m_voice == null) {
			ErrorDialog.show("Could not create FreeTTS voice");
		}
		
		m_voice.allocate();
		m_voice.speak("Welcome to Lunar Tuner");
	}
	
	public Voice getVoice() {
		return m_voice;
	}
	
	public void setSentence(String sentence) {
		m_sentence = sentence;
	}
	
	public String getSentence() {
		return m_sentence;
	}
	
	public static synchronized void speak(String sentence) {
		getInstance().setSentence(sentence);
		(new Thread() {
			public void run() {
				Speech.getInstance().getVoice().speak(Speech.getInstance().getSentence());
			}
		}).start();
	}
	
	static public Speech getInstance() {
		return m_instance;
	}
}
