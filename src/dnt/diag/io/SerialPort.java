package dnt.diag.io;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dnt.diag.Timer;
import dnt.qingqi.BuildConfig;

public final class SerialPort {
	static {
		System.loadLibrary("dntdiag");
	}

	public final static int HANDSHAKE_NONE = 0;
	public final static int HANDSHAKE_XON_XOFF = 1;
	public final static int HANDSHAKE_REQUEST_TO_SEND = 2;
	public final static int HANDSHAKE_REQUEST_TO_SEND_XON_XOFF = 3;

	public final static int PARITY_NONE = 0;
	public final static int PARITY_ODD = 1;
	public final static int PARITY_EVEN = 2;
	public final static int PARITY_MARK = 3;
	public final static int PARITY_SPACE = 4;

	public final static int STOPBITS_NONE = 0;
	public final static int STOPBITS_ONE = 1;
	public final static int STOPBITS_TWO = 2;
	public final static int STOPBITS_ONE_POINT_FIVE = 3;

	public final static int SIGNAL_NONE = 0;
	public final static int SIGNAL_CD = 1; // Carrier detect
	public final static int SIGNAL_CTS = 2; // Clear to send
	public final static int SIGNAL_DSR = 4; // Data set ready
	public final static int SIGNAL_DTR = 8; // Data terminal ready
	public final static int SIGNAL_RTS = 16; // Request to send

	public final static int INFINITE_TIMEOUT = -1;

	private final static int DEFAULT_READ_BUFFER_SIZE = 4096;
	private final static int DEFAULT_WRITE_BUFFER_SIZE = 2048;
	private final static int DEFAULT_BAUD_RATE = 9600;
	private final static int DEFAULT_DATA_BITS = 8;
	private final static int DEFAULT_PARITY = PARITY_NONE;
	private final static int DEFAULT_STOP_BITS = STOPBITS_ONE;

	private boolean isOpen;
	private int baudRate;
	private int parity;
	private int stopBits;
	private int handshake;
	private int dataBits;
	private boolean breakState = false;
	private boolean dtrEnable = false;
	private boolean rtsEnable = false;
	private SerialPortStream stream;
	private String portName;
	private int readTimeout = INFINITE_TIMEOUT;
	private int writeTimeout = INFINITE_TIMEOUT;
	private int readBufferSize = DEFAULT_READ_BUFFER_SIZE;
	private int writeBufferSize = DEFAULT_WRITE_BUFFER_SIZE;

	public SerialPort() {
		this(getDefaultPortName(), DEFAULT_BAUD_RATE, DEFAULT_PARITY,
				DEFAULT_DATA_BITS, DEFAULT_STOP_BITS);
	}

	public SerialPort(String portName) {
		this(portName, DEFAULT_BAUD_RATE, DEFAULT_PARITY, DEFAULT_DATA_BITS,
				DEFAULT_STOP_BITS);
	}

	public SerialPort(String portName, int baudRate) {
		this(portName, baudRate, DEFAULT_PARITY, DEFAULT_DATA_BITS,
				DEFAULT_STOP_BITS);
	}

	public SerialPort(String portName, int baudRate, int parity) {
		this(portName, baudRate, parity, DEFAULT_DATA_BITS, DEFAULT_STOP_BITS);
	}

	public SerialPort(String portName, int baudRate, int parity, int dataBits) {
		this(portName, baudRate, parity, dataBits, DEFAULT_STOP_BITS);
	}

	public SerialPort(String portName, int baudRate, int parity, int dataBits,
			int stopBits) {
		this.portName = portName;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
	}

	private static String getDefaultPortName() {
		String[] ports = getPortNames();
		if (ports.length > 0) {
			return ports[0];
		} else {
			return "ttyS0";
		}
	}

	private void checkOpen() {
		if (!isOpen)
			throw new RuntimeException("Specified port is not open.");
	}

	public void setBaudRate(int value) throws IOException {
		if (value <= 0)
			throw new IllegalArgumentException("value");

		if (isOpen)
			stream.setAttributes(value, parity, dataBits, stopBits, handshake);

		baudRate = value;
	}

	public int getBytesToRead() throws IOException {
		checkOpen();
		return stream.getBytesToRead();
	}

	public int getBytesToWrite() throws IOException {
		checkOpen();
		return stream.getBytesToWrite();
	}

