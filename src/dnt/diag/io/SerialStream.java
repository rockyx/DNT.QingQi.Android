package dnt.diag.io;

import java.io.IOException;

public interface SerialStream {
	int read(byte[] buffer, int offset, int count) throws IOException;

	void write(byte[] buffer, int offset, int count) throws IOException;

	void setAttributes(int baudRate, int parity, int dataBits, int stopBits,
			int handshake) throws IOException;

	void discardInBuffer() throws IOException;

	void discardOutBuffer() throws IOException;

	int getSignals() throws IOException;

	void setSignal(int signal, boolean value) throws Exception;

	void setBreakState(boolean value) throws IOException;

	void close();

	int getBytesToRead() throws IOException;

	int getBytesToWrite() throws IOException;

	int getReadTimeout();

	void setReadTimeout(int value);

	int getWriteTimeout();

	void setWriteTimeout(int value);
}
