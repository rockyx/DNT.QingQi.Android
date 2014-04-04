package dnt.diag.ecu.mikuni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.SparseArray;
import dnt.diag.Timer;
import dnt.diag.channel.ChannelException;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.TroubleCodeFunction;

class PowertrainTroubleCodeECU200 extends TroubleCodeFunction {

	private class TroubleCodeCalc {
		protected String low;
		protected String high;

		public TroubleCodeCalc(String low, String high) {
			this.low = low;
			this.high = high;
		}

		public String calcTroubleCode(byte[] recv, int offset, int count) {
			String value = new String(recv, offset, count);
			int data = Integer.valueOf(value, 16).intValue();
			if ((data & 0x1C00) != 0)
				return low;
			else if ((data & 0xE000) != 0)
				return high;
			return "0000";
		}
	}

	private static byte[] syntheticFailure;
	private static SparseArray<byte[]> failureCmds;
	private static SparseArray<TroubleCodeCalc> failureCalcs;
	private static byte[] failureHistoryPointer;
	private static String[] failureHistoryBufferName;
	private static SparseArray<byte[]> failureHistoryBuffer;
	private static byte[] failureHistoryClear;
	private static Map<String, String> mikuni16CTroubleCodes;
	private static Map<String, String> mikuni16ATroubleCodes;

	static {

		syntheticFailure = null;
		failureCmds = null;
		failureCalcs = null;
		failureHistoryPointer = null;

		failureHistoryBufferName = new String[16];
		for (int i = 0; i < 16; i++) {
			failureHistoryBufferName[i] = "Failure History Buffer"
					+ Integer.toString(i);
		}

		failureHistoryBuffer = null;
		failureHistoryClear = null;

		troubleCode16AMapInit();
		troubleCode16CMapInit();
	}

	private PowertrainModel model;
	private String sys;

	private static void troubleCode16AMapInit() {
		mikuni16ATroubleCodes = new HashMap<String, String>();
		mikuni16ATroubleCodes.put("0040", "00");
		mikuni16ATroubleCodes.put("0080", "00");
		mikuni16ATroubleCodes.put("0140", "01");
		mikuni16ATroubleCodes.put("0180", "01");
		mikuni16ATroubleCodes.put("0240", "02");
		mikuni16ATroubleCodes.put("0280", "02");
		mikuni16ATroubleCodes.put("0340", "03");
		mikuni16ATroubleCodes.put("0380", "03");
		mikuni16ATroubleCodes.put("0540", "05");
		mikuni16ATroubleCodes.put("0580", "05");
		mikuni16ATroubleCodes.put("0640", "06");
		mikuni16ATroubleCodes.put("0680", "06");
		mikuni16ATroubleCodes.put("0740", "07");
		mikuni16ATroubleCodes.put("0780", "07");
		mikuni16ATroubleCodes.put("0840", "08");
		mikuni16ATroubleCodes.put("0880", "08");
		mikuni16ATroubleCodes.put("0940", "09");
		mikuni16ATroubleCodes.put("0980", "09");
		mikuni16ATroubleCodes.put("2040", "20");
		mikuni16ATroubleCodes.put("2080", "20");
		mikuni16ATroubleCodes.put("2140", "21");
		mikuni16ATroubleCodes.put("2180", "21");
		mikuni16ATroubleCodes.put("2240", "22");
		mikuni16ATroubleCodes.put("2280", "22");
		mikuni16ATroubleCodes.put("2340", "23");
		mikuni16ATroubleCodes.put("2380", "23");
		mikuni16ATroubleCodes.put("2440", "24");
		mikuni16ATroubleCodes.put("2480", "24");
		mikuni16ATroubleCodes.put("4040", "40");
		mikuni16ATroubleCodes.put("4080", "40");
	}

	private static void troubleCode16CMapInit() {
		mikuni16CTroubleCodes = new HashMap<String, String>();
		mikuni16CTroubleCodes.put("0040", "13");
		mikuni16CTroubleCodes.put("0080", "13");
		mikuni16CTroubleCodes.put("0140", "44");
		mikuni16CTroubleCodes.put("0180", "44");
		mikuni16CTroubleCodes.put("0240", "14");
		mikuni16CTroubleCodes.put("0280", "14");
		mikuni16CTroubleCodes.put("0340", "98");
		mikuni16CTroubleCodes.put("0380", "98");
		mikuni16CTroubleCodes.put("0540", "99");
		mikuni16CTroubleCodes.put("0580", "99");
		mikuni16CTroubleCodes.put("0640", "15");
		mikuni16CTroubleCodes.put("0680", "15");
		mikuni16CTroubleCodes.put("0740", "21");
		mikuni16CTroubleCodes.put("0780", "21");
		mikuni16CTroubleCodes.put("0840", "23");
		mikuni16CTroubleCodes.put("0880", "23");
		mikuni16CTroubleCodes.put("0940", "12");
		mikuni16CTroubleCodes.put("0980", "12");
		mikuni16CTroubleCodes.put("2040", "32");
		mikuni16CTroubleCodes.put("2080", "32");
		mikuni16CTroubleCodes.put("2140", "24");
		mikuni16CTroubleCodes.put("2180", "24");
		mikuni16CTroubleCodes.put("2240", "67");
		mikuni16CTroubleCodes.put("2280", "67");
		mikuni16CTroubleCodes.put("2340", "66");
		mikuni16CTroubleCodes.put("2380", "66");
		mikuni16CTroubleCodes.put("2440", "49");
		mikuni16CTroubleCodes.put("2480", "49");
	}

