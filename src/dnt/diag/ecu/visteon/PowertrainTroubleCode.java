package dnt.diag.ecu.visteon;

import java.util.ArrayList;
import java.util.List;

import dnt.diag.channel.ChannelException;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.TroubleCodeFunction;

class PowertrainTroubleCode extends TroubleCodeFunction {

	private static byte[] readDTCNumber;
	private static byte[] readDTC;
	private static byte[] clearDTC;

	static {
		readDTCNumber = null;
		readDTC = null;
		clearDTC = null;
	}

	Powertrain ecu;

	private void initCommands() {
		if (readDTCNumber == null) {
			readDTCNumber = getDB().queryCommand("Read DTC Number", "Visteon");
		}

		if (readDTC == null) {
			readDTC = getDB().queryCommand("Read DTC", "Visteon");
		}

		if (clearDTC == null) {
			clearDTC = getDB().queryCommand("Clear DTC", "Visteon");
		}
	}

	PowertrainTroubleCode(Powertrain ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		ecu = this.ecu;
		initCommands();
	}

	@Override
	public List<TroubleCodeItem> readCurrent() {
		try {
			byte[] rData = new byte[100];
			List<TroubleCodeItem> items = new ArrayList<TroubleCodeItem>();

			int length = getChannel().sendAndRecv(readDTCNumber, 0,
					readDTCNumber.length, rData);
			int dtcNum = rData[2] & 0x7F;

			if (dtcNum == 0) {
				return items;
			}

			length = getChannel()
					.sendAndRecv(readDTC, 0, readDTC.length, rData);

			for (int i = 0; i < length; i += 7) {
				System.arraycopy(rData, i + 1, rData, 0, 6);
			}

			for (int i = 0; i < dtcNum; i++) {
				String code = calcStdObdTroubleCode(rData, i, 2, 0);
				items.add(getDB().queryTroubleCode(code, "Visteon"));
			}

			return items;
		} catch (ChannelException e) {
			throw new DiagException(getDB().queryText("Read Trouble Code Fail",
					"Visteon"));
		}
	}

	@Override
	public List<TroubleCodeItem> readHistory() {
		throw new UnsupportedOperationException(
				"Visteon powertrain unsupport history trouble read!!!");
	}

	@Override
	public void clear() {
		try {
			byte[] rData = new byte[100];
			getChannel().sendAndRecv(clearDTC, 0, clearDTC.length, rData);
		} catch (ChannelException e) {
			throw new DiagException(getDB().queryText(
					"Clear Trouble Code Fail", "System"));
		}
	}

}
