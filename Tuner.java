import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import pitchDetector.*;
import soundDevice.*;
import misc.*;

public class Tuner {

	private static final String METER_IMAGE = "resource/meter.gif";

	private static final int METER_DIMX = 93;

	private static final int METER_DIMY = 350;

	private Display m_display = null;
	private Shell m_shell = null;
	private Canvas m_canvasMeter = null;
	private Spinner m_spinnerNotifyInterval = null;
	private Combo m_comboInstrument = null;

	private Combo m_comboNote = null;

	private Text m_textNoteHeard = null;

	private Text m_textNoteInstructions = null;

	private Button m_buttonEnableNotify = null;

	private Button m_buttonDisableNotify = null;

	private double m_currentError = 0;

	private PitchCollection m_currentPitchCollection = null;

	private PitchAnalyzer m_pitchAnalyzer = null;

	private Vector<PitchCollection> m_instrumentList = new Vector<PitchCollection>();

	// For the interval notifier
	boolean m_intervalEnabled = false;
	long m_intervalLength = 10000;
	long m_intervalLastNotification = 0;
	Shell m_notifyShell = null;

	public static void main(String[] args) {
		try {
			Log.getInstance().setOutputStream(
					new FileOutputStream(new File("log.txt"), true));
		} catch (FileNotFoundException e) {
			ErrorDialog.show(e, "Could not open log file");
			System.exit(1);
		}

		try {
			TunerConf.getInstance().loadConfigFile("config.xml");
		} catch (IOException e) {
			ErrorDialog.show(e, "Could not load config file");
			System.exit(1);
		}

		new Tuner().run();
	}

