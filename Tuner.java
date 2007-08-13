import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
//import org.eclipse.swt.events.*;

import accessibility.AccessibleNotifier;

import pitchDetector.*;
import soundDevice.*;
import misc.*;

public class Tuner {

	private static final String METER_IMAGE = "resource/meter.gif";

	private static final int METER_DIMX = 93;

	private static final int METER_DIMY = 350;

	private Display m_display = null;

	private Canvas m_canvasMeter = null;

	private Combo m_comboNotifyInterval = null;

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

	private AccessibleNotifier m_accessibleNotifier = null;

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
		Shell shell = new Shell(m_display);
		setupDisplay(m_display, shell);
		
		ErrorDialog.setShell(shell);
		
		// Initialize accessible notifier
		m_accessibleNotifier = new AccessibleNotifier(shell);

		try {
			// Define sampling properties
			SoundInfo si = new SoundInfo(44100.0f, 16, 1, true, false, 4096);
			// Initialize sound device
			SoundDevice sd = new JavaSESound(si);
			// Initialize pitch detection engine
			
			PitchDetector pd = null;
			if (TunerConf.getInstance().getString("tuner_algorithm").equals("hsp")) {
				pd = new HspDetector(sd);
			}
			else
			if (TunerConf.getInstance().getString("tuner_algorithm").equals("nsdf")) {
				pd = new NsdfDetector(sd);
			}
			else {
				ErrorDialog.show("Invalid tuner algorithm specified in config file.  Try either hsp or nsdf.");
				System.exit(1);
			}

			// Initialize temporary variables for holding messages/values
			String noteHeard;
			String noteError;
			String noteInstructions;

			while (!shell.isDisposed()) {
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
							m_accessibleNotifier.update(noteHeard, noteError,
									noteInstructions);
						} else {
							m_accessibleNotifier.update(noteHeard, noteError);
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

		Composite mainWindow = new Composite(shell, SWT.NONE);
		mainWindow.setLayout(new RowLayout(SWT.VERTICAL));
		Composite m_meterAreaComposite = new Composite(mainWindow, SWT.NONE);
		m_meterAreaComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		// Create the canvas to draw the line on
		m_canvasMeter = new Canvas(m_meterAreaComposite, SWT.NONE);
		m_canvasMeter.setLayoutData(new RowData(METER_DIMX, METER_DIMY));
		m_canvasMeter.addPaintListener(new MeterUpdateListener(METER_DIMX,
				METER_DIMY));

		InputStream is = Tuner.class.getResourceAsStream(METER_IMAGE);
		m_canvasMeter.setBackgroundImage(new Image(m_display, is));

		Composite textAreaComposite = new Composite(m_meterAreaComposite,
				SWT.NONE);
		RowLayout textAreaLayout = new RowLayout(SWT.VERTICAL);
		textAreaLayout.spacing = 8;
		textAreaComposite.setLayout(textAreaLayout);
		textAreaComposite.setLayoutData(new RowData(200, 400));

		// Create the input fields

		new Label(textAreaComposite, SWT.NONE).setText("Notify:");
		m_buttonDisableNotify = new Button(textAreaComposite, SWT.RADIO);
		m_buttonEnableNotify = new Button(textAreaComposite, SWT.RADIO);
		m_buttonDisableNotify.setText("Disabled");
		m_buttonDisableNotify.setSelection(true);
		m_buttonEnableNotify.setText("Enabled");
		m_buttonEnableNotify.setSelection(false);
		m_buttonDisableNotify
				.addSelectionListener(new NotifyIntervalListener());
		m_buttonEnableNotify.addSelectionListener(new NotifyIntervalListener());

		// buttonEnableNotify.addSelectionListener(};

		new Label(textAreaComposite, SWT.NONE).setText("Notify Interval: ");

		// Add notification timing options
		m_comboNotifyInterval = new Combo(textAreaComposite, SWT.READ_ONLY);
		m_comboNotifyInterval
				.addSelectionListener(new NotifyIntervalListener());
		m_comboNotifyInterval.add("15");
		m_comboNotifyInterval.add("10");
		m_comboNotifyInterval.add("5");
		m_comboNotifyInterval.add("2");
		m_comboNotifyInterval.select(0);
		m_comboNotifyInterval.setEnabled(false);

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		// Add Instruments
		new Label(textAreaComposite, SWT.NONE).setText("Instrument:");
		m_comboInstrument = new Combo(textAreaComposite, SWT.READ_ONLY);
		m_comboInstrument.addSelectionListener(new InstrumentChangeListener());
		m_comboInstrument.add("Automatic");
		// For each instrument, add the instrument to the combo box
		Iterator instruments = m_instrumentList.iterator();
		while (instruments.hasNext()) {
			PitchCollection instrument = (PitchCollection) instruments.next();
			m_comboInstrument.add(instrument.getName());
		}
		m_comboInstrument.select(0);

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		// Add Select Note area
		new Label(textAreaComposite, SWT.NONE).setText("Note:");
		m_comboNote = new Combo(textAreaComposite, SWT.READ_ONLY);
		m_comboNote.addSelectionListener(new NoteTuneChangeListener());
		m_comboNote.setEnabled(false);

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		// Add Note Heard area
		new Label(textAreaComposite, SWT.NONE).setText("Note Heard:");
		m_textNoteHeard = new Text(textAreaComposite, SWT.MULTI | SWT.READ_ONLY);
		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		Font font = new Font(m_display, fontData);
		m_textNoteHeard.setFont(font);
		m_textNoteHeard.setText("None                                     ");

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		// Add Instructions area
		new Label(textAreaComposite, SWT.NONE).setText("Instructions:");
		m_textNoteInstructions = new Text(textAreaComposite, SWT.SINGLE
				| SWT.READ_ONLY);
		m_textNoteInstructions
				.setText("                                       ");
		m_textNoteInstructions.setFont(font);

		shell.pack();
		shell.open();
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
			m_accessibleNotifier.resetTimer();
		}
	}

	private class NotifyIntervalListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Notify Interval Selected: "
					+ m_comboNotifyInterval.getText());
		}

		public void widgetSelected(SelectionEvent e) {
			if (m_buttonDisableNotify.getSelection()) {
				m_accessibleNotifier.setEnabled(false);
				m_comboNotifyInterval.setEnabled(false);
			} else {
				m_comboNotifyInterval.setEnabled(true);
				m_accessibleNotifier.setEnabled(true);
				m_accessibleNotifier.setInterval(Integer
						.parseInt(m_comboNotifyInterval.getText()) * 1000);
			}
		}

	}
}
