package dnt.diag.ecu.mikuni;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import dnt.diag.ProtocolType;
import dnt.diag.Timer;
import dnt.diag.attribute.Attribute;
import dnt.diag.channel.ChannelException;
import dnt.diag.channel.ChannelFactory;
import dnt.diag.commbox.Commbox;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.TroubleCodeItem;
import dnt.diag.db.VehicleDB;
import dnt.diag.ecu.AbstractECU;
import dnt.diag.ecu.DiagException;
import dnt.diag.formats.MikuniECU300Format;

public class PowertrainECU300 extends AbstractECU {
	private byte[] startConnection;
	private byte[] tpsIdleLearningValueSetting;
	private byte[] longTermLearningValueReset;
	private byte[] dsvISCLearningValueSetting;
	private byte[] readEcuVersion;

	private Attribute attr;
	private PowertrainModel model;

	private void initCommands() {
		startConnection = getFormat().pack(
				getDB().queryCommand("Start Connection", "Mikuni ECU300"));

		tpsIdleLearningValueSetting = getFormat().pack(
				getDB().queryCommand("TPS Idle Learning Value Setting",
						"Mikuni ECU300"));
		longTermLearningValueReset = getFormat().pack(
				getDB().queryCommand(
						"02 Feed Back Long Term Learning Value Reset",
						"Mikuni ECU300"));
		dsvISCLearningValueSetting = getFormat().pack(
				getDB().queryCommand("DSV ISC Learning Value Reset",
						"Mikuni ECU300"));
		readEcuVersion = getFormat()
				.pack(getDB().queryCommand("ECU Version Information",
						"Mikuni ECU300"));
	}

	public static boolean checkIfPositive(byte[] rData, byte[] cmd) {
		if ((rData[0] & 0xFF) != ((cmd[2] & 0xFF) + 0x40))
			return false;
		return true;
	}

	public PowertrainModel getModel() {
		return model;
	}

	public PowertrainECU300(VehicleDB db, Commbox box, PowertrainModel model) {
		super(db, box);
		attr = new Attribute();
		this.model = model;

		switch (model) {
		case QM48QT_8:
			attr.klineParity = Attribute.KLineParity.None;
			attr.klineBaudRate = 19200;
			setChannel(ChannelFactory.create(attr, box,
					ProtocolType.MikuniECU300));
			setFormat(new MikuniECU300Format(attr));
			break;
		default:
			throw new DiagException("Unsupport Model!!");
		}
		this.model = model;

		if (getChannel() == null)
			throw new DiagException("Cannot create channel!!!");

		initCommands();

		setDataStream(new PowertrainDataStreamECU300(this));
		setTroubleCode(new PowertrainTroubleCodeECU300(this));
	}

	@Override
	public void channelInit() {
		try {
			getChannel().setByteTxInterval(Timer.fromMicroseconds(100));
			getChannel().setFrameTxInterval(Timer.fromMicroseconds(1000));
			getChannel().setByteRxTimeout(Timer.fromMicroseconds(400000));
			// getChannel().setFrameRxTimeout(Timer.fromMicroseconds(500000));
			getChannel().setFrameRxTimeout(Timer.fromSeconds(2));
			getChannel().startCommunicate();
			byte[] rData = new byte[128];
			int length = getChannel().sendAndRecv(startConnection, 0,
					startConnection.length, rData);
			if (length <= 0 || rData[0] != 0x40)
				throw new DiagException("Start Connection Fail!");
		} catch (ChannelException e) {
			// e.printStackTrace();
			throw new DiagException(e.getMessage());
		}
	}

	public static void checkEngineStop(PowertrainECU300 ecu) {

		try {
			LiveDataItem item = ecu.getDataStream().getLiveDataItems()
					.get("ERF");
			byte[] buff = item.getEcuResponseBuff().getBuff();
			byte[] cmd = item.getFormattedCommand();
			ecu.getChannel().sendAndRecv(cmd, 0, cmd.length, buff);

			if (!checkIfPositive(buff, cmd)) {
				throw new DiagException(ecu.getDB().queryText(
						"Checking Engine Status Fail", "Mikuni"));
			}

			item.calcValue();

			if (!item.getValue().equals(
					ecu.getDB().queryText("Stopped", "System"))) {
				throw new DiagException(ecu.getDB().queryText(
						"Function Fail Because ERF", "Mikuni"));
			}
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}

	}

	public void tpsIdleLearningValueSetting() {
		try {
			List<TroubleCodeItem> items = getTroubleCode().readCurrent();

			if (!items.isEmpty()) {
				throw new DiagException(getDB().queryText(
						"Function Fail Because TroubleCodes", "Mikuni"));
			}

			checkEngineStop(this);

			byte[] rData = new byte[100];

			getChannel().sendAndRecv(tpsIdleLearningValueSetting, 0,
					tpsIdleLearningValueSetting.length, rData);
			if (!checkIfPositive(rData, tpsIdleLearningValueSetting)) {
				throw new DiagException(getDB().queryText(
						"TPS Idle Setting Fail", "Mikuni"));
			}
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

	public void longTermLearningValueReset() {
		try {
			checkEngineStop(this);

			byte[] rData = new byte[100];

			getChannel().sendAndRecv(longTermLearningValueReset, 0,
					longTermLearningValueReset.length, rData);

			if (!checkIfPositive(rData, longTermLearningValueReset)) {
				throw new DiagException(getDB().queryText(
						"Long Term Learn Value Zone Initialization Fail",
						"Mikuni"));
			}
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

	public void dsvISCLearningValueSetting() {
		try {
			checkEngineStop(this);

			byte[] rData = new byte[100];

			getChannel().sendAndRecv(dsvISCLearningValueSetting, 0,
					dsvISCLearningValueSetting.length, rData);

			if (!checkIfPositive(rData, dsvISCLearningValueSetting)) {
				throw new DiagException(getDB().queryText(
						"DSV ISC Learning Value Reset Fail", "Mikuni"));
			}
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

	public PowertrainVersion readVersion() {
		try {
			byte[] rData = new byte[100];

			getChannel().sendAndRecv(readEcuVersion, 0, readEcuVersion.length,
					rData);

			if (!checkIfPositive(rData, readEcuVersion)) {
				throw new DiagException(getDB().queryText(
						"Read ECU Version Fail", "System"));
			}

			PowertrainVersion ver = new PowertrainVersion();
			// ver.model = "3Y07";
			ver.model = "ECU300"; // Customer ID

			Charset cs = Charset.forName("US-ASCII");
			ByteBuffer bb = ByteBuffer.allocate(100);

			for (int i = 1; rData[i] != 0x00; i++) {
				bb.put(rData[i]);
			}
			bb.flip();

			String temp = cs.decode(bb).toString();
			String[] slip = temp.split("-");

			ver.hardware = slip[0]; // Manage Number
			ver.software = slip[1]; // Software version
//			ver.hardware = "";
//			ver.software = cs.decode(bb).toString();

			return ver;

		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}
}
