package dnt.diag.ecu.synerject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import dnt.diag.ProtocolType;
import dnt.diag.attribute.Attribute;
import dnt.diag.channel.ChannelException;
import dnt.diag.channel.ChannelFactory;
import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import dnt.diag.ecu.AbstractECU;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;
import dnt.diag.formats.KWP2KFormat;

public class Powertrain extends AbstractECU {
	public final int TESTER_ID = 0xF1;
	public final int PHYSICAL = 0x11;
	public final int FUNCTIONAL = 0x10;
	public final int OBD_SERVICES = 0x33;
	public final int RETURN_CONTROL_TO_ECU = 0x00;
	public final int REPORT_CURRENT_STATE = 0x01;
	public final int SHORT_TERM_ADJUSTMENTS = 0x07;
	public final int LONG_TERM_ADJUSTMENTS = 0x08;

	protected static byte[] startDiagnosticSession;
	protected static byte[] stopDiagnosticSession;
	protected static byte[] stopCommunication;
	protected static byte[] fastCmd;
	protected static byte[] version;

	static {
		startDiagnosticSession = null;
		stopDiagnosticSession = null;
		stopCommunication = null;
		fastCmd = null;
		version = null;
	}

	private PowertrainModel model;
	protected DataStreamFunction.ActionRead startCommunication;
	protected DataStreamFunction.ActionRead endCommunication;

	private void initCommands() {
		if (startDiagnosticSession == null) {
			startDiagnosticSession = getFormat()
					.pack(getDB().queryCommand("Start DiagnosticSession",
							"Synerject"));
		}

		if (stopDiagnosticSession == null) {
			stopDiagnosticSession = getFormat()
					.pack(getDB().queryCommand("Stop DiagnosticSession",
							"Synerject"));
		}

		if (stopCommunication == null) {
			stopCommunication = getFormat().pack(
					getDB().queryCommand("Stop Communication", "Synerject"));
		}

		if (fastCmd == null) {
			fastCmd = getFormat().pack(
					getDB().queryCommand("Start Communication", "Synerject"));
		}

		if (version == null) {
			version = getFormat().pack(
					getDB().queryCommand("Version", "Synerject"));
		}
	}

	public Powertrain(VehicleDB db, Commbox box, PowertrainModel model) {
		super(db, box);
		this.model = model;

		Attribute attr = getAttribute();
		attr.klineBaudRate = 10416;
		attr.klineSourceAddress = TESTER_ID;
		attr.klineTargetAddress = PHYSICAL;
		attr.kwp2kMsgMode = Attribute.KWP2KMode.Mode8X;
		attr.kwp2kHeartbeatMode = Attribute.KWP2KMode.Mode8X;
		attr.kwp2KStartType = Attribute.KWP2KStartType.Fast;
		attr.kwp2kCurrentMode = Attribute.KWP2KMode.Mode8X;
		attr.klineComLine = 7;

		setChannel(ChannelFactory.create(attr, getCommbox(),
				ProtocolType.ISO14230));
		setFormat(new KWP2KFormat(attr));

		initCommands();

		attr.kwp2kFastCmd = fastCmd;

		if (getChannel() == null) {
			throw new DiagException("Cannot create channel!!!!");
		}

		startCommunication = new DataStreamFunction.ActionRead() {

			@Override
			public void execute() throws ChannelException {
				byte[] rData = new byte[100];
				int length = getChannel().sendAndRecv(startDiagnosticSession,
						0, startDiagnosticSession.length, rData);

				if (length <= 0 || rData[0] != 0x50) {
					throw new DiagException(getDB().queryText(
							"Communication Fail", "System"));
				}
			}

		};

		endCommunication = new DataStreamFunction.ActionRead() {

			@Override
			public void execute() throws ChannelException {
				byte[] rData = new byte[100];
				getChannel().sendAndRecv(stopDiagnosticSession, 0,
						stopDiagnosticSession.length, rData);
				getChannel().sendAndRecv(stopCommunication, 0,
						stopCommunication.length, rData);
			}
		};

		setTroubleCode(new PowertrainTroubleCode(this));
		setDataStream(new PowertrainDataStream(this));
		setActiveTest(new PowertrainActiveTest(this));
	}

	public PowertrainModel getModel() {
		return model;
	}

	public String readVersion() {
		try {
			startCommunication.execute();
			byte[] rData = new byte[100];
			int length = getChannel().sendAndRecv(version, 0, version.length,
					rData);
			endCommunication.execute();

			if (length <= 0 || rData[0] != 0x61) {
				throw new DiagException(getDB().queryText(
						"Read ECU Version Fail", "System"));
			}

			Charset cs = Charset.forName("US-ASCII");
			
			length -= 2;
			ByteBuffer bb = ByteBuffer.allocate(length);

			bb.put(rData, 2, length);
			bb.flip();

			return cs.decode(bb).toString();
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

	@Override
	public void channelInit() {
		try {
			getChannel().startCommunicate();
		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

}
