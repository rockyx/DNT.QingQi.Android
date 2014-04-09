package dnt.diag.ecu.mikuni;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;
import dnt.diag.channel.ChannelException;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.TroubleCodeFunction;

public class PowertrainTroubleCodeECU300 extends TroubleCodeFunction {

	private class TroubleCodeCalc {
		protected String low;
		protected String high;

		public TroubleCodeCalc(String low, String high) {
			this.low = low;
			this.high = high;
		}

		public String calcTroubleCode(byte[] recv, int offset, int count) {
			int value = recv[offset + 1] & 0xFF;
			if ((value & 0x04) != 0) {
				return high;
			} else if ((value & 0x20) != 0) {
				return low;
			}
			return null;
		}
	}

	private static byte[] syntheticFailure;
	private static SparseArray<byte[]> failureCmds;
	private static SparseArray<TroubleCodeCalc> failureCalcs;
	private static byte[] failureHistoryPointer;
	private static String[] failureHistoryBufferName;
	private static SparseArray<byte[]> failureHistoryBuffer;
	private static byte[] failureHistoryClear1;
	private static byte[] failureHistoryClear2;
	private static byte[] failureHistoryClear3;

	static {

		syntheticFailure = null;
		failureCmds = null;
		failureCalcs = null;
		failureHistoryPointer = null;

		failureHistoryBufferName = new String[17];
		for (int i = 1; i < 17; i++) {
			failureHistoryBufferName[i] = "Failure History Buffer"
					+ Integer.toString(i);
		}

		failureHistoryBuffer = null;
		failureHistoryClear1 = null;
		failureHistoryClear2 = null;
		failureHistoryClear3 = null;

	}

	private PowertrainModel model;
	private String sys;
	private LiveDataItem erfItem;

	private void initCommands() {

		if (syntheticFailure == null) {
			syntheticFailure = getFormat().pack(
					getDB().queryCommand("Synthetic Failure", "Mikuni ECU300"));
		}

		if (failureCmds == null && failureCalcs == null) {
			failureCmds = new SparseArray<byte[]>();
			failureCalcs = new SparseArray<TroubleCodeCalc>();

			byte[] cmd = getDB().queryCommand("O2 Sensor Failure",
					"Mikuni ECU300");
			failureCmds.put(1, getFormat().pack(cmd));
			failureCalcs.put(1, new TroubleCodeCalc("0140", "0180"));

			cmd = getDB().queryCommand("TPS Value Failure", "Mikuni ECU300");
			failureCmds.put(2, getFormat().pack(cmd));
			failureCalcs.put(2, new TroubleCodeCalc("0240", "0280"));

			cmd = getDB()
					.queryCommand("Sensor Source Failure", "Mikuni ECU300");
			failureCmds.put(3, getFormat().pack(cmd));
			failureCalcs.put(3, new TroubleCodeCalc("0340", "0380"));

			cmd = getDB().queryCommand("Battery Voltage Failure",
					"Mikuni ECU300");
			failureCmds.put(4, getFormat().pack(cmd));
			failureCalcs.put(4, new TroubleCodeCalc("0540", "0580"));

			cmd = getDB().queryCommand("Engine Temperature Sensor Failure",
					"Mikuni ECU300");
			failureCmds.put(5, getFormat().pack(cmd));
			failureCalcs.put(5, new TroubleCodeCalc("0640", "0680"));

			cmd = getDB().queryCommand("Tilt Sensor Failure", "Mikuni ECU300");
			failureCmds.put(6, getFormat().pack(cmd));
			failureCalcs.put(6, new TroubleCodeCalc("0880", "0880"));

			cmd = getDB().queryCommand("Injector Failure", "Mikuni ECU300");
			failureCmds.put(7, getFormat().pack(cmd));
			failureCalcs.put(7, new TroubleCodeCalc("2040", "2080"));

			cmd = getDB()
					.queryCommand("Ignition Coil Failure", "Mikuni ECU300");
			failureCmds.put(8, getFormat().pack(cmd));
			failureCalcs.put(8, new TroubleCodeCalc("2140", "2180"));

			cmd = getDB().queryCommand("DSV Failure", "Mikuni ECU300");
			failureCmds.put(9, getFormat().pack(cmd));
			failureCalcs.put(9, new TroubleCodeCalc("2840", "2880"));

			cmd = getDB().queryCommand("PDP Failure", "Mikuni ECU300");
			failureCmds.put(10, getFormat().pack(cmd));
			failureCalcs.put(10, new TroubleCodeCalc("2740", "2780"));

			cmd = getDB().queryCommand("EEPROM Failure", "Mikuni ECU300");
			failureCmds.put(11, getFormat().pack(cmd));
			failureCalcs.put(11, new TroubleCodeCalc("4040", "4080"));
		}

		if (failureHistoryPointer == null) {
			failureHistoryPointer = getFormat().pack(
					getDB().queryCommand("Failure History Pointer",
							"Mikuni ECU300"));
		}

		if (failureHistoryBuffer == null) {
			failureHistoryBuffer = new SparseArray<byte[]>();
			for (int i = 1; i < 17; i++) {
				byte[] cmd = getDB().queryCommand(failureHistoryBufferName[i],
						"Mikuni ECU300");
				cmd = getFormat().pack(cmd);
				failureHistoryBuffer.put(i, cmd);

			}
		}

		if (failureHistoryClear1 == null) {
			failureHistoryClear1 = getFormat().pack(
					getDB().queryCommand("Failure History Clear1",
							"Mikuni ECU300"));
		}

		if (failureHistoryClear2 == null) {
			failureHistoryClear2 = getFormat().pack(
					getDB().queryCommand("Failure History Clear2",
							"Mikuni ECU300"));
		}

		if (failureHistoryClear3 == null) {
			failureHistoryClear3 = getFormat().pack(
					getDB().queryCommand("Failure History Clear3",
							"Mikuni ECU300"));
		}
	}

