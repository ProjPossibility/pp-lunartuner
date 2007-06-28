package accessibility;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/*
 * 
 *	Uses an SWT shell to spawn a dialog box that notifies the user. 
 * 
 * 	Notification includes a customized message, dependent on number of
 *  arguments passed to the update method.
 * 
 * 	Uses a "timer" to keep track of the last time the user was notified.
 * 
 * 	Can be disabled by calling setEnabled with a false boolean.
 * 
 */

public class AccessibleNotifier {
	
	long m_interval = 10000;
	long m_lastNotification = 0;

	Shell m_shell = null;
	
	boolean m_enabled = false;
	
	MessageBox m_messageBox = null;
	
	public AccessibleNotifier(Shell shell) {
		m_shell = shell;
	}
	
	public void resetTimer() {
		m_lastNotification = new Date().getTime();
	}
	
	public void update(String noteHeard, String noteError) {
		if (new Date().getTime()-m_lastNotification > m_interval && m_enabled) {
			String explicitNoteHeard;
			
			
			if (noteHeard.length() > 1) {
				if(noteHeard.charAt(1) == 'b') {
					explicitNoteHeard = noteHeard.charAt(0) + " flat";
				}
				else{
					explicitNoteHeard = noteHeard.charAt(0) + " sharp";
				}
			}
			else {
				explicitNoteHeard = noteHeard;
			}
			m_messageBox = new MessageBox(m_shell,SWT.OK);
			m_messageBox.setMessage("I heard " + explicitNoteHeard + " with " + noteError + " error.");
			m_messageBox.open();
			m_lastNotification = new Date().getTime();
		}
	}

	public void update(String noteHeard, String noteError, String noteInstructions) {
		if (new Date().getTime()-m_lastNotification > m_interval && m_enabled) {
			m_messageBox = new MessageBox(m_shell,SWT.OK);
			m_messageBox.setMessage(noteError + " error. " + noteInstructions);
			m_messageBox.open();
			m_lastNotification = new Date().getTime();
		}
	}
	
	public void setInterval(long interval) {
		m_interval = interval;
	}
	
	public void setEnabled(boolean enabled) {
		m_enabled = enabled;
		m_lastNotification = new Date().getTime();
	}
}
