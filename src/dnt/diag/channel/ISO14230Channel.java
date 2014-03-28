package dnt.diag.channel;

import java.util.HashMap;
import java.util.Map;

import dnt.diag.Timer;
import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;
import dnt.diag.commbox.CommboxException;

public final class ISO14230Channel extends AbstractChannel {

	private int kline;
	private int lline;
	private Map<Attribute.KWP2KStartType, KWP2KStartFunc> startComms;

	private void startCommunicationInit() {
		startComms = new HashMap<Attribute.KWP2KStartType, KWP2KStartFunc>();

		startComms.put(Attribute.KWP2KStartType.Fast, new KWP2KStartFunc() {

			@Override
			public void execute() throws ChannelException {
				try {
					int valueOpen = 0;
					if (getAttribute().klineL)
						valueOpen = Commbox.PWC | Commbox.RZFC | Commbox.CK;
					else
						valueOpen = Commbox.PWC | Commbox.RZFC | Commbox.CK;

					getCommbox().setBuffId(0xFF);

					getCommbox().setCommCtrl(valueOpen, Commbox.SET_NULL);
					getCommbox().setCommLine(kline, lline);
					getCommbox().setCommLink(
							Commbox.RS_232 | Commbox.BIT9_MARK | Commbox.SEL_SL
									| Commbox.UN_DB20, Commbox.SET_NULL,
							Commbox.SET_NULL);
					getCommbox().setCommBaud(getAttribute().klineBaudRate);
					getCommbox().setCommTime(Commbox.SETBYTETIME,
							Timer.fromMilliseconds(5));
					getCommbox().setCommTime(Commbox.SETWAITTIME,
							Timer.fromMilliseconds(0));
					getCommbox().setCommTime(Commbox.SETRECBBOUT,
							Timer.fromMilliseconds(400));
					getCommbox().setCommTime(Commbox.SETRECFROUT,
							Timer.fromMilliseconds(500));
					getCommbox().setCommTime(Commbox.SETLINKTIME,
							Timer.fromMilliseconds(500));

					Thread.sleep(1000);

					getCommbox().setBuffId(0);
					getCommbox().newBatch();
					getCommbox().setLineLevel(Commbox.COMS, Commbox.SET_NULL);
					getCommbox().commboxDelay(Timer.fromMilliseconds(25));
					getCommbox().setLineLevel(Commbox.SET_NULL, Commbox.COMS);
					getCommbox().commboxDelay(Timer.fromMilliseconds(25));
					getCommbox().sendOutData(getAttribute().kwp2kFastCmd);
					getCommbox().runReceive(Commbox.REC_FR);
					getCommbox().endBatch();
					getCommbox().runBatch(false);

					byte[] rData = new byte[100];
					readOneFrame(rData);

					getCommbox().checkResult(Timer.fromMilliseconds(500));
					getCommbox().delBatch();
					getCommbox().setCommTime(Commbox.SETWAITTIME,
							Timer.fromMilliseconds(55));

				} catch (CommboxException e) {
					try {
						getCommbox().delBatch();
					} catch (CommboxException e2) {
					}

					throw new ChannelException(
							"ISO14230 start communication fail!");
				} catch (InterruptedException e) {
					// e.printStackTrace();
					throw new ChannelException("Thread sleep interrupted!");
				}
			}
		});

		startComms.put(Attribute.KWP2KStartType.Addr, new KWP2KStartFunc() {

			@Override
			public void execute() throws ChannelException {
				try {

					getCommbox().setCommCtrl(
							Commbox.PWC | Commbox.REFC | Commbox.RZFC
									| Commbox.CK, Commbox.SET_NULL);
					getCommbox().setCommBaud(5);
					getCommbox().setCommTime(Commbox.SETBYTETIME,
							Timer.fromMilliseconds(5));
					getCommbox().setCommTime(Commbox.SETWAITTIME,
							Timer.fromMilliseconds(12));
					getCommbox().setCommTime(Commbox.SETRECBBOUT,
							Timer.fromMilliseconds(400));
					getCommbox().setCommTime(Commbox.SETRECFROUT,
							Timer.fromMilliseconds(500));
					getCommbox().setCommTime(Commbox.SETLINKTIME,
							Timer.fromMilliseconds(500));

					Thread.sleep(1000);

					getCommbox().setBuffId(0);

					getCommbox().newBatch();
					getCommbox().sendOutData(
							Utils.loByte(getAttribute().klineAddrCode));
					getCommbox().setCommLine(
							(kline == Commbox.RK_NO) ? lline : Commbox.SK_NO,
							kline);
					getCommbox().runReceive(Commbox.SET55_BAUD);
					getCommbox().runReceive(Commbox.REC_LEN_1);
					getCommbox().turnOverOneByOne();
					getCommbox().runReceive(Commbox.REC_LEN_1);
					getCommbox().turnOverOneByOne();
					getCommbox().runReceive(Commbox.REC_LEN_1);
					getCommbox().endBatch();
					getCommbox().runBatch(false);

					byte[] temp = new byte[3];
					getCommbox().readData(temp, 0, temp.length,
							Timer.fromSeconds(5));

					getCommbox().checkResult(Timer.fromSeconds(5));
					getCommbox().delBatch();
					getCommbox().setCommTime(Commbox.SETWAITTIME,
							Timer.fromMilliseconds(55));

					if (temp[2] != 0)
						throw new ChannelException(
								"ISO14230 Addr code data in offset 2 not zero!");

				} catch (CommboxException ex) {
					throw new ChannelException("ISO14230 Addr init fail!");
				} catch (InterruptedException e) {
					throw new ChannelException("Thread interuptted!");
				}
			}
		});
	}

