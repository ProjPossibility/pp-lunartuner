package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.net.*;
import pitchDetector.*;
import speech.*;
import misc.*;
import misc.InstrumentInfo.*;

public class LunarTunerGui extends javax.swing.JFrame {
	static private LunarTunerGui m_instance = new LunarTunerGui();
	static private ImageIcon m_icon = null;
	static boolean m_enableSpeech = true;
	
	// For the interval notifier
	private boolean m_intervalEnabled = false;
	private long m_intervalLength;
	private long m_intervalLastNotification;
	
	private boolean m_fileChangeCtr = false;
	private boolean m_aboutChangeCtr = false;
	private boolean m_helpChangeCtr = false;
	private boolean m_notifyChangeCtr = false;
	private boolean m_speechChangeCtr = false;
	private boolean m_exitChangeCtr = false;
	
	private LunarTunerGui() {
		initComponents();
		loadInstruments();
		loadInstrumentNotes();
		
		ImageIcon img = new ImageIcon(getClass().getResource("/resource/icon.png"));
		setIconImage(img.getImage());
		m_intervalLength = 10000;
		m_intervalLastNotification = 0;
	}
	
	static public LunarTunerGui getInstance() {
		return m_instance;
	}
	
	static public void setPitchError(double err) {
		Graphics2D g = (Graphics2D)m_instance.m_lblMeter.getGraphics();
		ImageIcon icon = (ImageIcon)m_instance.m_lblMeter.getIcon();
		
		err = -1 * err;
		
		int errPix = icon.getIconHeight() / 2 + (int)err;
		if (errPix > icon.getIconHeight()) {
			errPix = icon.getIconHeight() - 1;
		}
		else
		if (errPix < 0) {
			errPix = 0;
		}
		
		g.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), m_instance);
		g.setPaint(Color.RED);
		g.fillRect(0, errPix, icon.getIconWidth(), 3);
	}
	
	static public void setNoteHeard(String txt) {
		m_instance.m_txtNoteHeard.setText(txt);
	}
	
	static public void setInstructions(String txt) {
		m_instance.m_txtInstructions.setText(txt);
	}
	
	private void loadInstruments() {
		InstrumentInfo.Instrument[] inst = InstrumentInfo.getInstance().getInstruments();
		m_cbInstrumentType.addItem(new Instrument());
		for (int i = 0; i < inst.length; ++i) {
			m_cbInstrumentType.addItem(inst[i]);
		}
		
		m_cbInstrumentType.setSelectedIndex(0);
	}
	
	private void loadInstrumentNotes() {
		Instrument inst = (InstrumentInfo.Instrument)m_cbInstrumentType.getSelectedItem();
		InstrumentNote[] notes = inst.getNotes();
		m_cbInstrumentNote.removeAllItems();
		for (int i = 0; i < notes.length; ++i) {
			m_cbInstrumentNote.addItem(notes[i]);
		}
		
		if (notes.length > 0) {
			m_cbInstrumentNote.setSelectedIndex(0);
		}
	}
	
	private void resetIntervalTimer() {
		m_intervalLastNotification = new Date().getTime();
	}
	
   public static void updateIntervalTimer(String noteHeard, String noteError) {
		long now = new Date().getTime();
		if (now - m_instance.m_intervalLastNotification > m_instance.m_intervalLength 
				&& m_instance.m_intervalEnabled) {
			if (m_enableSpeech) {
				Speech.speak(noteHeard + " " + noteError);
			}
			m_instance.m_intervalLastNotification = now;
		}
	}
	
	private void setIntervalNotifyLength(long interval) {
		m_intervalLength = interval;
	}
	
	private void setIntervalNotifyEnabled(boolean enabled) {
		m_intervalEnabled = enabled;
		m_intervalLastNotification = new Date().getTime();
	}
	
	public static void updateInterval(String noteHeard, String noteError, String noteInstructions) {
		long now = new Date().getTime();
		if (now - m_instance.m_intervalLastNotification > m_instance.m_intervalLength 
				&& m_instance.m_intervalEnabled) {			
			if (m_enableSpeech) {
				Speech.speak(noteInstructions);
			}
			m_instance.m_intervalLastNotification = now;
		}
	}
	
	public static void updateSpeechEnable() {
		m_enableSpeech = m_instance.m_chkEnableSpeech.isSelected();
		speakChkEnableSpeech();
	}
	
	public static void updateNotifyInterval() {
		m_instance.setIntervalNotifyLength(Long.parseLong((String)m_instance.m_cbNotifyInterval.getSelectedItem()) * 1000);
		m_instance.setIntervalNotifyEnabled(m_instance.m_chkNotify.isSelected());
		speakChkNotify();
	}
	
	public static void speakCbNotifyInterval() {
		if (!m_enableSpeech) return;
		if (m_instance.m_cbNotifyInterval.getSelectedItem() != null) {
			Speech.speak("Notify Interval: " + m_instance.m_cbNotifyInterval.getSelectedItem().toString() + " seconds");
		}
		else {
			Speech.speak("Notify Interval: none selected");
		}
	}

	public static void speakChkNotify() {
		if (!m_enableSpeech) return;
		Speech.speak("Notify checkbox" + ((m_instance.m_chkNotify.isSelected() ? " checked" : " not checked")));
	}
	
	public static void speakBtnPlay() {
		if (!m_enableSpeech) return;
		Speech.speak("Play Note Button");
	}
	
	public static void speakCbInstrumentNote() {
		if (!m_enableSpeech) return;
		if (m_instance.m_cbInstrumentNote.getSelectedItem() != null) {
			Speech.speak("Note: " + m_instance.m_cbInstrumentNote.getSelectedItem().toString());
		}
		else {
			Speech.speak("Note: none selected");
		}
	}
	
	public static void speakCbInstrumentType() {
		if (!m_enableSpeech) return;
		if (m_instance.m_cbInstrumentType.getSelectedItem() != null) {
			Speech.speak("Instrument: " + m_instance.m_cbInstrumentType.getSelectedItem().toString());
		}
		else {
			Speech.speak("Instrument: none selected");
		}
	}
	
	public static void speakTxtInstructions(boolean sayTitle) {
		if (!m_enableSpeech) return;
		
		String msg = m_instance.m_txtInstructions.getText();
		if (sayTitle) {
			msg = "Instructions: " + msg;
		}
		Speech.speak(msg);
	}
	
	public static void speakTxtNoteHeard(boolean sayTitle) {
		if (!m_enableSpeech) return;
		
		String msg = m_instance.m_txtNoteHeard.getText();
		if (sayTitle) {
			msg = "Note Heard: " + msg;
		}
		Speech.speak(msg);
	}
	
	public static void speakChkEnableSpeech() {
		if (!m_enableSpeech) return;
		Speech.speak("Enable speech checkbox" + ((m_instance.m_chkEnableSpeech.isSelected() ? " checked" : " not checked")));
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
   // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
   private void initComponents() {
      m_dlgAbout = new javax.swing.JDialog();
      jLabel6 = new javax.swing.JLabel();
      jLabel8 = new javax.swing.JLabel();
      jLabel9 = new javax.swing.JLabel();
      jLabel10 = new javax.swing.JLabel();
      jLabel11 = new javax.swing.JLabel();
      jLabel12 = new javax.swing.JLabel();
      jLabel13 = new javax.swing.JLabel();
      jLabel14 = new javax.swing.JLabel();
      m_txtAbout = new javax.swing.JTextField();
      m_dlgHelp = new javax.swing.JDialog();
      jLabel15 = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      m_txtHelp = new javax.swing.JTextArea();
      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      m_txtNoteHeard = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      m_txtInstructions = new javax.swing.JTextField();
      jPanel2 = new javax.swing.JPanel();
      m_lblMeter = new javax.swing.JLabel();
      jPanel3 = new javax.swing.JPanel();
      jLabel3 = new javax.swing.JLabel();
      m_cbInstrumentType = new javax.swing.JComboBox();
      jLabel4 = new javax.swing.JLabel();
      m_cbInstrumentNote = new javax.swing.JComboBox();
      m_btnPlayNote = new javax.swing.JButton();
      jPanel4 = new javax.swing.JPanel();
      m_chkNotify = new javax.swing.JCheckBox();
      jLabel5 = new javax.swing.JLabel();
      m_cbNotifyInterval = new javax.swing.JComboBox();
      m_chkEnableSpeech = new javax.swing.JCheckBox();
      m_menuBar = new javax.swing.JMenuBar();
      m_menuFile = new javax.swing.JMenu();
      m_menuItemAbout = new javax.swing.JMenuItem();
      m_menuItemHelp = new javax.swing.JMenuItem();
      m_menuItemEnableNotify = new javax.swing.JCheckBoxMenuItem();
      m_menuItemEnableSpeech = new javax.swing.JCheckBoxMenuItem();
      m_menuItemExit = new javax.swing.JMenuItem();

      m_dlgAbout.setLocationByPlatform(true);
      m_dlgAbout.setModal(true);
      m_dlgAbout.setResizable(false);
      m_dlgAbout.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosed(java.awt.event.WindowEvent evt) {
            m_dlgAboutWindowClosed(evt);
         }
         public void windowActivated(java.awt.event.WindowEvent evt) {
            m_dlgAboutWindowActivated(evt);
         }
      });

      jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/logo.jpg")));

      jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel8.setText("Brought to you by Project Possibility");
      jLabel8.getAccessibleContext().setAccessibleDescription("");

      jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel9.setText("http://www.projectpossibility.org");

      jLabel10.setFont(new java.awt.Font("Lucida Grande", 1, 13));
      jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel10.setText("Developers");

      jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel11.setText("Marc Allen <allen.marc@gmail.com>");

      jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel12.setText("Chris Leung <christopher.leung@projectpossibility.org>");

      jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel13.setText("Please contact us with suggestions, features, and new accessible software project ideas!");

      jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel14.setText("To get involved or learn more about Project Possibility, please visit http://projectpossibility.org");

      m_txtAbout.setBackground(null);
      m_txtAbout.setEditable(false);
      m_txtAbout.setFont(new java.awt.Font("Lucida Grande", 0, 24));
      m_txtAbout.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      m_txtAbout.setText("LunarTuner v0.1");
      m_txtAbout.setBorder(null);
      m_txtAbout.getAccessibleContext().setAccessibleName("LunarTuner version 0 point 1.\nBrought to you by Project Possibility.\nhttp colon slash slash w w w  dot project possibility dot o r g.\nPlease contact us with suggestions, features, and new accessible software project ideas!\nTo get involved or learn more about Project Possibility, please visit http colon slash slash w w w  dot project possibility dot o r g.");
      m_txtAbout.getAccessibleContext().setAccessibleDescription("LunarTuner version 0 point 1.\nBrought to you by Project Possibility.\nhttp colon slash slash w w w  dot project possibility dot o r g.\nPlease contact us with suggestions, features, and new accessible software project ideas!\nTo get involved or learn more about Project Possibility, please visit http colon slash slash w w w  dot project possibility dot o r g.");

      org.jdesktop.layout.GroupLayout m_dlgAboutLayout = new org.jdesktop.layout.GroupLayout(m_dlgAbout.getContentPane());
      m_dlgAbout.getContentPane().setLayout(m_dlgAboutLayout);
      m_dlgAboutLayout.setHorizontalGroup(
         m_dlgAboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(jLabel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, m_dlgAboutLayout.createSequentialGroup()
            .addContainerGap()
            .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE))
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
         .add(m_dlgAboutLayout.createSequentialGroup()
            .add(193, 193, 193)
            .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(202, 202, 202))
         .add(m_txtAbout, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
      );
      m_dlgAboutLayout.setVerticalGroup(
         m_dlgAboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(m_dlgAboutLayout.createSequentialGroup()
            .add(m_txtAbout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel10)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel11)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel12)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel13)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel14)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
      );
      m_dlgHelp.setLocationByPlatform(true);
      m_dlgHelp.setModal(true);
      m_dlgHelp.setResizable(false);
      m_dlgHelp.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosed(java.awt.event.WindowEvent evt) {
            m_dlgHelpWindowClosed(evt);
         }
         public void windowActivated(java.awt.event.WindowEvent evt) {
            m_dlgHelpWindowActivated(evt);
         }
      });

      jLabel15.setFont(new java.awt.Font("Lucida Grande", 1, 24));
      jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      jLabel15.setText("LunarTuner Help");

      m_txtHelp.setColumns(20);
      m_txtHelp.setLineWrap(true);
      m_txtHelp.setRows(5);
      m_txtHelp.setText("LunarTuner is an accessible instrument tuning software, designed to be usable by the blind community.  It is also useful as a general purpose tuner.\n\nThere are two modes of operation: \"Automatic\" mode and \"Targeted  Note\" mode.  In Automatic mode, the software will examine the current pitch and find the note closest to it.  The percentage error relative to this note is calculated.\n\nIn Targeted Note mode, the user will select an instrument using  the \"Type\" dropdown menu and a note using the \"Note\" dropdown menu.   The tuner will then determine the percent error based on the target note,  and will use the \"Instructions\" field to tell the user whether to tune up or down.\n\nIn both modes, the \"Note Heard\" field will contain text regarding the  current note and error.\n\nWhen either the \"Note Heard\" or \"Instructions\" fields are focused, pressing H will read the current value.\n\nLunarTuner can also play a note on demand. If the user presses the \"Play Note\" button, the currently selected note will be played through the sound system.\n\nThere are several options for screen reader access.  LunarTuner has a built in screen reader based on the FreeTTS software package.  This feature can be turned on and off using the \"Enable Speech\" checkbox, or by pressing Control-S.  External screenreaders such as Jaws are also usable with LunarTuner.\n\nTo be updated periodically with the status of the current note, set the \"Enable Notify\" checkbox and set the \"Notify Interval in Seconds\" field to the desired update rate.  To stop the notifier, click  the \"Enable Notify\" checkbox again, or press Control-N.\n\nLunarTuner requires Java 1.5+ and has been tested on Windows 2000/XP and Mac OS/X.");
      m_txtHelp.setWrapStyleWord(true);
      jScrollPane1.setViewportView(m_txtHelp);

      org.jdesktop.layout.GroupLayout m_dlgHelpLayout = new org.jdesktop.layout.GroupLayout(m_dlgHelp.getContentPane());
      m_dlgHelp.getContentPane().setLayout(m_dlgHelpLayout);
      m_dlgHelpLayout.setHorizontalGroup(
         m_dlgHelpLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jLabel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
         .add(m_dlgHelpLayout.createSequentialGroup()
            .add(24, 24, 24)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 568, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(20, 20, 20))
      );
      m_dlgHelpLayout.setVerticalGroup(
         m_dlgHelpLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(m_dlgHelpLayout.createSequentialGroup()
            .add(jLabel15)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
            .addContainerGap())
      );

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("LunarTuner Beta v0.1");
      setLocationByPlatform(true);
      setResizable(false);
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Status")));
      jPanel1.setName("Status");
      jLabel1.setText("Note Heard");

      m_txtNoteHeard.setEditable(false);
      m_txtNoteHeard.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_txtNoteHeardFocusGained(evt);
         }
      });
      m_txtNoteHeard.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyPressed(java.awt.event.KeyEvent evt) {
            m_txtNoteHeardKeyPressed(evt);
         }
      });

      jLabel2.setText("Instructions");

      m_txtInstructions.setEditable(false);
      m_txtInstructions.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_txtInstructionsFocusGained(evt);
         }
      });
      m_txtInstructions.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyPressed(java.awt.event.KeyEvent evt) {
            m_txtInstructionsKeyPressed(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jLabel2)
               .add(jLabel1))
            .addContainerGap(178, Short.MAX_VALUE))
         .add(m_txtNoteHeard, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
         .add(m_txtInstructions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel1Layout.createSequentialGroup()
            .add(jLabel1)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(m_txtNoteHeard, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jLabel2)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(m_txtInstructions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );
      jPanel1.getAccessibleContext().setAccessibleName("Status");

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
      m_lblMeter.setForeground(new java.awt.Color(255, 0, 0));
      m_lblMeter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/meter.gif")));

      org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(m_lblMeter)
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(m_lblMeter)
      );

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Instrument"));
      jLabel3.setText("Type");

      m_cbInstrumentType.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            m_cbInstrumentTypeItemStateChanged(evt);
         }
      });
      m_cbInstrumentType.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_cbInstrumentTypeActionPerformed(evt);
         }
      });
      m_cbInstrumentType.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_cbInstrumentTypeFocusGained(evt);
         }
      });

      jLabel4.setText("Note");

      m_cbInstrumentNote.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            m_cbInstrumentNoteItemStateChanged(evt);
         }
      });
      m_cbInstrumentNote.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_cbInstrumentNoteActionPerformed(evt);
         }
      });
      m_cbInstrumentNote.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_cbInstrumentNoteFocusGained(evt);
         }
      });

      m_btnPlayNote.setText("Play Note");
      m_btnPlayNote.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_btnPlayNoteActionPerformed(evt);
         }
      });
      m_btnPlayNote.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_btnPlayNoteFocusGained(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
               .add(m_cbInstrumentType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(m_cbInstrumentNote, 0, 217, Short.MAX_VALUE)))
         .add(m_btnPlayNote)
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel3Layout.createSequentialGroup()
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel3)
               .add(m_cbInstrumentType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel4)
               .add(m_cbInstrumentNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(m_btnPlayNote))
      );

      jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Notify Settings"));
      m_chkNotify.setText("Enable Notify");
      m_chkNotify.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      m_chkNotify.setMargin(new java.awt.Insets(0, 0, 0, 0));
      m_chkNotify.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_chkNotifyActionPerformed(evt);
         }
      });
      m_chkNotify.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_chkNotifyFocusGained(evt);
         }
      });

      jLabel5.setText("Notify Interval in seconds");

      m_cbNotifyInterval.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "3", "4", "5", "6", "7", "8", "9", "10" }));
      m_cbNotifyInterval.setSelectedIndex(2);
      m_cbNotifyInterval.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            m_cbNotifyIntervalItemStateChanged(evt);
         }
      });
      m_cbNotifyInterval.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_cbNotifyIntervalActionPerformed(evt);
         }
      });
      m_cbNotifyInterval.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_cbNotifyIntervalFocusGained(evt);
         }
      });

      m_chkEnableSpeech.setSelected(true);
      m_chkEnableSpeech.setText("Enable Speech");
      m_chkEnableSpeech.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
      m_chkEnableSpeech.setMargin(new java.awt.Insets(0, 0, 0, 0));
      m_chkEnableSpeech.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_chkEnableSpeechActionPerformed(evt);
         }
      });
      m_chkEnableSpeech.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusGained(java.awt.event.FocusEvent evt) {
            m_chkEnableSpeechFocusGained(evt);
         }
      });

      org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
      jPanel4.setLayout(jPanel4Layout);
      jPanel4Layout.setHorizontalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel4Layout.createSequentialGroup()
            .add(jLabel5)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(m_cbNotifyInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(59, Short.MAX_VALUE))
         .add(m_chkEnableSpeech, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
         .add(m_chkNotify, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
      );
      jPanel4Layout.setVerticalGroup(
         jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(jPanel4Layout.createSequentialGroup()
            .add(m_chkNotify)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
               .add(jLabel5)
               .add(m_cbNotifyInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(m_chkEnableSpeech)
            .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      m_menuFile.setMnemonic('F');
      m_menuFile.setText("File");
      m_menuFile.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuFileStateChanged(evt);
         }
      });

      m_menuItemAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
      m_menuItemAbout.setText("About");
      m_menuItemAbout.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_menuItemAboutActionPerformed(evt);
         }
      });
      m_menuItemAbout.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuItemAboutStateChanged(evt);
         }
      });

      m_menuFile.add(m_menuItemAbout);
      m_menuItemAbout.getAccessibleContext().setAccessibleDescription("About");

      m_menuItemHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
      m_menuItemHelp.setText("Help");
      m_menuItemHelp.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_menuItemHelpActionPerformed(evt);
         }
      });
      m_menuItemHelp.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuItemHelpStateChanged(evt);
         }
      });

      m_menuFile.add(m_menuItemHelp);
      m_menuItemHelp.getAccessibleContext().setAccessibleDescription("Help");

      m_menuItemEnableNotify.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
      m_menuItemEnableNotify.setText("Enable Notify");
      m_menuItemEnableNotify.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_menuItemEnableNotifyActionPerformed(evt);
         }
      });
      m_menuItemEnableNotify.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuItemEnableNotifyStateChanged(evt);
         }
      });

      m_menuFile.add(m_menuItemEnableNotify);
      m_menuItemEnableNotify.getAccessibleContext().setAccessibleDescription("Enable Notify");

      m_menuItemEnableSpeech.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
      m_menuItemEnableSpeech.setSelected(true);
      m_menuItemEnableSpeech.setText("Speech Enabled");
      m_menuItemEnableSpeech.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_menuItemEnableSpeechActionPerformed(evt);
         }
      });
      m_menuItemEnableSpeech.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuItemEnableSpeechStateChanged(evt);
         }
      });

      m_menuFile.add(m_menuItemEnableSpeech);
      m_menuItemEnableSpeech.getAccessibleContext().setAccessibleDescription("Speech Enabled");

      m_menuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
      m_menuItemExit.setText("Exit");
      m_menuItemExit.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            m_menuItemExitActionPerformed(evt);
         }
      });
      m_menuItemExit.addChangeListener(new javax.swing.event.ChangeListener() {
         public void stateChanged(javax.swing.event.ChangeEvent evt) {
            m_menuItemExitStateChanged(evt);
         }
      });

      m_menuFile.add(m_menuItemExit);
      m_menuItemExit.getAccessibleContext().setAccessibleDescription("Exit");

      m_menuBar.add(m_menuFile);
      m_menuFile.getAccessibleContext().setAccessibleDescription("File");

      setJMenuBar(m_menuBar);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createSequentialGroup()
            .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
               .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                  .add(jPanel1, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
               .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
               .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
               .add(jPanel4, 0, 106, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
      );
      pack();
   }// </editor-fold>//GEN-END:initComponents

	private void m_menuItemExitStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuItemExitStateChanged
		m_exitChangeCtr = !m_exitChangeCtr;
		if (m_exitChangeCtr) {
			Speech.speak("Exit");
		}
	}//GEN-LAST:event_m_menuItemExitStateChanged

	private void m_menuItemEnableSpeechStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuItemEnableSpeechStateChanged
		m_speechChangeCtr = !m_speechChangeCtr;
		if (m_speechChangeCtr) {
			Speech.speak("Speech: " + (m_menuItemEnableSpeech.isSelected() ? "On" : "Off"));
		}
	}//GEN-LAST:event_m_menuItemEnableSpeechStateChanged

	private void m_menuItemEnableNotifyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuItemEnableNotifyStateChanged
		m_notifyChangeCtr = !m_notifyChangeCtr;
		if (m_notifyChangeCtr) {
			Speech.speak("Notify: " + (m_menuItemEnableNotify.isSelected() ? "On" : "Off"));
		}
	}//GEN-LAST:event_m_menuItemEnableNotifyStateChanged

	private void m_menuItemAboutStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuItemAboutStateChanged
		m_aboutChangeCtr = !m_aboutChangeCtr;
		if (m_aboutChangeCtr) {
			Speech.speak("About");
		}
	}//GEN-LAST:event_m_menuItemAboutStateChanged

	private void m_menuItemHelpStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuItemHelpStateChanged
		m_helpChangeCtr = !m_helpChangeCtr;
		if (m_helpChangeCtr) {
			Speech.speak("Help");
		}
	}//GEN-LAST:event_m_menuItemHelpStateChanged

	private void m_menuFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_menuFileStateChanged
		Speech.speak("File Menu");
	}//GEN-LAST:event_m_menuFileStateChanged

	private void m_menuItemEnableNotifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_menuItemEnableNotifyActionPerformed
		m_chkNotify.setSelected(!m_chkNotify.isSelected());
		updateNotifyInterval();
	}//GEN-LAST:event_m_menuItemEnableNotifyActionPerformed

	private void m_menuItemEnableSpeechActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_menuItemEnableSpeechActionPerformed
		m_chkEnableSpeech.setSelected(!m_chkEnableSpeech.isSelected());
		updateSpeechEnable();
	}//GEN-LAST:event_m_menuItemEnableSpeechActionPerformed

	private void m_dlgHelpWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_m_dlgHelpWindowActivated
		if (m_enableSpeech) {
			Speech.speak(m_txtHelp.getText());
		}
	}//GEN-LAST:event_m_dlgHelpWindowActivated

	private void m_dlgAboutWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_m_dlgAboutWindowActivated
		if (m_enableSpeech) {
			Speech.speak(m_txtAbout.getAccessibleContext().getAccessibleDescription());
		}
	}//GEN-LAST:event_m_dlgAboutWindowActivated

	private void m_dlgAboutWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_m_dlgAboutWindowClosed
		Speech.cancel();
	}//GEN-LAST:event_m_dlgAboutWindowClosed

	private void m_dlgHelpWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_m_dlgHelpWindowClosed
		Speech.cancel();
	}//GEN-LAST:event_m_dlgHelpWindowClosed

	private void m_chkEnableSpeechFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_chkEnableSpeechFocusGained
		speakChkEnableSpeech();
	}//GEN-LAST:event_m_chkEnableSpeechFocusGained

	private void m_chkEnableSpeechActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_chkEnableSpeechActionPerformed
		updateSpeechEnable();
	}//GEN-LAST:event_m_chkEnableSpeechActionPerformed

	private void m_cbNotifyIntervalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_m_cbNotifyIntervalItemStateChanged
		speakCbNotifyInterval();
	}//GEN-LAST:event_m_cbNotifyIntervalItemStateChanged

	private void m_cbNotifyIntervalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_cbNotifyIntervalFocusGained
		speakCbNotifyInterval();
	}//GEN-LAST:event_m_cbNotifyIntervalFocusGained

	private void m_chkNotifyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_chkNotifyFocusGained
		speakChkNotify();
	}//GEN-LAST:event_m_chkNotifyFocusGained

	private void m_btnPlayNoteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_btnPlayNoteFocusGained
		speakBtnPlay();
	}//GEN-LAST:event_m_btnPlayNoteFocusGained

	private void m_cbInstrumentNoteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_m_cbInstrumentNoteItemStateChanged
		if (m_cbInstrumentNote.hasFocus()) {
			speakCbInstrumentNote();
		}
	}//GEN-LAST:event_m_cbInstrumentNoteItemStateChanged

	private void m_cbInstrumentNoteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_cbInstrumentNoteFocusGained
		speakCbInstrumentNote();
	}//GEN-LAST:event_m_cbInstrumentNoteFocusGained
	
	private void m_cbInstrumentTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_m_cbInstrumentTypeItemStateChanged
		speakCbInstrumentType();
	}//GEN-LAST:event_m_cbInstrumentTypeItemStateChanged
	
	private void m_cbInstrumentTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_cbInstrumentTypeFocusGained
		speakCbInstrumentType();
	}//GEN-LAST:event_m_cbInstrumentTypeFocusGained

	private void m_txtInstructionsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtInstructionsKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_H) {
			speakTxtInstructions(false);
		}
	}//GEN-LAST:event_m_txtInstructionsKeyPressed

	private void m_txtNoteHeardKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtNoteHeardKeyPressed
		if (evt.getKeyCode() == KeyEvent.VK_H) {
			speakTxtNoteHeard(false);			
		}
	}//GEN-LAST:event_m_txtNoteHeardKeyPressed

	private void m_txtInstructionsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_txtInstructionsFocusGained
		speakTxtInstructions(true);
	}//GEN-LAST:event_m_txtInstructionsFocusGained

	private void m_txtNoteHeardFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_txtNoteHeardFocusGained
		speakTxtNoteHeard(true);
	}//GEN-LAST:event_m_txtNoteHeardFocusGained

	private void m_cbNotifyIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cbNotifyIntervalActionPerformed
		setIntervalNotifyLength(Long.parseLong((String)m_cbNotifyInterval.getSelectedItem()) * 1000);
	}//GEN-LAST:event_m_cbNotifyIntervalActionPerformed

	private void m_chkNotifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_chkNotifyActionPerformed
		updateNotifyInterval();
	}//GEN-LAST:event_m_chkNotifyActionPerformed

	private void m_menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_menuItemExitActionPerformed
		System.exit(0);
	}//GEN-LAST:event_m_menuItemExitActionPerformed

	private void m_menuItemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_menuItemHelpActionPerformed
		m_dlgHelp.pack();
		m_dlgHelp.setVisible(true);
	}//GEN-LAST:event_m_menuItemHelpActionPerformed

	private void m_menuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_menuItemAboutActionPerformed
		m_dlgAbout.pack();
		m_dlgAbout.setVisible(true);
	}//GEN-LAST:event_m_menuItemAboutActionPerformed
	
   private void m_cbInstrumentTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cbInstrumentTypeActionPerformed
		loadInstrumentNotes();
   }//GEN-LAST:event_m_cbInstrumentTypeActionPerformed
	
   private void m_cbInstrumentNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cbInstrumentNoteActionPerformed
		String name = ((Instrument)m_cbInstrumentType.getSelectedItem()).getName();
		InstrumentNote note = (InstrumentNote)m_cbInstrumentNote.getSelectedItem();
		if (!name.equals("Automatic") && note != null) {
			UpdateLunarTuner.changeTuneNote(new PitchSample(note.getName(), note.getFreqIdx()));
		}
		else {
			UpdateLunarTuner.changeTuneNote(null);
		}
   }//GEN-LAST:event_m_cbInstrumentNoteActionPerformed
	
   private void m_btnPlayNoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_btnPlayNoteActionPerformed
		InstrumentNote note = (InstrumentNote)m_cbInstrumentNote.getSelectedItem();
		if (note != null) {
			UpdateLunarTuner.playNote(new PitchSample(note.getName(), note.getFreqIdx()));
		}
   }//GEN-LAST:event_m_btnPlayNoteActionPerformed
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				LunarTunerGui.getInstance().setVisible(true);
			}
		});
		
		Thread updateThread = new Thread() {
			public void run() {
				UpdateLunarTuner.getInstance().updateLoop();
			}
		};
		
		updateThread.start();
	}
	
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel11;
   private javax.swing.JLabel jLabel12;
   private javax.swing.JLabel jLabel13;
   private javax.swing.JLabel jLabel14;
   private javax.swing.JLabel jLabel15;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JButton m_btnPlayNote;
   private javax.swing.JComboBox m_cbInstrumentNote;
   private javax.swing.JComboBox m_cbInstrumentType;
   private javax.swing.JComboBox m_cbNotifyInterval;
   private javax.swing.JCheckBox m_chkEnableSpeech;
   private javax.swing.JCheckBox m_chkNotify;
   private javax.swing.JDialog m_dlgAbout;
   private javax.swing.JDialog m_dlgHelp;
   private javax.swing.JLabel m_lblMeter;
   private javax.swing.JMenuBar m_menuBar;
   private javax.swing.JMenu m_menuFile;
   private javax.swing.JMenuItem m_menuItemAbout;
   private javax.swing.JCheckBoxMenuItem m_menuItemEnableNotify;
   private javax.swing.JCheckBoxMenuItem m_menuItemEnableSpeech;
   private javax.swing.JMenuItem m_menuItemExit;
   private javax.swing.JMenuItem m_menuItemHelp;
   private javax.swing.JTextField m_txtAbout;
   private javax.swing.JTextArea m_txtHelp;
   private javax.swing.JTextField m_txtInstructions;
   private javax.swing.JTextField m_txtNoteHeard;
   // End of variables declaration//GEN-END:variables
	
}
