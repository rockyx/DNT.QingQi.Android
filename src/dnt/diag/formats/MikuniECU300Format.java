package dnt.diag.formats;

import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;

public final class MikuniECU300Format extends AbstractFormat {

	public MikuniECU300Format(Attribute attr) {
		super(attr);
	}

	@Override
	public byte[] pack(byte[] src, int offset, int count) {
		byte[] result = new byte[count + 3];
		count += 1;
		result[0] = Utils.hiByte(count);
		result[1] = Utils.loByte(count);
		System.arraycopy(src, offset, result, 2, count - 1);

		int cs = 0;
		int length = result.length - 1;
		for (int i = 0; i < length; i++)
			cs += result[i] & 0xFF;

		cs = 0x00 - (cs & 0xFF);
		result[length] = Utils.loByte(cs);

		return result;
	}

	@Override
	public byte[] unpack(byte[] src, int offset, int count) {
		int length = ((src[0] & 0xFF) << 8) | (src[1] & 0xFF);
		byte[] result = new byte[length];
		System.arraycopy(src, offset - 2, result, 0, length);
		return result;
	}

}