	private void initCommands() {

		if (syntheticFailure == null) {
			syntheticFailure = getFormat().pack(
					getDB().queryCommand("Synthetic Failure", "Mikuni ECU200"));
		}

		if (failureCmds == null && failureCalcs == null) {
			failureCmds = new SparseArray<byte[]>();
			failureCalcs = new SparseArray<TroubleCodeCalc>();

			byte[] cmd = getDB().queryCommand("Manifold Pressure Failure",
					"Mikuni ECU200");
			failureCmds.put(1, getFormat().pack(cmd));
			failureCalcs.put(1, new TroubleCodeCalc("0040", "0080"));

			cmd = getDB().queryCommand("O2 Sensor Failure", "Mikuni ECU200");
			failureCmds.put(2, getFormat().pack(cmd));
			failureCalcs.put(2, new TroubleCodeCalc("0140", "0180"));

			cmd = getDB().queryCommand("TPS Sensor Failure", "Mikuni ECU200");
			failureCmds.put(3, getFormat().pack(cmd));
			failureCalcs.put(3, new TroubleCodeCalc("0240", "0280"));

			cmd = getDB()
					.queryCommand("Sensor Source Failure", "Mikuni ECU200");
			failureCmds.put(4, getFormat().pack(cmd));
			failureCalcs.put(4, new TroubleCodeCalc("0340", "0380"));

			cmd = getDB().queryCommand("Battery Voltage Failure",
					"Mikuni ECU200");
			failureCmds.put(5, getFormat().pack(cmd));
			failureCalcs.put(5, new TroubleCodeCalc("0540", "0580"));

			cmd = getDB().queryCommand("Engine Temperature Sensor Failure",
					"Mikuni ECU200");
			failureCmds.put(6, getFormat().pack(cmd));
			failureCalcs.put(6, new TroubleCodeCalc("0640", "0680"));

			cmd = getDB().queryCommand("Manifold Temperature Failure",
					"Mikuni ECU200");
			failureCmds.put(7, getFormat().pack(cmd));
			failureCalcs.put(7, new TroubleCodeCalc("0740", "0780"));

			cmd = getDB().queryCommand("Tilt Sensor Failure", "Mikuni ECU200");
			failureCmds.put(8, getFormat().pack(cmd));
			failureCalcs.put(8, new TroubleCodeCalc("0840", "0880"));

			cmd = getDB().queryCommand("DCP Failure", "Mikuni ECU200");
			failureCmds.put(9, getFormat().pack(cmd));
			failureCalcs.put(9, new TroubleCodeCalc("2040", "2080"));

			cmd = getDB()
					.queryCommand("Ignition Coil Failure", "Mikuni ECU200");
			failureCmds.put(10, getFormat().pack(cmd));
			failureCalcs.put(10, new TroubleCodeCalc("2140", "2180"));

			cmd = getDB().queryCommand("O2 Heater Failure", "Mikuni ECU200");
			failureCmds.put(11, getFormat().pack(cmd));
			failureCalcs.put(11, new TroubleCodeCalc("2240", "2280"));

			cmd = getDB().queryCommand("EEPROM Failure", "Mikuni ECU200");
			failureCmds.put(12, getFormat().pack(cmd));
			failureCalcs.put(12, new TroubleCodeCalc("4040", "4080"));

			cmd = getDB().queryCommand("Air Valve Failure", "Mikuni ECU200");
			failureCmds.put(13, getFormat().pack(cmd));
			failureCalcs.put(13, new TroubleCodeCalc("2340", "2380"));

			cmd = getDB().queryCommand("SAV Failure", "Mikuni ECU200");
			failureCmds.put(14, getFormat().pack(cmd));
			failureCalcs.put(14, new TroubleCodeCalc("2440", "2480"));

			cmd = getDB().queryCommand("CPS Failure", "Mikuni ECU200");
			failureCmds.put(15, getFormat().pack(cmd));
			failureCalcs.put(15, new TroubleCodeCalc("0940", "0980"));
		}

		if (failureHistoryPointer == null) {
			failureHistoryPointer = getFormat().pack(
					getDB().queryCommand("Failure History Pointer",
							"Mikuni ECU200"));
		}

		if (failureHistoryBuffer == null) {
			failureHistoryBuffer = new SparseArray<byte[]>();
			for (int i = 0; i < 16; i++) {
				byte[] cmd = getDB().queryCommand(failureHistoryBufferName[i],
						"Mikuni ECU200");
				cmd = getFormat().pack(cmd);
				failureHistoryBuffer.put(i, cmd);

			}
		}

		if (failureHistoryClear == null) {
			failureHistoryClear = getFormat().pack(
					getDB().queryCommand("Failure History Clear",
							"Mikuni ECU200"));
		}
	}