	public void run() {

		// Setup stuff for analysis
		double rawSample;
		PitchSample sample;
		m_pitchAnalyzer = new PitchAnalyzer();

		// Setup pitch collection for guitar
		PitchCollection guitar = new PitchCollection();
		guitar.setName("Guitar");
		guitar.addPitch(new PitchSample("1E", 64));
		guitar.addPitch(new PitchSample("2B", 59));
		guitar.addPitch(new PitchSample("3G", 55));
		guitar.addPitch(new PitchSample("4D", 50));
		guitar.addPitch(new PitchSample("5A", 45));
		guitar.addPitch(new PitchSample("6E", 40));

		// Setup pitch collection for cello
		PitchCollection cello = new PitchCollection();
		cello.setName("Cello");
		cello.addPitch(new PitchSample("1A", 57));
		cello.addPitch(new PitchSample("2D", 50));
		cello.addPitch(new PitchSample("3G", 43));
		cello.addPitch(new PitchSample("4C", 36));

		// Add guitar, cello to instruments
		m_instrumentList.add(guitar);
		m_instrumentList.add(cello);

		// Setup GUI
		m_display = new Display();
		m_shell = new Shell(m_display);
		setupDisplay(m_display, m_shell);

		ErrorDialog.setShell(m_shell);

		try {
			// Define sampling properties
			SoundInfo si = new SoundInfo(44100.0f, 16, 1, true, false, 4096);
			// Initialize sound device
			SoundDevice sd = new JavaSESound(si);
			// Initialize pitch detection engine

			PitchDetector pd = null;
			if (TunerConf.getInstance().getString("tuner_algorithm").equals(
					"hsp")) {
				pd = new HspDetector(sd);
			} else if (TunerConf.getInstance().getString("tuner_algorithm")
					.equals("nsdf")) {
				pd = new NsdfDetector(sd);
			} else {
				ErrorDialog
						.show("Invalid tuner algorithm specified in config file.  Try either hsp or nsdf.");
				System.exit(1);
			}

			// Initialize temporary variables for holding messages/values
			String noteHeard;
			String noteError;
			String noteInstructions;

			while (!m_shell.isDisposed()) {
				if (!m_display.readAndDispatch()) {
					// Read a sample - the pitch detector is subscribed and will
					// get a copy
					sd.readSample();
					pd.calcPitch(); // Do the calculation
					rawSample = pd.getPitch(); // Retrieve the results
					sample = m_pitchAnalyzer.analyze(rawSample); // Analyze
					// the note

					if (sample.getPitchFreq() > 0) {
						// Update m_canvasMeter
						m_currentError = sample.getPitchErrorNormalized();
						m_canvasMeter.redraw();

						// Update note heard and error text
						noteHeard = sample.getExplicitPitchName();
						noteError = Math.round(m_currentError) + "% error";
						m_textNoteHeard.setText(noteHeard + " with "
								+ noteError);
						m_textNoteHeard.update();

						// Update visible instructions
						if (m_pitchAnalyzer.currentTuneNoteIsSet()) {
							if (m_currentError > 10.0) {
								noteInstructions = "Tune Down";
							} else if (m_currentError < -10.0) {
								noteInstructions = "Tune Up";
							} else {
								noteInstructions = "Tuned!";
							}
							m_textNoteInstructions.setText(noteInstructions);
							m_textNoteInstructions.update();
							updateInterval(noteHeard, noteError,
									noteInstructions);
						} else {
							updateIntervalTimer(noteHeard, noteError);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.getInstance().logError(getClass(), "Unknown error", e);
			System.exit(1);
		}

		m_display.dispose();
	}

	private void setupDisplay(Display display, Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				System.exit(0);
			}
		});

		display.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e) {
				System.exit(0);
			}
		});

		shell.setText("LunarTuner v0.1");
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		// ------ File, help menu test ------
		Menu menuBar, fileMenu, helpMenu;
		MenuItem fileMenuHeader, helpMenuHeader;
		MenuItem fileExitItem, fileSaveItem, helpHelpItem, helpAboutItem;

		menuBar = new Menu(shell, SWT.BAR);
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText("&File");

		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);

		fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText("E&xit");

		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText("&Help");

		helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);

		helpHelpItem = new MenuItem(helpMenu, SWT.PUSH);
		helpHelpItem.setText("&Help");

		helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
		helpAboutItem.setText("&About");

		fileExitItem.addSelectionListener(new fileExitItemListener());
		helpHelpItem.addSelectionListener(new helpHelpItemListener());
		helpAboutItem.addSelectionListener(new helpAboutItemListener());

		shell.setMenuBar(menuBar);

		// ------- End File, help menu test ----------

		Composite mainWindow = new Composite(shell, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 2;
		mainWindow.setLayout(mainLayout);
		Composite m_meterAreaComposite = new Composite(mainWindow, SWT.NONE);

		// Generic 1-column layout
		GridLayout oneColumnGridLayout = new GridLayout();
		oneColumnGridLayout.numColumns = 1;
		oneColumnGridLayout.verticalSpacing = 10;

		// Create the canvas to draw the line on
		GridData canvasLayoutData = new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING
						| GridData.VERTICAL_ALIGN_BEGINNING);
		canvasLayoutData.heightHint = METER_DIMY + 10;
		canvasLayoutData.widthHint = METER_DIMX + 10;

		GridData canvasLayoutData2 = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		canvasLayoutData2.minimumHeight = METER_DIMY;
		canvasLayoutData2.minimumWidth = METER_DIMX;

		m_meterAreaComposite.setLayout(oneColumnGridLayout);
		m_meterAreaComposite.setLayoutData(canvasLayoutData);
		m_canvasMeter = new Canvas(m_meterAreaComposite, SWT.NONE);
		m_canvasMeter.setLayoutData(canvasLayoutData2);
		m_canvasMeter.addPaintListener(new MeterUpdateListener(METER_DIMX,
				METER_DIMY));

		InputStream is = Tuner.class.getResourceAsStream(METER_IMAGE);
		m_canvasMeter.setBackgroundImage(new Image(m_display, is));

		// Create the form area to put all the form items into
		Composite formAreaComposite = new Composite(mainWindow, SWT.NONE);
		formAreaComposite.setLayout(oneColumnGridLayout);
		GridData formAreaLayoutData = new GridData(GridData.FILL_BOTH);
		formAreaLayoutData.minimumWidth = 150;
		formAreaLayoutData.minimumHeight = METER_DIMY;
		formAreaComposite.setLayoutData(formAreaLayoutData);

		// Create generic grid layout
		GridLayout twoColumnGridLayout = new GridLayout();
		twoColumnGridLayout.numColumns = 2;
		twoColumnGridLayout.horizontalSpacing = 8;
		twoColumnGridLayout.verticalSpacing = 8;
		twoColumnGridLayout.marginHeight = 10;
		twoColumnGridLayout.marginWidth = 8;

		// Create status area
		Group m_statusGroup = new Group(formAreaComposite, SWT.NONE);
		m_statusGroup.setText("Status");
		m_statusGroup.setLayout(oneColumnGridLayout);
		m_statusGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create heard area
		new Label(m_statusGroup, SWT.NONE).setText("Note Heard:");
		m_textNoteHeard = new Text(m_statusGroup, SWT.SINGLE | SWT.READ_ONLY);
		// m_textNoteHeard.setLayoutData(new RowData(180,20));
		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		Font font = new Font(m_display, fontData);
		m_textNoteHeard.setFont(font);
		m_textNoteHeard.setText("None");
		m_textNoteHeard.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Add Instructions area
		new Label(m_statusGroup, SWT.NONE).setText("Instructions:");
		m_textNoteInstructions = new Text(m_statusGroup, SWT.SINGLE
				| SWT.READ_ONLY);
		// m_textNoteInstructions.setLayoutData(new RowData(180,20));
		m_textNoteInstructions.setText("None");
		m_textNoteInstructions.setFont(font);
		m_textNoteInstructions.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		// Create instrument group
		Group m_instrumentGroup = new Group(formAreaComposite, SWT.NONE);
		m_instrumentGroup.setText("Instrument");
		m_instrumentGroup.setLayout(twoColumnGridLayout);
		m_instrumentGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create name label
		new Label(m_instrumentGroup, SWT.NONE).setText("Name:");

		// Create instrument combo box
		m_comboInstrument = new Combo(m_instrumentGroup, SWT.READ_ONLY);
		m_comboInstrument.addSelectionListener(new InstrumentChangeListener());
		m_comboInstrument.add("Automatic");
		m_comboInstrument.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// For each instrument, add the instrument to the combo box
		Iterator instruments = m_instrumentList.iterator();
		while (instruments.hasNext()) {
			PitchCollection instrument = (PitchCollection) instruments.next();
			m_comboInstrument.add(instrument.getName());
		}
		m_comboInstrument.select(0);

		// Create note label
		new Label(m_instrumentGroup, SWT.NONE).setText("Note:");

		// Create note combo box
		m_comboNote = new Combo(m_instrumentGroup, SWT.READ_ONLY);
		m_comboNote.addSelectionListener(new NoteTuneChangeListener());
		m_comboNote.setEnabled(false);
		m_comboNote.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the notify group
		Group m_notifyGroup = new Group(formAreaComposite, SWT.NONE);
		m_notifyGroup.setText("Notify Settings");
		m_notifyGroup.setLayout(twoColumnGridLayout);
		m_notifyGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create generic grid data for horizontal span=2
		GridData gridDataColSpan2 = new GridData();
		gridDataColSpan2.horizontalSpan = 2;

		// Create notify radio buttons
		m_buttonDisableNotify = new Button(m_notifyGroup, SWT.RADIO);
		m_buttonDisableNotify.setLayoutData(gridDataColSpan2);
		m_buttonDisableNotify.setText("Disable Interval Notify");
		m_buttonDisableNotify.setSelection(true);
		m_buttonDisableNotify
				.addSelectionListener(new NotifyIntervalListener());

		// Create generic grid data for horizontal span=2
		gridDataColSpan2 = new GridData();
		gridDataColSpan2.horizontalSpan = 2;

		m_buttonEnableNotify = new Button(m_notifyGroup, SWT.RADIO);
		m_buttonEnableNotify.setLayoutData(gridDataColSpan2);
		m_buttonEnableNotify.setText("Enable Inverval Notify");
		m_buttonEnableNotify.setSelection(false);
		m_buttonEnableNotify.addSelectionListener(new NotifyIntervalListener());

		// Create notify interval label
		new Label(m_notifyGroup, SWT.NONE).setText("Interval in Seconds: ");

		// Create notify interval spinner box
		m_spinnerNotifyInterval = new Spinner(m_notifyGroup, SWT.NONE);
		m_spinnerNotifyInterval
				.addSelectionListener(new NotifyIntervalListener());
		m_spinnerNotifyInterval.setValues(15, 2, 15, 0, 1, 5);
		m_spinnerNotifyInterval.setEnabled(false);

		shell.pack();
		shell.open();

	}

	private class fileExitItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			m_shell.close();
			m_display.dispose();
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			m_shell.close();
			m_display.dispose();
		}
	}

	private class helpHelpItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			// label.setText("No worries!");
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			// label.setText("No worries!");
		}
	}

	private class helpAboutItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			// label.setText("No worries!");
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			// label.setText("No worries!");
		}
	}

	private class MeterUpdateListener implements PaintListener {
		int m_xDim, m_yDim, m_lineWidth;

		public MeterUpdateListener(int xDim, int yDim) {
			m_xDim = xDim;
			m_yDim = yDim;
			m_lineWidth = Math.round(yDim / 100);
		}

		public void paintControl(PaintEvent e) {
			int y_pos;

			if (m_currentError > 75.0) {
				y_pos = 1;
			} else if (m_currentError < -75.0) {
				y_pos = m_yDim - m_lineWidth;
			} else {
				y_pos = (m_canvasMeter.getBounds().height / 2)
						- (int) (m_currentError
								* m_canvasMeter.getBounds().height / (m_yDim / 2));
			}

			e.gc.setLineWidth(m_lineWidth);
			e.gc.setForeground(new Color(m_display, 255, 0, 0));
			e.gc.drawLine(0, y_pos, m_canvasMeter.getBounds().width, y_pos);
		}
	}

	private class InstrumentChangeListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Instrument Selected: "
					+ m_comboInstrument.getText());
		}

		public void widgetSelected(SelectionEvent e) {
			if (m_comboInstrument.getText().equals("Automatic")) {
				// Clear everything out of the notes
				m_comboNote.setEnabled(false);
				m_comboNote.removeAll();
				m_comboNote.update();

				// Update
				m_currentPitchCollection = null;
				m_pitchAnalyzer.resetCurrentTuneNote();
				m_textNoteInstructions.setText("                ");
				m_textNoteInstructions.update();
			} else {
				// Look for the instrument selected
				Iterator instruments = m_instrumentList.iterator();
				PitchCollection selectedInstrument = null;
				while (selectedInstrument == null && instruments.hasNext()) {
					PitchCollection currentInstrument = (PitchCollection) instruments
							.next();
					if (currentInstrument.getName().equals(
							m_comboInstrument.getText())) {
						selectedInstrument = currentInstrument;
					}
				}
				// Remove all notes already in the note combo box
				m_comboNote.removeAll();

				// Add all of the selected instrument's notes to the combo box
				Iterator notes = selectedInstrument.getPitches();
				while (notes.hasNext()) {
					m_comboNote
							.add(((PitchSample) notes.next()).getPitchName());
				}
				m_currentPitchCollection = selectedInstrument;

				m_comboNote.setEnabled(true);
				m_comboNote.select(0);
				m_comboNote.update();

				new NoteTuneChangeListener().widgetSelected(null);
			}
		}
	}

	private class NoteTuneChangeListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Note Selected: "
					+ m_comboNote.getText());
		}

		public void widgetSelected(SelectionEvent e) {
			// Look for the PitchSample
			Iterator pitches = m_currentPitchCollection.getPitches();
			PitchSample currentPitch = null;

			while (pitches.hasNext()) {
				currentPitch = (PitchSample) pitches.next();
				if (currentPitch.getPitchName().equals(m_comboNote.getText())) {
					m_pitchAnalyzer.setCurrentTuneNote(currentPitch);
				}
			}
			resetIntervalTimer();
		}
	}

	private class NotifyIntervalListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Notify Interval Selected: "
					+ m_spinnerNotifyInterval.getSelection());
		}

		public void widgetSelected(SelectionEvent e) {
			if (m_buttonDisableNotify.getSelection()) {
				setEnabled(false);
				m_spinnerNotifyInterval.setEnabled(false);
			} else {
				m_spinnerNotifyInterval.setEnabled(true);
				setEnabled(true);
				setInterval(m_spinnerNotifyInterval.getSelection() * 1000);
			}
		}

	}

	/*
	 * All the code below is related to the accessible INNTERVAL messages that
	 * appear when interval notify is enabled.
	 * 
	 * As of current revision (24) the accessibility package has been migrated
	 * into Tuner.java
	 */

	public void resetIntervalTimer() {
		m_intervalLastNotification = new Date().getTime();
	}

	public void updateIntervalTimer(String noteHeard, String noteError) {
		if (new Date().getTime() - m_intervalLastNotification > m_intervalLength
				&& m_intervalEnabled) {
			throwMessageBox("I heard " + noteHeard + " with " + noteError);
			m_intervalLastNotification = new Date().getTime();
		}
	}

	public void setInterval(long interval) {
		m_intervalLength = interval;
	}

	public void setEnabled(boolean enabled) {
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

		if (m_notifyShell != null) {
			m_notifyShell.close();
			m_notifyShell = null;
		}
		m_notifyShell = new Shell(m_display);
		m_notifyShell.setLayout(new RowLayout());
		Button stop = new Button(m_notifyShell, SWT.PUSH);
		stop.setLayoutData(new RowData(600, 100));
		stop.setText("Stop");
		stop.addSelectionListener(new IntervalStopSelectionListener());

		m_notifyShell.setText(message);
		m_notifyShell.pack();
		m_notifyShell.open();
	}

	private class IntervalStopSelectionListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing
		}

		public void widgetSelected(SelectionEvent e) {
			m_notifyShell.close();
			m_notifyShell = null;
			setEnabled(false);
			m_buttonEnableNotify.setSelection(false);
			m_buttonDisableNotify.setSelection(true);
			m_spinnerNotifyInterval.setEnabled(false);
			m_buttonDisableNotify.setFocus();
		}

	}
}
