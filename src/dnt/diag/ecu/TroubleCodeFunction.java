package dnt.diag.ecu;

import java.util.List;

import dnt.diag.channel.Channel;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.db.VehicleDB;
import dnt.diag.formats.Format;

public abstract class TroubleCodeFunction extends AbstractFunction {

	public TroubleCodeFunction(VehicleDB db, Channel chn, Format format) {
		super(db, chn, format);
	}

	public static String calcStdObdTroubleCode(byte[] buffer, int pos,
			int factor, int offset) {
		StringBuilder sb = new StringBuilder();
		int mode = buffer[pos * factor + offset] & 0xC0;
		int value1 = buffer[pos * factor + offset] & 0xFF;
		int value2 = buffer[pos * factor + offset + 1] & 0xFF;
		switch (mode) {
		case 0x00:
			sb.append(String.format("P%1$02X", value1));
			break;
		case 0x40:
			sb.append(String.format("C%1$02X", value1));
			break;
		case 0x80:
			sb.append(String.format("B%1$02X", value1));
			break;
		case 0xC0:
			sb.append(String.format("U%1$02X", value1));
			break;
		default:
			break;
		}
		sb.append(String.format("%1$02X", value2));
		return sb.toString();
	}

	public abstract List<TroubleCodeItem> readCurrent();

	public abstract List<TroubleCodeItem> readHistory();

	public abstract void clear();

}