	private int readMode80(byte[] buff) throws ChannelException {
		int pos = 3;

		if (getCommbox().readBytes(buff, pos++, 1) != 1) {
			throw new ChannelException("ISO14230 Read Mode80 Length Fail!");
		}

		int length = (buff[3] & 0xFF);

		if (getCommbox().readBytes(buff, pos, length) != length) {
			throw new ChannelException("ISO14230 Read Mode80 data fail!");
		}

		pos += length;

		if (getCommbox().readBytes(buff, pos, 1) != 1) {
			throw new ChannelException("ISO14230 Read Mode80 checksum fail!");
		}

		System.arraycopy(buff, 4, buff, 0, length);
		return length;
	}

	private int readMode8XCX(byte[] buff) throws ChannelException {
		int length = buff[0] & 0xFF;
		length = (length & 0xC0) == 0xC0 ? length - 0xC0 : length - 0x80;

		int pos = 3;

		if (getCommbox().readBytes(buff, pos, length) != length) {
			throw new ChannelException("ISO14230 Read ModeCX/8X Data Fail!");
		}

		pos += length;

		if (getCommbox().readBytes(buff, pos, 1) != 1) {
			throw new ChannelException("ISO14230 Read ModeCX/8X Checksum Fail!");
		}

		System.arraycopy(buff, 3, buff, 0, length);
		return length;
	}

	private int readMode00(byte[] buff) throws ChannelException {
		int length = buff[1] & 0xFF;

		int pos = 3;

		if (getCommbox().readBytes(buff, pos, length) != length) {
			throw new ChannelException("ISO14230 Mode00 fail!");
		}

		System.arraycopy(buff, 2, buff, 0, length);
		return length;
	}

	private int readModeXX(byte[] buff) throws ChannelException {
		int length = (buff[0] & 0xFF);

		if (getCommbox().readBytes(buff, 3, length - 1) != (length - 1)) {
			throw new ChannelException("ISO14230 Read ModeXX Fail!");
		}

		System.arraycopy(buff, 1, buff, 0, length);

		return length;
	}

	private int readOneFrame(byte[] output) throws ChannelException {
		int len = getCommbox().readBytes(output, 0, 3);

		if (len != 3)
			throw new ChannelException("ISO14230 Read Header Fail!");

		int temp0 = output[0] & 0xFF;
		int temp1 = output[1] & 0xFF;
		int length = 0;

		if (temp1 == getAttribute().klineSourceAddress) {
			if (temp0 == 0x80) {
				length = readMode80(output);
			} else {
				length = readMode8XCX(output);
			}
		} else {
			if (temp0 == 0x00) {
				length = readMode00(output);
			} else {
				length = readModeXX(output);
			}
		}

		return length;
	}

	private boolean configLines() {
		if (getAttribute().klineComLine == 7) {
			lline = Commbox.SK1;
			kline = Commbox.RK1;
		} else {
			return false;
		}

		return true;
	}

	public ISO14230Channel(Attribute attr, Commbox commbox) {
		super(attr, commbox);
		kline = Commbox.SK_NO;
		lline = Commbox.RK_NO;
		startCommunicationInit();
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
			throw new ChannelException("ISO14230 Start Heartbeat fail!");
		}
	}

	@Override
	public void stopHeartbeat() throws ChannelException {
		try {
			getCommbox().keepLink(false);
		} catch (CommboxException e) {
			throw new ChannelException("ISO14230 Stop heartbeat fail!");
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

			int length = readOneFrame(output);

			getCommbox().stopNow(false);
			getCommbox().delBatch();

			return length;
		} catch (CommboxException e) {
			try {
				getCommbox().stopNow(false);
				getCommbox().delBatch();
			} catch (CommboxException e2) {
			}

			throw new ChannelException("ISO14230 Send And Recv Fail!");
		}
	}

	@Override
	public void startCommunicate() throws ChannelException {
		if (!configLines())
			throw new ChannelException("ISO14230 unsupported com line!");
		startComms.get(getAttribute().kwp2KStartType).execute();
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
