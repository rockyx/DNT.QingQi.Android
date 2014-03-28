package dnt.diag.formats;

import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;

public final class KWP1281Format extends AbstractFormat {

	private int frameCounter;
	static private final byte FRAME_END = 0x03;

	public KWP1281Format(Attribute attr) {
		super(attr);
		frameCounter = 0;
	}

	byte frameCounterIncrement() {
		return Utils.loByte(++frameCounter);
	}

	@Override
	public byte[] pack(byte[] src, int offset, int count) {
		byte[] result = new byte[count + 3];
		result[0] = Utils.loByte(count + 2);
		result[1] = frameCounterIncrement();

		System.arraycopy(src, offset, result, 2, count);
		result[result.length - 1] = FRAME_END;
		return result;
	}

	@Override
	public byte[] unpack(byte[] src, int offset, int count) {
		byte[] result = new byte[count - 2];
		System.arraycopy(src, offset + 1, result, 0, count - 2);
		return result;
	}

}