	public void setDataBits(int value) throws IOException {
		if (value < 5 || value > 8)
			throw new IllegalArgumentException("value");

		if (isOpen)
			stream.setAttributes(baudRate, parity, value, stopBits, handshake);

		dataBits = value;
	}

	public void setDtrEnable(boolean value) throws Exception {
		if (dtrEnable == value)
			return;
		if (isOpen)
			stream.setSignal(SIGNAL_DTR, value);

		dtrEnable = value;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setParity(int value) throws IOException {
		if (value < PARITY_NONE || value > PARITY_SPACE)
			throw new IllegalArgumentException("value");

		if (isOpen)
			stream.setAttributes(baudRate, value, dataBits, stopBits, handshake);

		parity = value;
	}

	public void setPortName(String value) {
		if (isOpen)
			throw new RuntimeException(
					"Port name cannot be set while port is open.");
		if (value == null)
			throw new IllegalArgumentException("value");
		if (value.length() == 0 || value.startsWith("\\\\"))
			throw new IllegalArgumentException("value");

		if (!value.startsWith("/"))
			portName = "/dev/" + value;
		else
			portName = value;
	}

	public void setReadTimeout(Timer time) {
		setReadTimeout((int) time.toMilliseconds());
	}

	public void setReadTimeout(int value) {
		if (value < 0 && value != INFINITE_TIMEOUT)
			throw new IllegalArgumentException("value");

		if (isOpen)
			stream.setReadTimeout(value);

		readTimeout = value;
	}

	public void setStopBits(int value) throws IOException {
		if (value < STOPBITS_ONE || value > STOPBITS_ONE_POINT_FIVE)
			throw new IllegalArgumentException("value");

		if (isOpen)
			stream.setAttributes(baudRate, parity, dataBits, value, handshake);

		stopBits = value;
	}

	public void close() {
		isOpen = false;
		if (stream != null) {
			stream.close();
			stream = null;
		}
	}

	public void discardInBuffer() throws IOException {
		checkOpen();
		stream.discardInBuffer();
	}

	public void discardOutBuffer() throws IOException {
		checkOpen();
		stream.discardOutBuffer();
	}

	public void open() throws Exception {
		if (isOpen)
			throw new RuntimeException("Port is already open");

		stream = new SerialPortStream(portName, baudRate, dataBits, parity,
				stopBits, dtrEnable, rtsEnable, handshake, readTimeout,
				writeTimeout, readBufferSize, writeBufferSize);

		isOpen = true;
	}

	public int read(byte[] buffer, int offset, int count) throws IOException {
		checkOpen();
		if (buffer == null)
			throw new NullPointerException("buffer");
		if (offset < 0 || count < 0)
			throw new IllegalArgumentException(
					"offset or count less than zero.");

		if (buffer.length - offset < count)
			throw new IllegalArgumentException(
					"The size of the buffer is less than offset + count.");

		int ret = stream.read(buffer, offset, count);
		showData(buffer, offset, ret, "Recv");
		return ret;
	}

	public int readByte() throws IOException {
		checkOpen();
		byte[] buff = new byte[1];
		if (stream.read(buff, 0, 1) > 0) {
			showData(buff, 0, 1, "Recv");
			return buff[0] & 0xFF;
		}

		return -1;
	}

	public void write(byte[] buffer, int offset, int count) throws IOException {
		checkOpen();
		if (buffer == null)
			throw new NullPointerException("buffer");

		if (offset < 0 || count < 0)
			throw new IllegalArgumentException();

		if (buffer.length - offset < count)
			throw new IllegalArgumentException(
					"The size of the buffer is less than offset + count.");

		showData(buffer, offset, count, "Send");
		stream.write(buffer, offset, count);
	}

	public void write(byte... params) throws IOException {
		write(params, 0, params.length);
	}

	public static String[] getPortNames() {
		return new String[] { "s3c2410_serial1" };
	}

	private void showData(byte[] buff, int offset, int count, String tag) {
		if (BuildConfig.DEBUG) {
			count += offset;

			StringBuilder sb = new StringBuilder();

			Date now = new Date();
			DateFormat fmt = SimpleDateFormat
					.getTimeInstance(SimpleDateFormat.FULL);

			sb.append(fmt.format(now));
			sb.append(String.format(" %s : ", tag));

			for (int i = offset; i < count; i++) {
				sb.append(String.format("%1$02X ", buff[i]));
			}

			android.util.Log.i("Commbox", sb.toString());
		}
	}
}
