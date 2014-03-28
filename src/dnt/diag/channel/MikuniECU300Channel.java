package dnt.diag.channel;

import dnt.diag.Timer;
import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;
import dnt.diag.commbox.CommboxException;

public class MikuniECU300Channel extends AbstractChannel {

	private Timer byteTime;
	private Timer waitTime;
	private Timer recbbout;
	private Timer recfrout;

	public MikuniECU300Channel(Attribute attr, Commbox commbox) {
		super(attr, commbox);
	}

	@Override
	public void startHeartbeat(Timer interval, byte[] data, int offset,
			int count) throws ChannelException {
		try {
			getCommbox().setCommTime(Commbox.SETLINKTIME, interval);
			getCommbox().setBuffId(Utils.loByte(Commbox.LINKBLOCK));
			getCommbox().newBatch();
			getCommbox().sendOutData(data, offset, count);
			getCommbox().endBatch();
			getCommbox().keepLink(true);
		} catch (CommboxException e) {
			throw new ChannelException("Mikuni ECU300 Start Heartbeat fail!");
		}
	}

	@Override
	public void stopHeartbeat() throws ChannelException {
		try {
			getCommbox().keepLink(false);
		} catch (CommboxException e) {
			throw new ChannelException("Mikuni ECU300 Stop heartbeat fail!");
		}
	}

	@Override
	public void setByteTxInterval(Timer interval) throws ChannelException {
		byteTime = interval;
	}
	
	@Override
	public void setFrameTxInterval(Timer interval) throws ChannelException {
		waitTime = interval;
	}
	
	@Override
	public void setByteRxTimeout(Timer timeout) throws ChannelException {
		recbbout = timeout;
	}
	
	@Override
	public void setFrameRxTimeout(Timer timeout) throws ChannelException {
		recfrout = timeout;
	}

	@Override
	public int sendAndRecv(byte[] sData, int sOffset, int sCount, byte[] output)
			throws ChannelException {
		try {
			getCommbox().setBuffId(0);
			getCommbox().newBatch();
			getCommbox().sendOutData(sData, sOffset, sCount);
			getCommbox().runReceive(Commbox.RECEIVE);
			getCommbox().endBatch();
			getCommbox().runBatch(false);

			if (getCommbox().readBytes(output, 0, 2) != 2) {
				throw new ChannelException("Mikuni ECU300 read header fail!");
			}

			int length = ((output[0] & 0xFF) << 8) | (output[1] & 0xFF) - 1; // Skip
																				// checksum

			if (getCommbox().readBytes(output, 2, length) != length) {
				throw new ChannelException("Mikuni ECU300 read data fail!");
			}

			if (getCommbox().readBytes(output, length + 2, 1) != 1) {
				throw new ChannelException("Mikuni ECU300 read checksum fail!");
			}

			getCommbox().stopNow(false);
			getCommbox().checkResult(Timer.fromMilliseconds(500));
			getCommbox().delBatch();

			System.arraycopy(output, 2, output, 0, length);
			return length;

		} catch (CommboxException e) {
			try {
				getCommbox().delBatch();
			} catch (CommboxException e2) {
			}

			throw new ChannelException("Mikuni ECU300 send and receive fail!");
		}
	}

	@Override
	public void startCommunicate() throws ChannelException {
		try {
			int cmd1 = 0;
			int cmd2 = Commbox.SET_NULL;
			int cmd3 = Commbox.SET_NULL;

			cmd1 = Commbox.RS_232 | Commbox.BIT9_MARK | Commbox.SEL_SL
					| Commbox.UN_DB20;
			cmd2 = 0xFF;
			cmd3 = 0x02;
			// cmd1 = Commbox.RS_232 | Commbox.BIT9_EVEN | Commbox.SEL_SL
			// | Commbox.UN_DB20;
			// cmd2 = 0xFF;
			// cmd3 = 0x03;

			getCommbox().setCommCtrl(Commbox.PWC | Commbox.RZFC | Commbox.CK,
					Commbox.SET_NULL);
			getCommbox().setCommLine(Commbox.SK_NO, Commbox.RK1);
			getCommbox().setCommLink(cmd1, cmd2, cmd3);
			getCommbox().setCommBaud(getAttribute().klineBaudRate);
			getCommbox().setCommTime(Commbox.SETBYTETIME, byteTime);
			getCommbox().setCommTime(Commbox.SETWAITTIME, waitTime);
			getCommbox().setCommTime(Commbox.SETRECBBOUT, recbbout);
			getCommbox().setCommTime(Commbox.SETRECFROUT, recfrout);
			getCommbox().setCommTime(Commbox.SETLINKTIME,
					Timer.fromMilliseconds(500));

			Thread.sleep(1000);
		} catch (CommboxException e) {
			throw new ChannelException(
					"Mikuni ECU300 Start communication fail!");
		} catch (InterruptedException e) {
		}
	}

}
