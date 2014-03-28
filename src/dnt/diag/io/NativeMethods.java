package dnt.diag.io;

public final class NativeMethods {

	protected static int error;

	protected native static String getLastError();

	protected native static boolean isBaudRateLegal(int baudRate);

	protected native static boolean setAttributes(int fd, int baudRate,
			int parity, int dataBits, int stopBits, int handshake);

	protected native static int getSignals(int fd);
	
	protected native static int setSignal(int fd, int signal, boolean value);

	protected native static int openSerial(String portName);

	protected native static int closeSerial(int fd);

	protected native static boolean pollSerial(int fd, int timeout);

	protected native static int readSerial(int fd, byte[] buffer, int offset,
			int count);

	protected native static int writeSerial(int fd, byte[] buffer, int offset,
			int count, int timeout);

	protected native static int getBytesInBuffer(int fd, boolean intput);

	protected native static int discardBuffer(int fd, boolean input);
	
	protected native static int breakprop(int fd);
}