	private void pushTcs(List<TroubleCodeItem> tcs, String code) {
		TroubleCodeItem item = getDB().queryTroubleCode(code, sys);
		if (model == PowertrainModel.DCJ_16A)
			item.setCode(mikuni16ATroubleCodes.get(code));
		else if (model == PowertrainModel.DCJ_16C)
			item.setCode(mikuni16CTroubleCodes.get(code));
		tcs.add(item);
	}

	public PowertrainTroubleCodeECU200(PowertrainECU200 ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());

		this.model = ecu.getModel();
		switch (model) {
		case DCJ_16A:
			troubleCode16AMapInit();
			sys = "DCJ Mikuni ECU200";
			break;
		case DCJ_16C:
			troubleCode16CMapInit();
			sys = "DCJ Mikuni ECU200";
			break;
		case DCJ_10:
			sys = "DCJ Mikuni ECU200";
			break;
		case QM200GY_F:
		case QM200_3D:
		case QM200J_3L:
			sys = "QingQi Mikuni ECU200";
			break;
		default:
			break;
		}

		initCommands();
	}

	@Override
	public List<TroubleCodeItem> readCurrent() {

		try {
			List<TroubleCodeItem> tcs = new ArrayList<TroubleCodeItem>();

			byte[] rData = new byte[100];
			int length = getChannel().sendAndRecv(syntheticFailure, 0,
					syntheticFailure.length, rData);

			if (rData[0] != 0x30 || rData[1] != 0x30 || rData[2] != 0x30
					|| rData[3] != 0x30) {
				for (int i = 1; i <= 15; i++) {
					byte[] cmd = failureCmds.get(i);
					length = getChannel()
							.sendAndRecv(cmd, 0, cmd.length, rData);

					if (rData[0] != 0x30 || rData[1] != 0x30
							|| rData[2] != 0x30 || rData[3] != 0x30) {
						String code = failureCalcs.get(i).calcTroubleCode(
								rData, 0, length);
						if (!code.equals("0000"))
							pushTcs(tcs, code);
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
			int length = getChannel().sendAndRecv(failureHistoryPointer, 0,
					failureHistoryPointer.length, rData);

			String temp = new String(rData, 0, length);

			int pointer = Integer.valueOf(temp, 16);

			for (int i = 0; i < 16; i++) {
				int pos = pointer - i - 1;
				if (pos < 0)
					pos = pointer + 15 - i;

				byte[] cmd = failureHistoryBuffer.get(pos);
				length = getChannel().sendAndRecv(cmd, 0, cmd.length, rData);

				if (rData[0] != 0x30 || rData[1] != 0x30 || rData[2] != 0x30
						|| rData[3] != 0x30) {
					String code = new String(rData, 0, length);
					if (!code.equals("0000"))
						pushTcs(tcs, code);
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
			getChannel().sendAndRecv(failureHistoryClear, 0,
					failureHistoryClear.length, rData);

			if (rData[0] != 'A') {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

			Thread.sleep(Timer.fromSeconds(5).toMilliseconds());

			getChannel().sendAndRecv(failureHistoryPointer, 0,
					failureHistoryPointer.length, rData);

			if (rData[0] != '0' || rData[1] != '0' || rData[2] != '0'
					|| rData[3] != '0') {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

			for (int i = 0; i < 16; i++) {
				byte[] cmd = failureHistoryBuffer.get(i);
				getChannel().sendAndRecv(cmd, 0, cmd.length, rData);

				if (rData[0] != '0' || rData[1] != '0' || rData[2] != '0'
						|| rData[3] != '0') {
					throw new DiagException(getDB().queryText(
							"Clear Trouble Code Fail", "System"));
				}
			}

		} catch (ChannelException e) {
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		} catch (InterruptedException e) {
			throw new DiagException("System interrupted!");
		}
	}
}
