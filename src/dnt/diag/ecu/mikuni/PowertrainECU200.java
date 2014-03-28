package dnt.diag.ecu.mikuni;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import android.util.SparseArray;

import dnt.diag.ProtocolType;
import dnt.diag.Timer;
import dnt.diag.attribute.Attribute;
import dnt.diag.channel.ChannelException;
import dnt.diag.channel.ChannelFactory;
import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import dnt.diag.ecu.AbstractECU;
import dnt.diag.ecu.DiagException;
import dnt.diag.formats.MikuniECU200Format;

public class PowertrainECU200 extends AbstractECU {

	private static byte[] readECUVersion1;
	private static byte[] readECUVersion2;
	private static byte[] engineRevolutions;
	private static byte[] tpsIdleAdjustments;
	private static byte[] longTermLearnValueZoneInitialization;
	private static SparseArray<byte[]> longTermLearnValueZones;
	private static byte[] iscLearnValueInitialization;

	static {
		readECUVersion1 = null;
		readECUVersion2 = null;
		engineRevolutions = null;
		tpsIdleAdjustments = null;
		longTermLearnValueZoneInitialization = null;
		longTermLearnValueZones = null;
		iscLearnValueInitialization = null;
	}

	private PowertrainModel model;

	private void initCommands() {
		if (readECUVersion1 == null) {
			readECUVersion1 = getFormat()
					.pack(getDB().queryCommand("Read ECU Version 1",
							"Mikuni ECU200"));
		}

		if (readECUVersion2 == null) {
			readECUVersion2 = getFormat()
					.pack(getDB().queryCommand("Read ECU Version 2",
							"Mikuni ECU200"));
		}

		if (engineRevolutions == null) {
			engineRevolutions = getFormat()
					.pack(getDB().queryCommand("Engine Revolutions",
							"Mikuni ECU200"));
		}

		if (tpsIdleAdjustments == null) {
			tpsIdleAdjustments = getFormat()
					.pack(getDB().queryCommand("TPS Idle Adjustment",
							"Mikuni ECU200"));
		}

		if (longTermLearnValueZoneInitialization == null) {
			longTermLearnValueZoneInitialization = getFormat().pack(
					getDB().queryCommand(
							"Long Term Learn Value Zone Initialization",
							"Mikuni ECU200"));

		}

		if (longTermLearnValueZones == null) {
			longTermLearnValueZones = new SparseArray<byte[]>();
			for (int i = 1; i < 11; i++) {
				byte[] cmd = getDB().queryCommand(
						"Long Term Learn Value Zone_" + Integer.toString(i),
						"Mikuni ECU200");
				cmd = getFormat().pack(cmd);
				longTermLearnValueZones.put(i, cmd);
			}

		}

		if (iscLearnValueInitialization == null) {
			iscLearnValueInitialization = getFormat().pack(
					getDB().queryCommand("ISC Learn Value Initialization",
							"Mikuni ECU200"));
		}
	}
	
	public PowertrainModel getModel() {
		return model;
	}

	public PowertrainECU200(VehicleDB db, Commbox box, PowertrainModel model) {
		super(db, box);
		switch (model) {
		case DCJ_16A:
		case DCJ_16C:
		case DCJ_10:
			getAttribute().klineParity = Attribute.KLineParity.None;
			getAttribute().klineBaudRate = 19200;
			setChannel(ChannelFactory.create(getAttribute(), box,
					ProtocolType.MikuniECU200));
			setFormat(new MikuniECU200Format(getAttribute()));
			break;
		case QM200GY_F:
		case QM200_3D:
		case QM200J_3L:
			getAttribute().klineParity = Attribute.KLineParity.Even;
			getAttribute().klineBaudRate = 19200;
			setChannel(ChannelFactory.create(getAttribute(), box,
					ProtocolType.MikuniECU200));
			setFormat(new MikuniECU200Format(getAttribute()));
			break;
		default:
			throw new DiagException("Unsupport model!");
		}

		this.model = model;

		if (getChannel() == null)
			throw new DiagException("Cannot create channel!!!!");

		initCommands();

		setTroubleCode(new PowertrainTroubleCodeECU200(this));
		setDataStream(new PowertrainDataStreamECU200(this));

	}

