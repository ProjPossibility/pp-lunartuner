import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import pitchDetector.PitchAnalyzer;
import pitchDetector.PitchCollection;
import pitchDetector.PitchDetector;
import pitchDetector.PitchSample;
import soundDevice.JavaSESound;
import soundDevice.SoundDevice;
import soundDevice.SoundInfo;

public class Tuner {

	private Display m_display = null;
	private Canvas m_canvasMeter = null;
	private Combo m_comboInstrument = null;
	private Combo m_comboNote = null;
	private Label m_labelNoteHeard = null;
	private Label m_labelNoteError = null;
	private Label m_labelNoteInstructions = null;
	
	private double m_currentError = 0;
	private PitchCollection m_currentPitchCollection = null;
	
	private PitchAnalyzer m_pitchAnalyzer = null;
	
	private Vector<PitchCollection> m_instrumentList = new Vector<PitchCollection>();
	
	public static void main(String[] args) {
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
		guitar.addPitch(new PitchSample("1E",64));
		guitar.addPitch(new PitchSample("2B",59));
		guitar.addPitch(new PitchSample("3G",55));
		guitar.addPitch(new PitchSample("4D",50));
		guitar.addPitch(new PitchSample("5A",45));
		guitar.addPitch(new PitchSample("6E",40));

		// Setup pitch collection for guitar
		PitchCollection cello = new PitchCollection();
		cello.setName("Cello");
		cello.addPitch(new PitchSample("1E",64));
		cello.addPitch(new PitchSample("2B",59));
		cello.addPitch(new PitchSample("3G",55));
		cello.addPitch(new PitchSample("4D",50));

		// Add guitar, cello to instruments
		m_instrumentList.add(guitar);
		m_instrumentList.add(cello);
		
		// Setup GUI
		m_display = new Display();
		Shell shell = new Shell(m_display);
		setupDisplay(shell);
		
		try {
			// Define sampling properties
			SoundInfo si = new SoundInfo(44100.0f, 16, 1, true, false, 4096);
			// Initialize sound device
			SoundDevice sd = new JavaSESound(si);
			// Initialize pitch detection engine
			PitchDetector pd = new PitchDetector(sd);
			
			while (!shell.isDisposed()) {
				if (!m_display.readAndDispatch()) {
					// Read a sample - the pitch detector is subscribed and will get a copy
					sd.readSample();
					pd.calcPitch(); // Do the calculation
					rawSample = pd.getPitch(); // Retrieve the results
					sample = m_pitchAnalyzer.analyze(rawSample); // Analyze the note
					
					if (sample.getPitchFreq() > 0) {
						// Update m_canvasMeter
						m_currentError = sample.getPitchErrorNormalized();
						m_canvasMeter.redraw();

						// Update note heard text
						m_labelNoteHeard.setText(sample.getPitchName());
						m_labelNoteHeard.update();
						
						// Update current error text
						m_labelNoteError.setText(Math.round(m_currentError) + "%");
						m_labelNoteError.update();
						
						// Update instructions
						if (m_pitchAnalyzer.currentTuneNoteIsSet()) {
							if(m_currentError > 10.0) {
								m_labelNoteInstructions.setText("Tune Down");
							}
							else if(m_currentError < -10.0) {
								m_labelNoteInstructions.setText("Tune Up");
							}
							else {
								m_labelNoteInstructions.setText("Tuned!");
							}
							m_labelNoteInstructions.update();
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		m_display.dispose();
	}

	private void setupDisplay(Shell shell) {

		shell.setText("LunarTuner v0.1a");
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite mainWindow = new Composite(shell, SWT.NONE);
		mainWindow.setLayout(new RowLayout(SWT.VERTICAL));
		Composite m_meterAreaComposite = new Composite(mainWindow, SWT.NONE);
		m_meterAreaComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		// Create the canvas to draw the line on
		m_canvasMeter = new Canvas(m_meterAreaComposite, SWT.NONE);
		m_canvasMeter.setLayoutData(new RowData(80, 300));
		m_canvasMeter.addPaintListener(new MeterUpdateListener());
		
		InputStream is = Tuner.class.getResourceAsStream("resource/meter.gif");
		m_canvasMeter.setBackgroundImage(new Image(m_display, is));
		
		Composite textAreaComposite = new Composite(m_meterAreaComposite,
				SWT.NONE);
		RowLayout textAreaLayout = new RowLayout(SWT.VERTICAL);
		textAreaLayout.spacing = 8;
		textAreaComposite.setLayout(textAreaLayout);
		textAreaComposite.setLayoutData(new RowData(150, 400));
		
		
		// Create the input fields
		
		new Label(textAreaComposite, SWT.NONE).setText("Select Instrument:");
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

		new Label(textAreaComposite, SWT.NONE).setText("Select Note:");
		m_comboNote = new Combo(textAreaComposite, SWT.READ_ONLY);
		m_comboNote.addSelectionListener(new NoteTuneChangeListener());
		m_comboNote.setEnabled(false);
		
		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		new Label(textAreaComposite, SWT.NONE).setText("Note Heard:");
		m_labelNoteHeard = new Label(textAreaComposite, SWT.NONE);
		m_labelNoteHeard.setText("None");
		FontData fontData = new FontData();
		fontData.setName("Courier New");
		fontData.setStyle(SWT.BOLD);
		fontData.setHeight(15);
		Font font = new Font(m_display, fontData);
		m_labelNoteHeard.setFont(font);

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);

		new Label(textAreaComposite, SWT.NONE).setText("Note Error:");
		m_labelNoteError = new Label(textAreaComposite, SWT.NONE);
		m_labelNoteError.setText("                ");
		m_labelNoteError.setFont(font);

		new Label(textAreaComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		
		new Label(textAreaComposite, SWT.NONE).setText("Instructions:");
		m_labelNoteInstructions = new Label(textAreaComposite, SWT.NONE);
		m_labelNoteInstructions.setText("                ");
		m_labelNoteInstructions.setFont(font);

		shell.pack();
		shell.open();
	}
	
	private class MeterUpdateListener implements PaintListener {
		public void paintControl(PaintEvent e) {
			int y_pos;
			
			if(m_currentError > 75.0) {
				y_pos = 1;
			}
			else if (m_currentError < -75.0){
				y_pos = 297;
			}
			else {
				y_pos = (m_canvasMeter.getBounds().height / 2) - (int) (m_currentError * m_canvasMeter.getBounds().height / 150);
			}

			e.gc.setLineWidth(3);
			e.gc.setForeground(new Color(m_display, 255, 0, 0));
			e.gc.drawLine(0, y_pos, m_canvasMeter.getBounds().width, y_pos);
		}
	}
	
	private class InstrumentChangeListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Instrument Selected: " + m_comboInstrument.getText());
		}

		public void widgetSelected(SelectionEvent e) {
			if(m_comboInstrument.getText().equals("Automatic")) {
				// Clear everything out of the notes 
				m_comboNote.setEnabled(false);
				m_comboNote.removeAll();
				m_comboNote.update();

				// Update
				m_currentPitchCollection = null;
				m_pitchAnalyzer.resetCurrentTuneNote();
				m_labelNoteInstructions.setText("                ");
				m_labelNoteInstructions.update();
			}
			else {
				// Look for the instrument selected
				Iterator instruments = m_instrumentList.iterator();
				PitchCollection selectedInstrument = null;
				while(selectedInstrument == null && instruments.hasNext()) {
					PitchCollection currentInstrument = (PitchCollection)instruments.next(); 
					if (currentInstrument.getName().equals(m_comboInstrument.getText())) {
						selectedInstrument = currentInstrument;
					}
				}
				// Add all of the selected instrument's notes to the combo box
				Iterator notes = selectedInstrument.getPitches();
				while(notes.hasNext()) {
					m_comboNote.add(((PitchSample)notes.next()).getPitchName());
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
			System.out.println("Default Note Selected: " + m_comboNote.getText());
		}

		public void widgetSelected(SelectionEvent e) {
			// Look for the PitchSample
			Iterator pitches = m_currentPitchCollection.getPitches();
			PitchSample currentPitch = null;
			
			while(pitches.hasNext()) {
				currentPitch = (PitchSample)pitches.next(); 
				if(currentPitch.getPitchName().equals(m_comboNote.getText())) {
					m_pitchAnalyzer.setCurrentTuneNote(currentPitch);
				}
			}
		}
	}
}
