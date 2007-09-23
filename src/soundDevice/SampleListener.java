package soundDevice;

import java.io.InputStream;

public interface SampleListener {
	public void setAudioStream(InputStream is);
	public InputStream getAudioStream();
}
