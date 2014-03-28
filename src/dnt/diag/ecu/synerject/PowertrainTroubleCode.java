package dnt.diag.ecu.synerject;

import java.util.ArrayList;
import java.util.List;

import dnt.diag.channel.ChannelException;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.TroubleCodeFunction;

public class PowertrainTroubleCode extends TroubleCodeFunction {

	protected static byte[] readDTCByStatus;
	protected static byte[] clearTroubleCode1;

	static {
		readDTCByStatus = null;
		clearTroubleCode1 = null;
	}

	private String sys;
	private Powertrain ecu;

	private void initCommands() {
		if (readDTCByStatus == null) {
			readDTCByStatus = getFormat().pack(
					getDB().queryCommand("Read DTC By Status", "Synerject"));
		}

		if (clearTroubleCode1 == null) {
			clearTroubleCode1 = getFormat().pack(
					getDB().queryCommand("Clear Trouble Code1", "Synerject"));
		}
	}

	public PowertrainTroubleCode(Powertrain ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		this.ecu = ecu;
		switch (ecu.getModel()) {
		case QM125T_8H:
		case QM250GY:
		case QM250T:
			sys = "QingQi Synerject";
			break;
		default:
			throw new DiagException("Unsupport model!!");
		}

		initCommands();
	}

	private List<TroubleCodeItem> readTroubleCode(boolean isHistory) {
		try {
			byte[] rData = new byte[100];

			ecu.startCommunication.execute();

			int length = getChannel().sendAndRecv(readDTCByStatus, 0,
					readDTCByStatus.length, rData);

			ecu.endCommunication.execute();

			if (length <= 0) {
				throw new DiagException(getDB().queryText("Communication Fail",
						"System"));
			}

			if (rData[0] != 0x58) {
				throw new DiagException(getDB().queryText(
						"Read Trouble Code Fail", "System"));
			}

			List<TroubleCodeItem> items = new ArrayList<TroubleCodeItem>();

			int dtcNum = rData[1] & 0xFF;

			for (int i = 0; i < dtcNum; i++) {
				if (!isHistory) {
					if ((rData[i * 3 + 4] & 0x40) == 0)
						continue;
				}
				String code = calcStdObdTroubleCode(rData, i, 3, 2);
				TroubleCodeItem item = getDB().queryTroubleCode(code, sys);
				items.add(item);
			}

			return items;

		} catch (ChannelException ex) {
			throw new DiagException(ex.getMessage());
		}
	}

	@Override
	public List<TroubleCodeItem> readCurrent() {
		return readTroubleCode(false);
	}

	@Override
	public List<TroubleCodeItem> readHistory() {
		return readTroubleCode(true);
	}

	@Override
	public void clear() {
		try {
			ecu.startCommunication.execute();

			byte[] rData = new byte[100];
			int length = getChannel().sendAndRecv(clearTroubleCode1, 0,
					clearTroubleCode1.length, rData);

			if (length <= 0) {
				throw new DiagException(getDB().queryText("Communication Fail",
						"System"));
			}

			if (rData[0] != 0x54) {
				throw new DiagException(getDB().queryText(
						"Clear Trouble Code Fail", "System"));
			}

		} catch (ChannelException ex) {
			throw new DiagException(ex.getMessage());
		}
	}

}
