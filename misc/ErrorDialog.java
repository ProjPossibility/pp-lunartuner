package misc;

import org.eclipse.swt.widgets.*;

public class ErrorDialog {
	static final long serialVersionUID = 0;
	
	static private Shell m_shell = null;
	
	static public void setShell(Shell shell) {
		m_shell = shell;
	}
	
	static public void show(String s) {
		new ErrorDialog(null, s);
	}
	
	static public void show(Exception e) {
		new ErrorDialog(e);
	}
	
	static public void show(Exception e, String s) {
		new ErrorDialog(e, s);
	}
	
	public ErrorDialog(Exception e) {
		init(e, null);
	}
	
	public ErrorDialog(Exception e, String msg) {
		init(e, msg);
	}
	
	private void init(Exception e, String msg) {
		MessageBox mbox = new MessageBox(m_shell);
		
		String contents = "";
		
		if (msg != null) {
			contents += msg + "\n";
			Log.getInstance().logMessage(getClass(), msg);
		}
		
		if (e != null) {
			contents += e.getMessage() + "\n";
			Log.getInstance().logError(getClass(), e.getMessage(), e);
			
			StackTraceElement[] trace = e.getStackTrace();
			for (int i = 0; i < ((trace.length < 10) ? trace.length : 10); ++i) {
				String line = trace[i].getFileName() + ":" 
						+ trace[i].getClassName() + ":" 
						+ trace[i].getMethodName() + ":" 
						+ trace[i].getLineNumber();
				contents += line + "\n";
			}
		}
		
		mbox.setText(contents);
		mbox.open();
	}
	
}
