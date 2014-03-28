package dnt.diag.formats;

import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;

public final class KWP2KFormat extends AbstractFormat {

	final static int KWP8X_HEADER_LENGTH = 3;
	final static int KWPCX_HEADER_LENGTH = 3;
	final static int KWP80_HEADER_LENGTH = 4;
	final static int KWPXX_HEADER_LENGTH = 1;
	final static int KWP00_HEADER_LENGTH = 2;
	final static int KWP_CHECKSUM_LENGTH = 1;
	final static int KWP_MAX_DATA_LENGTH = 128;

	public KWP2KFormat(Attribute attr) {
		super(attr);
	}

	@Override
	public byte[] pack(byte[] src, int offset, int count) {
		int pos = 0;
		int cs = 0;
		byte[] result = null;

		switch (getAttribute().kwp2kCurrentMode) {
		case Mode8X:
			result = new byte[KWP8X_HEADER_LENGTH + count + KWP_CHECKSUM_LENGTH];
			result[pos++] = Utils.loByte(0x80 | count);
			result[pos++] = Utils.loByte(getAttribute().klineTargetAddress);
			result[pos++] = Utils.loByte(getAttribute().klineSourceAddress);
			break;
		case ModeCX:
			result = new byte[KWPCX_HEADER_LENGTH + count + KWP_CHECKSUM_LENGTH];
			result[pos++] = Utils.loByte(0xC0 | count);
			result[pos++] = Utils.loByte(getAttribute().klineTargetAddress);
			result[pos++] = Utils.loByte(getAttribute().klineSourceAddress);
			break;
		case Mode80:
			result = new byte[KWP80_HEADER_LENGTH + count + KWP_CHECKSUM_LENGTH];
			result[pos++] = Utils.loByte(0x80);
			result[pos++] = Utils.loByte(getAttribute().klineTargetAddress);
			result[pos++] = Utils.loByte(getAttribute().klineSourceAddress);
			result[pos++] = Utils.loByte(count);
			break;
		case Mode00:
			result = new byte[KWP00_HEADER_LENGTH + count + KWP_CHECKSUM_LENGTH];
			result[pos++] = 0x00;
			result[pos++] = Utils.loByte(count);
			break;
		case ModeXX:
			result = new byte[KWPXX_HEADER_LENGTH + count + KWP_CHECKSUM_LENGTH];
			result[pos++] = Utils.loByte(count);
			break;
		default:
			return null;
		}

		System.arraycopy(src, offset, result, pos, count);
		pos += count;

		int i;
		for (i = 0; i < pos; i++)
			cs += result[i] & 0xFF;

		result[i] = Utils.loByte(cs);
		return result;
	}

	@Override
	public byte[] unpack(byte[] src, int offset, int count) {
		int length = 0;
		byte[] result = null;

		if ((src[offset] & 0xFF) > 0x80) {
			length = (src[offset] & 0xFF) - 0x80;
			if ((src[offset + 1] & 0xFF) != getAttribute().klineSourceAddress)
				return null;

			if (length != (count - KWP8X_HEADER_LENGTH - KWP_CHECKSUM_LENGTH)) {
				length = (src[offset] & 0xFF) - 0xC0; // for kwp cx
				if (length != (count - KWPCX_HEADER_LENGTH - KWP_CHECKSUM_LENGTH))
					return null;
				else
					offset = offset + KWPCX_HEADER_LENGTH;
			} else {
				offset = offset + KWP8X_HEADER_LENGTH;
			}
		} else if ((src[offset] & 0xFF) == 0x80) {
			length = src[offset + 3] & 0xFF;
			if ((src[offset + 1] & 0xFF) != getAttribute().klineSourceAddress)
				return null;

			if (length != (count - KWP80_HEADER_LENGTH - KWP_CHECKSUM_LENGTH))
				return null;
			offset = offset + KWP80_HEADER_LENGTH;
		} else if (src[offset] == 0x00) {
			length = src[offset + 1] & 0xFF;
			if (length != (count - KWP00_HEADER_LENGTH - KWP_CHECKSUM_LENGTH))
				return null;
			offset = offset + KWP00_HEADER_LENGTH;
		} else {
			length = src[offset] & 0xFF;
			if (length != (count - KWPXX_HEADER_LENGTH - KWP_CHECKSUM_LENGTH))
				return null;
			offset = offset + KWPXX_HEADER_LENGTH;
		}

		result = new byte[length];
		System.arraycopy(src, offset, result, 0, length);
		return result;
	}

}