	private static PowertrainVersion formatVersion(String hex) {
		PowertrainVersion ver = new PowertrainVersion();

		Charset cs = Charset.forName("US-ASCII");
		ByteBuffer bb = ByteBuffer.allocate(100);
		StringBuilder sb = new StringBuilder();

		sb.append("ECU");

		for (int i = 0; i < 6; i += 2) {
			String temp = hex.substring(i, i + 2);
			byte b = Byte.valueOf(temp, 16).byteValue();
			if (Character.isLetterOrDigit(b)) {
				bb.put(b);
			}
		}

		bb.flip();
		sb.append(cs.decode(bb));

		bb.clear();

		sb.append('-');

		int beginOfSoftware = 18;
		for (int i = 6; i < 16; i += 2) {
			String temp = hex.substring(i, i + 2);
			byte b = Byte.valueOf(temp, 16).byteValue();
			if (Character.isLetterOrDigit(b)) {
				bb.put(b);
			} else {
				beginOfSoftware -= 2;
			}
		}

		bb.flip();
		sb.append(cs.decode(bb));
		bb.clear();

		ver.hardware = sb.toString();

		for (int i = beginOfSoftware; i < (beginOfSoftware + 12); i += 2) {
			String temp = hex.substring(i, i + 2);
			byte b = Byte.valueOf(temp, 16).byteValue();
			if (Character.isLetterOrDigit(b)) {
				bb.put(b);
			}
		}

		bb.flip();
		ver.software = new String(cs.decode(bb).array());
		return ver;
	}

	@Override
	public void channelInit() {
		try {
			getChannel().startCommunicate();
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

	public PowertrainVersion readVersion() {
		try {
			byte[] rData = new byte[100];
			int length = getChannel().sendAndRecv(readECUVersion1, 0,
					readECUVersion1.length, rData);

			System.arraycopy(rData, 0, readECUVersion2, 3, 4);

			length = getChannel().sendAndRecv(readECUVersion2, 0,
					readECUVersion2.length, rData);

			String temp1 = new String(rData, 0, length);
			PowertrainVersion ver = formatVersion(temp1);

			switch (model) {
			case DCJ_16A:
			case DCJ_16C:
			case DCJ_10:
				break;
			case QM200GY_F:
				ver.model = "M16-02";
				break;
			case QM200_3D:
				ver.model = "M16-01";
				break;
			case QM200J_3L:
				ver.model = "M16-03";
				break;
			default:
				throw new DiagException("Unsupport model!");
			}

			return ver;
		} catch (ChannelException e) {
//			e.printStackTrace();
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

	public void tpsIdleSetting() {
		try {
			byte[] rData = new byte[100];
			getChannel().sendAndRecv(engineRevolutions, 0,
					engineRevolutions.length, rData);

			if (rData[0] != '0' || rData[1] != '0' || rData[2] != '0'
					|| rData[3] != '0') {
				throw new DiagException(getDB().queryText(
						"Engine RPM Not Zero", "System"));
			}

			getChannel().sendAndRecv(tpsIdleAdjustments, 0,
					tpsIdleAdjustments.length, rData);

			if (rData[0] != 'A')
				throw new DiagException(getDB().queryText(
						"TPS Idle Setting Fail", "Mikuni"));
		} catch (ChannelException e) {
//			e.printStackTrace();
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

	public void longTermLearnValueZoneInitialization() {
		try {
			byte[] rData = new byte[100];
			getChannel().sendAndRecv(
					longTermLearnValueZoneInitialization, 0,
					longTermLearnValueZoneInitialization.length, rData);

			if (rData[0] != 'A') {
				throw new DiagException(getDB().queryText(
						"Long Term Learn Value Zone Initialization Fail",
						"System"));
			}

			Thread.sleep(Timer.fromSeconds(5).toMilliseconds());

			for (int i = 1; i < 11; i++) {
				byte[] cmd = longTermLearnValueZones.get(i);
				getChannel().sendAndRecv(cmd, 0, cmd.length, rData);

				if (rData[0] != '0' || rData[1] != '0' || rData[2] != '8'
						|| rData[3] != '0') {
					throw new DiagException(getDB().queryText(
							"Long Term Learn Value Zone Initialization Fail",
							"System"));
				}
			}
		} catch (ChannelException e) {
//			e.printStackTrace();
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		} catch (InterruptedException e) {
//			e.printStackTrace();
			throw new DiagException("Interrupted!");
		}
	}

	public void iscLearnValueInitialization() {
		try {
			byte[] rData = new byte[100];
			getChannel().sendAndRecv(iscLearnValueInitialization, 0,
					iscLearnValueInitialization.length, rData);

			if (rData[0] != 'A') {
				throw new DiagException(getDB().queryText(
						"ISC Learn Value Initialization Fail", "System"));
			}
		} catch (ChannelException e) {
//			e.printStackTrace();
			throw new DiagException(getDB().queryText("Communication Fail",
					"System"));
		}
	}

}
