package dnt.diag.formats;

import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;

public final class ISO9141Format extends AbstractFormat {

	public ISO9141Format(Attribute attr) {
		super(attr);
	}

	@Override
	public byte[] pack(byte[] src, int offset, int count) {
		byte[] result = new byte[count + 4];
		result[0] = Utils.loByte(getAttribute().isoHeader);
		result[1] = Utils.loByte(getAttribute().klineTargetAddress);
		result[2] = Utils.loByte(getAttribute().klineSourceAddress);
		System.arraycopy(src, offset, result, 3, count);

		int lastIndex = result.length - 1;
		int length = count + 3;
		int checksum = 0;
		for (int i = 0; i < length; i++) {
			checksum += result[i] & 0xFF;
		}
		result[lastIndex] = Utils.loByte(checksum);
		return result;
	}

	@Override
	public byte[] unpack(byte[] src, int offset, int count) {
		int cs = 0;
		int length = count - 1;
		for (int i = 0; i < length; i++) {
			cs += src[offset + i] & 0xFF;
		}

		cs &= 0xFF;
		if (cs != (src[offset + length] & 0xFF))
			return null;

		length = count - 4;
		byte[] result = new byte[length];
		System.arraycopy(src, offset + 3, result, 0, length);

		return result;
	}

}