	public PowertrainTroubleCodeECU300(PowertrainECU300 ecu,
			LiveDataItem erfItem) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		this.model = ecu.getModel();

		switch (model) {
		case QM48QT_8:
			sys = "QingQi Mikuni ECU300";
			break;
		default:
			break;
		}
		initCommands();
		this.erfItem = erfItem;
	}

	@Override
	public List<TroubleCodeItem> readCurrent() {
		try {
			List<TroubleCodeItem> tcs = new ArrayList<TroubleCodeItem>();

			byte[] rData = new byte[100];
			int length = getChannel().sendAndRecv(syntheticFailure, 0,
					syntheticFailure.length, rData);

			if (!PowertrainECU300.checkIfPositive(rData, syntheticFailure))
				throw new DiagException(getDB().queryText(
						"Read Trouble Code Fail", "System"));

			if (rData[1] != 0x00 || rData[2] != 0x00) {
				for (int i = 1; i <= 11; i++) {
					byte[] cmd = failureCmds.get(i);
					length = getChannel()
							.sendAndRecv(cmd, 0, cmd.length, rData);

					if (!PowertrainECU300.checkIfPositive(rData, cmd))
						throw new DiagException(getDB().queryText(
								"Read Trouble Code Fail", "System"));

					if ((rData[1] != 0x00) || (rData[2] != 0x00)) {
						String code = failureCalcs.get(i).calcTroubleCode(
								rData, 1, length);
						if (code != null)
							tcs.add(getDB().queryTroubleCode(code, sys));
					}
				}
			}

			return tcs;

		} catch (ChannelException ex) {
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

	@Override
	public List<TroubleCodeItem> readHistory() {
		try {
			List<TroubleCodeItem> tcs = new ArrayList<TroubleCodeItem>();

			byte[] rData = new byte[100];
			getChannel().sendAndRecv(failureHistoryPointer, 0,
					failureHistoryPointer.length, rData);

			if (!PowertrainECU300.checkIfPositive(rData, failureHistoryPointer))
				throw new DiagException(getDB().queryText(
						"Read Trouble Code Fail", "System"));

			int pointer = rData[2] & 0xFF;

			for (int i = 1; i < 17; i++) {
				int pos = pointer + 1 - i;
				if (pos <= 0)
					pos = i;

				byte[] cmd = failureHistoryBuffer.get(pos);
				getChannel().sendAndRecv(cmd, 0, cmd.length, rData);

				if (!PowertrainECU300.checkIfPositive(rData, cmd))
					throw new DiagException(getDB().queryText(
							"Read Trouble Code Fail", "System"));

				if (rData[1] != 0x00 || rData[2] != 0x00) {
					String code = Integer.toHexString((rData[1] & 0xFF) * 256
							+ (rData[2] & 0xFF));
					if (code.length() != 4) {
						code = "0" + code;
					}
					TroubleCodeItem item = getDB().queryTroubleCode(code, sys);
					if (!tcs.contains(item)) {
						tcs.add(getDB().queryTroubleCode(code, sys));
					}
				}
			}

			return tcs;
		} catch (ChannelException ex) {
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

	@Override
	public void clear() {
		try {
			byte[] rData = new byte[100];

			// Check engine status.
			byte[] buff = erfItem.getEcuResponseBuff().getBuff();
			byte[] cmd = erfItem.getFormattedCommand();
			getChannel().sendAndRecv(cmd, 0, cmd.length, buff);

			if (!PowertrainECU300.checkIfPositive(buff, cmd)) {
				throw new DiagException(getDB().queryText(
						"Checking Engine Status Fail", "Mikuni"));
			}

			erfItem.calcValue();

			if (!erfItem.getValue().equals(getDB().queryText("Stopped", "System"))) {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail Because ERF", "Mikuni"));
			}

			getChannel().sendAndRecv(failureHistoryClear1, 0,
					failureHistoryClear1.length, rData);

			if (!PowertrainECU300.checkIfPositive(rData, failureHistoryClear1)) {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

			getChannel().sendAndRecv(failureHistoryClear2, 0,
					failureHistoryClear2.length, rData);

			if (!PowertrainECU300.checkIfPositive(rData, failureHistoryClear1)) {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

			getChannel().sendAndRecv(failureHistoryClear3, 0,
					failureHistoryClear3.length, rData);

			if (!PowertrainECU300.checkIfPositive(rData, failureHistoryClear1)) {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

		} catch (ChannelException e) {
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

}
