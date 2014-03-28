package dnt.diag;

public final class Utils {
	public static byte loByte(long value) {
		return (byte) (value & 0xFF);
	}

	public static byte loByte(int value) {
		return (byte) (value & 0xFF);
	}

	public static byte loByte(short value) {
		return (byte) (value & 0xFF);
	}

	public static byte hiByte(long value) {
		return (byte) ((value >> 8) & 0xFF);
	}

	public static byte hiByte(int value) {
		return (byte) ((value >> 8) & 0xFF);
	}

	public static byte hiByte(short value) {
		return (byte) ((value >> 8) & 0xFF);
	}

	public static short loWord(long value) {
		return (short) (value & 0xFFFF);
	}

	public static short loWord(int value) {
		return (short) (value & 0xFFFF);
	}

	public static short hiWord(long value) {
		return (short) ((value >> 16) & 0xFFFF);
	}

	public static short hiWord(int value) {
		return (short) ((value >> 16) & 0xFFFF);
	}
}
