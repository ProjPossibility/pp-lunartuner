package misc;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class BigPipedInputStream extends PipedInputStream {
	protected static int PIPE_SIZE = 32768;
	
	public BigPipedInputStream() {
		super();
		buffer = new byte[PIPE_SIZE];
	}

	public BigPipedInputStream(PipedOutputStream pos) throws IOException {
		super(pos);
	}
}
