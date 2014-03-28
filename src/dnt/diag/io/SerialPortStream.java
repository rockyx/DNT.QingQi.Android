package dnt.diag.io;

import java.io.IOException;

import dnt.diag.Settings;
import dnt.diag.Timer;

public class SerialPortStream implements SerialStream {
	int fd;
	int readTimeout;
	int writeTimeout;

	public SerialPortStream(String portName, int baudRate, int dataBits,
			int parity, int stopBits, boolean dtrEnable, boolean rtsEnable,
			int handshake, int readTimeout, int writeTimeout,
			int readBufferSize, int writeBufferSize) throws Exception {
		fd = NativeMethods.openSerial(portName);
		if (fd == -1)
			throwIOException();

		tryBaudRate(baudRate);

		if (!NativeMethods.setAttributes(fd, baudRate, parity, dataBits,
				stopBits, handshake))
			throwIOException(); // Probably Win32Exc for compatibility

		this.readTimeout = readTimeout;
		this.writeTimeout = writeTimeout;

		setSignal(SerialPort.SIGNAL_DTR, dtrEnable);

		if (handshake != SerialPort.HANDSHAKE_REQUEST_TO_SEND
				&& handshake != SerialPort.HANDSHAKE_REQUEST_TO_SEND_XON_XOFF)
			setSignal(SerialPort.SIGNAL_RTS, rtsEnable);
	}

	@Override
	public int getReadTimeout() {
		return readTimeout;
	}

	@Override
	public void setReadTimeout(int value) {
		if (value < 0 && value != SerialPort.INFINITE_TIMEOUT)
			throw new IllegalArgumentException("Read timeout value");
		readTimeout = value;
	}

	@Override
	public int getWriteTimeout() {
		return writeTimeout;
	}

	@Override
	public void setWriteTimeout(int value) {
		if (value < 0 && value != SerialPort.INFINITE_TIMEOUT)
			throw new IllegalArgumentException("Write timeout value");
		writeTimeout = value;
	}

	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		if (buffer == null)
			throw new NullPointerException("buffer");
		if (offset < 0 || count < 0)
			throw new IllegalArgumentException(
					"offset or count less than zero.");

		if (buffer.length - offset < count)
			throw new IllegalArgumentException(
					"the size of the buffer is less than offset + count.");

		if (Settings.device == Settings.TOMIC_DEVICE_V1) {
			long expire = Timer.fromMilliseconds(readTimeout).toNanoseconds();

			long start = System.nanoTime();

			long elapsed = System.nanoTime() - start;

			int len = 0;

			while (elapsed < expire) {
				int size = getBytesToRead();
				if (size >= count) {
					size = NativeMethods.readSerial(fd, buffer, offset, count);
					if (size == -1)
						throwIOException();
					len += size;
					return len;
				}

				if (size != 0) {
					size = NativeMethods.readSerial(fd, buffer, offset, size);
					if (size == -1)
						throwIOException();
					len += size;
					count -= size;
					offset += size;
				}

				elapsed = System.nanoTime() - start;
			}

			return len;
		} else {
			boolean pollResult = NativeMethods.pollSerial(fd, readTimeout);
			if (NativeMethods.error == -1)
				throwIOException();

			if (!pollResult) {
				throw new IOException("Timeout");
			}

			int result = NativeMethods.readSerial(fd, buffer, offset, count);
			if (result == -1)
				throwIOException();

			return result;
		}
	}

	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		if (buffer == null)
			throw new NullPointerException("buffer");

		if (offset < 0 || count < 0)
			throw new IllegalArgumentException();

		if (buffer.length - offset < count)
			throw new IllegalArgumentException(
					"The size of the buffer is less than offset + count.");

		if (NativeMethods.writeSerial(fd, buffer, offset, count, writeTimeout) < 0)
			throw new IOException("Timeout");
	}

	@Override
	public void close() {
		NativeMethods.closeSerial(fd);
	}

	public void setAttributes(int baudRate, int parity, int dataBits,
			int stopBits, int handshake) throws IOException {
		if (!NativeMethods.setAttributes(fd, baudRate, parity, dataBits,
				stopBits, handshake))
			throwIOException();
	}

	@Override
	public int getBytesToRead() throws IOException {
		int result = NativeMethods.getBytesInBuffer(fd, true);
		if (result == -1)
			throwIOException();
		return result;
	}

	@Override
	public int getBytesToWrite() throws IOException {
		int result = NativeMethods.getBytesInBuffer(fd, false);
		if (result == -1)
			throwIOException();
		return result;
	}

	@Override
	public void discardInBuffer() throws IOException {
		if (NativeMethods.discardBuffer(fd, true) != 0)
			throwIOException();
	}

	@Override
	public void discardOutBuffer() throws IOException {
		if (NativeMethods.discardBuffer(fd, false) != 0)
			throwIOException();
	}

	@Override
	public void setSignal(int signal, boolean value) throws Exception {
		if (signal < SerialPort.SIGNAL_CD || signal > SerialPort.SIGNAL_RTS
				|| signal == SerialPort.SIGNAL_CD
				|| signal == SerialPort.SIGNAL_CTS
				|| signal == SerialPort.SIGNAL_DSR) {
			throw new Exception("Invalid internval value");
		}

		if (NativeMethods.setSignal(fd, signal, value) == -1)
			throwIOException();
	}

	@Override
	public int getSignals() throws IOException {
		int signals = NativeMethods.getSignals(fd);
		if (NativeMethods.error == -1)
			throwIOException();

		return signals;
	}

	@Override
	public void setBreakState(boolean value) throws IOException {
		if (value)
			if (NativeMethods.breakprop(fd) == -1)
				throwIOException();
	}

	private static void throwIOException() throws IOException {
		throw new IOException(NativeMethods.getLastError());
	}

	private void tryBaudRate(int baudRate) {
		if (!NativeMethods.isBaudRateLegal(baudRate)) {
			throw new IllegalArgumentException(
					"Given baud rate is not supported on this platform.");
		}
	}
}
