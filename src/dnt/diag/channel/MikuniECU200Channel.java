package dnt.diag.channel;

import dnt.diag.Timer;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;
import dnt.diag.commbox.CommboxException;

public final class MikuniECU200Channel extends AbstractChannel {

	public MikuniECU200Channel(Attribute attr, Commbox commbox) {
		super(attr, commbox);
	}

	@Override
	public void startHeartbeat(Timer interval, byte[] data, int offset,
			int count) throws ChannelException {
		try {
			getCommbox().setCommTime(Commbox.SETLINKTIME, interval);
			getCommbox().setBuffId(Commbox.LINKBLOCK);
			getCommbox().newBatch();
			getCommbox().sendOutData(data, offset, count);
			getCommbox().endBatch();
			getCommbox().keepLink(true);
		} catch (CommboxException e) {
			throw new ChannelException("Mikuni ECU 200 start heartbeat fail!");
		}
	}

	@Override
	public void stopHeartbeat() throws ChannelException {
		try {
			getCommbox().keepLink(false);
		} catch (CommboxException e) {
			throw new ChannelException("Mikuni ECU200 stop heartbeat fail!");
		}
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

			int pos = 0;
			byte before = 0;

			// Read header 0x48
			while (true) {
				if (getCommbox().readBytes(output, 0, 1) == 1) {
					if (output[0] == 0x48) {
						pos++;
						break;
					}
				} else {
					throw new ChannelException(
							"Mikuni ECU200 read header fail!");
				}
			}

			while (true) {
				if (getCommbox().readBytes(output, pos, 1) == 1) {
					if (before == 0x0D && output[pos] == 0x0A) {
						pos++;
						break;
					}
					before = output[pos];
					pos++;
				} else {
					throw new ChannelException("Mikuni ECU200 read data fail!");
				}
			}

			getCommbox().stopNow(false);
			getCommbox().checkResult(Timer.fromMilliseconds(500));
			getCommbox().delBatch();

			pos -= 3;
			System.arraycopy(output, 1, output, 0, pos);

			return pos;

		} catch (CommboxException e) {
			try {
				getCommbox().delBatch();
			} catch (CommboxException e2) {
			}
			throw new ChannelException("Mikuni ECU200 send and receive fail!");
		}
	}

	@Override
	public void startCommunicate() throws ChannelException {

		try {
			int cmd1 = 0;
			int cmd2 = Commbox.SET_NULL;
			int cmd3 = Commbox.SET_NULL;

			if (getAttribute().klineParity == Attribute.KLineParity.None) {
				cmd1 = Commbox.RS_232 | Commbox.BIT9_MARK | Commbox.SEL_SL
						| Commbox.UN_DB20;
				cmd2 = 0xFF;
				cmd3 = 0x02;
			} else {
				cmd1 = Commbox.RS_232 | Commbox.BIT9_EVEN | Commbox.SEL_SL
						| Commbox.UN_DB20;
				cmd2 = 0xFF;
				cmd3 = 0x03;
			}

			getCommbox().setCommCtrl(
					Commbox.PWC | Commbox.RZFC | Commbox.CK | Commbox.REFC,
					Commbox.SET_NULL);
			getCommbox().setCommLine(Commbox.SK_NO, Commbox.RK1);
			getCommbox().setCommLink(cmd1, cmd2, cmd3);
			getCommbox().setCommBaud(getAttribute().klineBaudRate);
			getCommbox().setCommTime(Commbox.SETBYTETIME,
					Timer.fromMicroseconds(100));
			getCommbox().setCommTime(Commbox.SETWAITTIME,
					Timer.fromMicroseconds(1000));
			getCommbox().setCommTime(Commbox.SETRECBBOUT,
					Timer.fromMicroseconds(400000));
			getCommbox().setCommTime(Commbox.SETRECFROUT,
					Timer.fromMicroseconds(500000));
			getCommbox().setCommTime(Commbox.SETLINKTIME,
					Timer.fromMicroseconds(500000));

			Thread.sleep(1000);
		} catch (CommboxException e) {
			throw new ChannelException(
					"Mikuni ECU200 start communication fail!");
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void setByteTxInterval(Timer interval) throws ChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrameTxInterval(Timer interval) throws ChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setByteRxTimeout(Timer timeout) throws ChannelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrameRxTimeout(Timer timeout) throws ChannelException {
		// TODO Auto-generated method stub
		
	}

}
