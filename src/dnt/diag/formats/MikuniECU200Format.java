package dnt.diag.formats;

import dnt.diag.attribute.Attribute;

public final class MikuniECU200Format extends AbstractFormat {

	private final static byte HEAD_FORMAT = 0x48;

	public MikuniECU200Format(Attribute attr) {
		super(attr);
	}

	@Override
	public byte[] pack(byte[] src, int offset, int count) {
		byte[] result = new byte[count + 3];
		result[0] = HEAD_FORMAT;
		System.arraycopy(src, offset, result, 1, count);
		result[count + 1] = 0x0D;
		result[count + 2] = 0x0A;

		return result;
	}

	@Override
	public byte[] unpack(byte[] src, int offset, int count) {
		byte[] result = new byte[count - 3];
		System.arraycopy(src, offset + 1, result, 0, count - 3);

		return result;
	}

}
