package dnt.diag.channel;

import dnt.diag.Timer;
import dnt.diag.Utils;
import dnt.diag.attribute.Attribute;
import dnt.diag.commbox.Commbox;
import dnt.diag.commbox.CommboxException;

public final class ISO9141Channel extends AbstractChannel {

	private int kline;
	private int lline;

	public ISO9141Channel(Attribute attr, Commbox commbox) {
		super(attr, commbox);
		lline = Commbox.SK_NO;
		kline = Commbox.RK_NO;
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
			throw new ChannelException(e.getMessage());
		}
	}

	@Override
	public void stopHeartbeat() throws ChannelException {
		try {
			getCommbox().keepLink(false);
		} catch (CommboxException e) {
			throw new ChannelException(e.getMessage());
		}
	}

	@Override
	public void startCommunicate() throws ChannelException {
		try {
			if (!configLines()) {
				throw new ChannelException("ISO9141 communication line wrong!");
			}

			getCommbox().setCommCtrl(Commbox.PWC | Commbox.RZFC | Commbox.CK,
					Commbox.SET_NULL);
			getCommbox().setCommLine(lline, kline);
			getCommbox().setCommLink(
					Commbox.RS_232 | Commbox.BIT9_MARK | Commbox.SEL_SL
							| Commbox.SET_DB20, Commbox.SET_NULL,
					Commbox.INVERTBYTE);
			getCommbox().setCommBaud(5);
			getCommbox().setCommTime(Commbox.SETBYTETIME,
					Timer.fromMilliseconds(5));
			getCommbox().setCommTime(Commbox.SETWAITTIME,
					Timer.fromMilliseconds(25));
			getCommbox().setCommTime(Commbox.SETRECBBOUT,
					Timer.fromMilliseconds(400));
			getCommbox().setCommTime(Commbox.SETRECFROUT,
					Timer.fromMilliseconds(500));
			getCommbox().setCommTime(Commbox.SETLINKTIME,
					Timer.fromMilliseconds(500));

			Thread.sleep(1000);

			getCommbox().setBuffId(0);
			getCommbox().newBatch();
			getCommbox()
					.sendOutData(Utils.loByte(getAttribute().klineAddrCode));
			getCommbox().setCommLine(
					kline == Commbox.RK_NO ? lline : Commbox.SK_NO, kline);
			getCommbox().runReceive(Commbox.SET55_BAUD);
			getCommbox().runReceive(Commbox.REC_LEN_1);
			getCommbox().turnOverOneByOne();
			getCommbox().runReceive(Commbox.REC_LEN_1);
			getCommbox().turnOverOneByOne();
			getCommbox().runReceive(Commbox.REC_LEN_1);
			getCommbox().endBatch();

			int tempLength = 0;
			byte[] tempBuff = new byte[3];

			getCommbox().runBatch(false);
			tempLength = getCommbox().readData(tempBuff, 0, 3,
					Timer.fromMilliseconds(3));
			if (tempLength != 3)
				throw new ChannelException(
						"ISO9141 start communication read timeout");

			getCommbox().checkResult(Timer.fromMilliseconds(500));
			getCommbox().delBatch();

			getCommbox().setCommTime(Commbox.SETBYTETIME,
					Timer.fromMilliseconds(5));
			getCommbox().setCommTime(Commbox.SETWAITTIME,
					Timer.fromMilliseconds(15));
			getCommbox().setCommTime(Commbox.SETRECBBOUT,
					Timer.fromMilliseconds(80));
			getCommbox().setCommTime(Commbox.SETRECFROUT,
					Timer.fromMilliseconds(200));
		} catch (CommboxException e) {
			try {
				getCommbox().delBatch();
			} catch (CommboxException e2) {
			}
			throw new ChannelException(e.getMessage());
		} catch (InterruptedException e) {
			throw new ChannelException(
					"ISO9141 start communication interrupted!");
		}
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

	private int singleUnpack(byte[] buff, int offset, int length)
			throws ChannelException {
		length--; // data length.
		int checksum = 0;
		for (int i = 0; i < length; i++) {
			checksum += buff[offset + i];
			if (checksum != buff[length]) {
				throw new ChannelException(
						"ISO9141 recv data but checksum error!");
			}
		}

		length -= 3;

		System.arraycopy(buff, offset + 3, buff, offset, length);
		return length;
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
			int j = 3;
			int k = 0;
			int retlen = 0;
			while (true) {
				if (getCommbox().readBytes(output, pos++, 1) != 1)
					break;
			}

			getCommbox().stopNow(false);
			getCommbox().checkResult(Timer.fromMilliseconds(500));
			getCommbox().delBatch();

			if (pos < 5)
				throw new ChannelException("ISO9141 receive data fail!");

			while (j < pos) {
				// Multiple Frame
				if (output[k] == output[j] && (output[k + 1] == output[j + 1])
						&& (output[k + 2] == output[j + 2])) {
					retlen += singleUnpack(output, k, k - j);
					k = j;
				}
				j++;
			}

			// Add last frame or it's a single frame
			retlen += singleUnpack(output, k, j - k);

			return retlen;
		} catch (CommboxException e) {
			throw new ChannelException(e.getMessage());
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
