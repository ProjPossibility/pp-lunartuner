package misc;

public class Fftw {
	static {
	    try {
	    	System.loadLibrary("fftw");
		}
	    catch (UnsatisfiedLinkError e) {
	    	throw new UnsatisfiedLinkError("Fail to load fftw library : " + e.getMessage());
		}
	}
	
	native static public void fft(double[] in, double[] out);
}
