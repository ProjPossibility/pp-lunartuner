package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.net.*;
import pitchDetector.*;
import misc.*;
import misc.InstrumentInfo.*;

public class LunarTunerGui extends javax.swing.JFrame {
	static private LunarTunerGui m_instance = new LunarTunerGui();
	static private ImageIcon m_icon = null;

	// For the interval notifier
	private boolean m_intervalEnabled = false;
	private long m_intervalLength;
	private long m_intervalLastNotification;
	private Robot m_robot = null;
	
	private LunarTunerGui() {
		initComponents();
		loadInstruments();
		loadInstrumentNotes();
		
		try {
			m_robot = new Robot();
		}
		catch (AWTException e) {
			ErrorDialog.show(e);
		}
		
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
		System.out.println((now - m_instance.m_intervalLastNotification) + ">" + m_instance.m_intervalLength);
		if (m_instance.isFocused() 
				&& now - m_instance.m_intervalLastNotification > m_instance.m_intervalLength 
				&& m_instance.m_intervalEnabled) {
			m_instance.m_txtNoteHeard.requestFocus();
			m_instance.m_txtNoteHeard.setSelectionStart(0);
			m_instance.m_txtNoteHeard.setSelectionEnd(m_instance.m_txtNoteHeard.getText().length());
			
			m_instance.m_robot.keyPress(KeyEvent.VK_CAPS_LOCK);
			m_instance.m_robot.keyPress(KeyEvent.VK_W);
			
			m_instance.m_robot.keyRelease(KeyEvent.VK_W);
			m_instance.m_robot.keyRelease(KeyEvent.VK_CAPS_LOCK);	
			
			m_instance.m_chkNotify.requestFocus();
			
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
		if (m_instance.isFocused() 
				&& now - m_instance.m_intervalLastNotification > m_instance.m_intervalLength 
				&& m_instance.m_intervalEnabled) {			
			m_instance.m_txtInstructions.requestFocus();
			m_instance.m_txtInstructions.setSelectionStart(0);
			m_instance.m_txtInstructions.setSelectionEnd(m_instance.m_txtInstructions.getText().length());
			
			m_instance.m_robot.keyPress(KeyEvent.VK_CAPS_LOCK);
			m_instance.m_robot.keyPress(KeyEvent.VK_W);
			
			m_instance.m_robot.keyRelease(KeyEvent.VK_W);
			m_instance.m_robot.keyRelease(KeyEvent.VK_CAPS_LOCK);	
			
			m_instance.m_chkNotify.requestFocus();
			
			m_instance.m_intervalLastNotification = now;
		}
	}
	
	private void throwMessageBox(String message) {
		if(m_notifyWindow != null) {
			m_notifyWindow.setVisible(false);
		}
		m_jop.setOptions(new Object[] {"Stop"});
		m_jop.setMessage(null);
		m_jop.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("value")) {
					setIntervalNotifyEnabled(false);
					m_chkNotify.setSelected(false);
					m_notifyWindow = null;
				}
			}
		});
		m_notifyWindow = m_jop.createDialog(null, message);
		m_notifyWindow.setModal(false);
		m_notifyWindow.setVisible(true);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        m_jop = new javax.swing.JOptionPane();
        m_notifyWindow = new javax.swing.JDialog();
        m_dlgAbout = new javax.swing.JDialog();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        m_dlgHelp = new javax.swing.JDialog();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        m_panelMeter = new javax.swing.JPanel();
        m_labelNoteHeard = new javax.swing.JLabel();
        m_txtNoteHeard = new javax.swing.JTextField();
        m_labelInstructions = new javax.swing.JLabel();
        m_txtInstructions = new javax.swing.JTextField();
        m_panelStatus = new javax.swing.JPanel();
        m_lblMeter = new javax.swing.JLabel();
        m_panelInstrument = new javax.swing.JPanel();
        m_labelInstrumentType = new javax.swing.JLabel();
        m_cbInstrumentType = new javax.swing.JComboBox();
        m_labelInstrumentNote = new javax.swing.JLabel();
        m_cbInstrumentNote = new javax.swing.JComboBox();
        m_btnPlayNote = new javax.swing.JButton();
        m_panelNotifySettings = new javax.swing.JPanel();
        m_chkNotify = new javax.swing.JCheckBox();
        m_labelNotifyInterval = new javax.swing.JLabel();
        m_cbNotifyInterval = new javax.swing.JComboBox();
        m_menuBar = new javax.swing.JMenuBar();
        m_menuFile = new javax.swing.JMenu();
        m_menuItemAbout = new javax.swing.JMenuItem();
        m_menuItemHelp = new javax.swing.JMenuItem();
        m_menuItemExit = new javax.swing.JMenuItem();

        org.jdesktop.layout.GroupLayout m_notifyWindowLayout = new org.jdesktop.layout.GroupLayout(m_notifyWindow.getContentPane());
        m_notifyWindow.getContentPane().setLayout(m_notifyWindowLayout);
        m_notifyWindowLayout.setHorizontalGroup(
            m_notifyWindowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        m_notifyWindowLayout.setVerticalGroup(
            m_notifyWindowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
        m_dlgAbout.setTitle("About Lunar Tuner");
        m_dlgAbout.setAlwaysOnTop(true);
        m_dlgAbout.setBackground(java.awt.Color.white);
        m_dlgAbout.setForeground(java.awt.Color.white);
        m_dlgAbout.setLocationByPlatform(true);
        m_dlgAbout.setModal(true);
        m_dlgAbout.setResizable(false);
        m_dlgAbout.getAccessibleContext().setAccessibleName("");
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/logo.jpg")));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Brought to you by Project Possibility");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("http://www.projectpossibility.org");

        jLabel10.setFont(new java.awt.Font("Lucida Grande", 1, 13));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Developers");

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Marc Allen <marc.allen@projectpossibility.org>");

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Chris Leung <christopher.leung@projectpossibility.org>");

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Please contact us with suggestions, features, and new accessible software project ideas!");

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("To get involved or learn more about Project Possibility, please visit http://projectpossibility.org");

        jTextField1.setEditable(false);
        jTextField1.setFont(new java.awt.Font("Tahoma", 1, 24));
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("LunarTuner v0.1 Beta");
        jTextField1.setBorder(null);
        jTextField1.getAccessibleContext().setAccessibleName("Lunar Tuner Version 0.1\nBrought to you by Project:Possibility\nwww.projectpossibility.org\nDevelopers\nMarc Allen <marc.allen@projectpossibility.org>\nChris Leung <christopher.leung@projectpossibility.org>\nPlease contact us with suggestions, features, and new accessible software project ideas!\nTo get involved or learn more about Project:Possibility, please visit www.projectpossibility.org");

        org.jdesktop.layout.GroupLayout m_dlgAboutLayout = new org.jdesktop.layout.GroupLayout(m_dlgAbout.getContentPane());
        m_dlgAbout.getContentPane().setLayout(m_dlgAboutLayout);
        m_dlgAboutLayout.setHorizontalGroup(
            m_dlgAboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(jLabel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, m_dlgAboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 623, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, m_dlgAboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                .addContainerGap())
        );
        m_dlgAboutLayout.setVerticalGroup(
            m_dlgAboutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_dlgAboutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(13, 13, 13)
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
        jLabel15.setFont(new java.awt.Font("Lucida Grande", 1, 24));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("LunarTuner Help");

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("LunarTuner is an accessible instrument tuning software, designed to be usable by the blind community.\n\nThere are two modes of operation: \"Automatic\" mode and \"Targeted  Note\" mode.  In Automatic mode, the tuner will find the closest note to  the pitch it detects and give determine the percent error based on that  note.  In Targeted Note mode, the user will select an instrument using  the \"Type\" dropdown menu and a note using the \"Note\" dropdown menu.   The tuner will then determine the percent error based on the target note,  and will use the \"Instructions\" field to tell the user whether to tune up or down.\n\nIn both modes, the \"Note Heard\" field will contain text regarding the  current note and error percentage.\n\nLunarTuner can also play a note on demand. If the user presses the \"Play Note\" button, the currently selected note will be played through the sound system.\n\nIf the user has a screen reader like Jaws and wants to be updated periodically with the current note, they can click the \"Enable Notify\" checkbox and set the \"Notify Interval in Seconds\" field to the desired update rate.  A dialog box containing the current note information will  be opened and closed at that interval to trigger to screen reader.  To stop the notifier, press the stop button in the dialog box or click  the \"Enable Notify\" checkbox again.\n\nLunarTuner requires Java 1.5+ and has been tested on Windows 2000/XP and Mac OS/X.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);
        jTextArea1.getAccessibleContext().setAccessibleName(jTextArea1.getText());

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
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Lunar Tuner Version 0.1 Beta");
        setResizable(false);
        m_panelMeter.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Status")));
        m_panelMeter.setName("Status");
        m_labelNoteHeard.setText("Note Heard");

        m_txtNoteHeard.setEditable(false);
        m_txtNoteHeard.getAccessibleContext().setAccessibleName("Note Heard");

        m_labelInstructions.setText("Instructions");

        m_txtInstructions.setEditable(false);
        m_txtInstructions.getAccessibleContext().setAccessibleName("Instructions");

        org.jdesktop.layout.GroupLayout m_panelMeterLayout = new org.jdesktop.layout.GroupLayout(m_panelMeter);
        m_panelMeter.setLayout(m_panelMeterLayout);
        m_panelMeterLayout.setHorizontalGroup(
            m_panelMeterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelMeterLayout.createSequentialGroup()
                .add(m_panelMeterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_labelInstructions)
                    .add(m_labelNoteHeard))
                .addContainerGap(187, Short.MAX_VALUE))
            .add(m_txtNoteHeard, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
            .add(m_txtInstructions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
        );
        m_panelMeterLayout.setVerticalGroup(
            m_panelMeterLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelMeterLayout.createSequentialGroup()
                .add(m_labelNoteHeard)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_txtNoteHeard, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_labelInstructions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_txtInstructions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        m_panelStatus.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        m_lblMeter.setForeground(new java.awt.Color(255, 0, 0));
        m_lblMeter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/meter.gif")));

        org.jdesktop.layout.GroupLayout m_panelStatusLayout = new org.jdesktop.layout.GroupLayout(m_panelStatus);
        m_panelStatus.setLayout(m_panelStatusLayout);
        m_panelStatusLayout.setHorizontalGroup(
            m_panelStatusLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_lblMeter)
        );
        m_panelStatusLayout.setVerticalGroup(
            m_panelStatusLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_lblMeter)
        );

        m_panelInstrument.setBorder(javax.swing.BorderFactory.createTitledBorder("Instrument"));
        m_labelInstrumentType.setText("Type");

        m_cbInstrumentType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cbInstrumentTypeActionPerformed(evt);
            }
        });

        m_cbInstrumentType.getAccessibleContext().setAccessibleName("Instrument Type");

        m_labelInstrumentNote.setText("Note");

        m_cbInstrumentNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cbInstrumentNoteActionPerformed(evt);
            }
        });

        m_cbInstrumentNote.getAccessibleContext().setAccessibleName("Instrument Note");

        m_btnPlayNote.setText("Play Note");
        m_btnPlayNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_btnPlayNoteActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout m_panelInstrumentLayout = new org.jdesktop.layout.GroupLayout(m_panelInstrument);
        m_panelInstrument.setLayout(m_panelInstrumentLayout);
        m_panelInstrumentLayout.setHorizontalGroup(
            m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelInstrumentLayout.createSequentialGroup()
                .add(m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_labelInstrumentType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_labelInstrumentNote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(m_cbInstrumentType, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(m_cbInstrumentNote, 0, 217, Short.MAX_VALUE)))
            .add(m_btnPlayNote)
        );
        m_panelInstrumentLayout.setVerticalGroup(
            m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelInstrumentLayout.createSequentialGroup()
                .add(m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelInstrumentType)
                    .add(m_cbInstrumentType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_panelInstrumentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelInstrumentNote)
                    .add(m_cbInstrumentNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_btnPlayNote))
        );

        m_panelNotifySettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Notify Settings"));
        m_chkNotify.setText("Enable Notify");
        m_chkNotify.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        m_chkNotify.setMargin(new java.awt.Insets(0, 0, 0, 0));
        m_chkNotify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_chkNotifyActionPerformed(evt);
            }
        });

        m_labelNotifyInterval.setText("Notify Interval in seconds");

        m_cbNotifyInterval.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        m_cbNotifyInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_cbNotifyIntervalActionPerformed(evt);
            }
        });

        m_cbNotifyInterval.getAccessibleContext().setAccessibleName("Notify Interval in Seconds");

        org.jdesktop.layout.GroupLayout m_panelNotifySettingsLayout = new org.jdesktop.layout.GroupLayout(m_panelNotifySettings);
        m_panelNotifySettings.setLayout(m_panelNotifySettingsLayout);
        m_panelNotifySettingsLayout.setHorizontalGroup(
            m_panelNotifySettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelNotifySettingsLayout.createSequentialGroup()
                .add(m_panelNotifySettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_chkNotify)
                    .add(m_panelNotifySettingsLayout.createSequentialGroup()
                        .add(m_labelNotifyInterval)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(m_cbNotifyInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 57, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap(68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        m_panelNotifySettingsLayout.setVerticalGroup(
            m_panelNotifySettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelNotifySettingsLayout.createSequentialGroup()
                .add(m_chkNotify)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_panelNotifySettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelNotifyInterval)
                    .add(m_cbNotifyInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        m_menuFile.setMnemonic('F');
        m_menuFile.setText("File");
        m_menuItemAbout.setText("About");
        m_menuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_menuItemAboutActionPerformed(evt);
            }
        });

        m_menuFile.add(m_menuItemAbout);

        m_menuItemHelp.setText("Help");
        m_menuItemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_menuItemHelpActionPerformed(evt);
            }
        });

        m_menuFile.add(m_menuItemHelp);

        m_menuItemExit.setText("Exit");
        m_menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_menuItemExitActionPerformed(evt);
            }
        });

        m_menuFile.add(m_menuItemExit);

        m_menuBar.add(m_menuFile);
        m_menuFile.getAccessibleContext().setAccessibleName("File Menu");

        setJMenuBar(m_menuBar);
        m_menuBar.getAccessibleContext().setAccessibleName("Menu Bar");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(m_panelStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(m_panelMeter, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(m_panelNotifySettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(m_panelInstrument, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(m_panelStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(layout.createSequentialGroup()
                .add(m_panelMeter, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_panelInstrument, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_panelNotifySettings, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void m_cbNotifyIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_cbNotifyIntervalActionPerformed
        setIntervalNotifyLength(Long.parseLong((String)m_cbNotifyInterval.getSelectedItem()) * 1000);
    }//GEN-LAST:event_m_cbNotifyIntervalActionPerformed

	private void m_chkNotifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_chkNotifyActionPerformed
		setIntervalNotifyEnabled(m_chkNotify.isSelected());
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
		} else {
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
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton m_btnPlayNote;
    private javax.swing.JComboBox m_cbInstrumentNote;
    private javax.swing.JComboBox m_cbInstrumentType;
    private javax.swing.JComboBox m_cbNotifyInterval;
    private javax.swing.JCheckBox m_chkNotify;
    private javax.swing.JDialog m_dlgAbout;
    private javax.swing.JDialog m_dlgHelp;
    private javax.swing.JOptionPane m_jop;
    private javax.swing.JLabel m_labelInstructions;
    private javax.swing.JLabel m_labelInstrumentNote;
    private javax.swing.JLabel m_labelInstrumentType;
    private javax.swing.JLabel m_labelNoteHeard;
    private javax.swing.JLabel m_labelNotifyInterval;
    private javax.swing.JLabel m_lblMeter;
    private javax.swing.JMenuBar m_menuBar;
    private javax.swing.JMenu m_menuFile;
    private javax.swing.JMenuItem m_menuItemAbout;
    private javax.swing.JMenuItem m_menuItemExit;
    private javax.swing.JMenuItem m_menuItemHelp;
    private javax.swing.JDialog m_notifyWindow;
    private javax.swing.JPanel m_panelInstrument;
    private javax.swing.JPanel m_panelMeter;
    private javax.swing.JPanel m_panelNotifySettings;
    private javax.swing.JPanel m_panelStatus;
    private javax.swing.JTextField m_txtInstructions;
    private javax.swing.JTextField m_txtNoteHeard;
    // End of variables declaration//GEN-END:variables
	
}
