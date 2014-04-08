package dnt.diag.commbox;

import java.io.IOException;
import java.util.Random;

import dnt.diag.Timer;
import dnt.diag.Utils;
import dnt.diag.io.SerialPort;

public final class Commbox {
	public static final int BOXINFO_LEN = 12;
	public static final int MAXPORT_NUM = 4;
	public static final int MAXBUFF_NUM = 4;
	public static final int MAXBUFF_LEN = 0xA8;
	public static final int LINKBLOCK = 0x40;

	// 批处理执行次数
	public static final int RUN_ONCE = 0x00;
	public static final int RUN_MORE = 0x01;

	// 通讯校验和方式
	public static final int CHECK_SUM = 0x01;
	public static final int CHECK_REVSUM = 0x02;
	public static final int CHECK_CRC = 0x03;

	// /////////////////////////////////////////////////////////////////////////////
	// 通讯口 PORT
	// /////////////////////////////////////////////////////////////////////////////
	public static final int DH = 0x80; // 高电平输出,1为关闭,0为打开
	public static final int DL2 = 0x40; // 低电平输出,1为关闭,0为打开,正逻辑发送通讯线
	public static final int DL1 = 0x20; // 低电平输出,1为关闭,0为打开,正逻辑发送通讯线,带接受控制
	public static final int DL0 = 0x10; // 低电平输出,1为关闭,0为打开,正逻辑发送通讯线,带接受控制
	public static final int PWMS = 0x08; // PWM发送线
	public static final int PWMR = 0x04;
	public static final int COMS = 0x02; // 标准发送通讯线路
	public static final int COMR = 0x01;
	public static final int SET_NULL = 0x00; // 不选择任何设置

	// /////////////////////////////////////////////////////////////////////////////
	// 通讯物理控制口
	// /////////////////////////////////////////////////////////////////////////////
	public static final int PWC = 0x80; // 通讯电平控制,1为5伏,0为12伏
	public static final int REFC = 0x40; // 通讯比较电平控制,1为通讯电平1/5,0为比较电平控制1/2
	public static final int CK = 0x20; // K线控制开关,1为双线通讯,0为单线通讯
	public static final int SZFC = 0x10; // 发送逻辑控制,1为负逻辑,0为正逻辑
	public static final int RZFC = 0x08; // 接受逻辑控制,1为负逻辑,0为正逻辑
	public static final int DLC0 = 0x04; // DLC1接受控制,1为接受关闭,0为接受打开
	public static final int DLC1 = 0x02; // DLC0接受控制,1为接受关闭,0为接受打开
	public static final int SLC = 0x01; // 线选地址锁存器控制线(待用)
	public static final int CLOSEALL = 0x08; // 关闭所有发送口线，和接受口线

	// /////////////////////////////////////////////////////////////////////////////
	// 通讯控制字1设定
	// /////////////////////////////////////////////////////////////////////////////
	public static final int RS_232 = 0x00;
	public static final int EXRS_232 = 0x20;
	public static final int SET_VPW = 0x40;
	public static final int SET_PWM = 0x60;
	public static final int BIT9_SPACE = 0x00;
	public static final int BIT9_MARK = 0x01;
	public static final int BIT9_EVEN = 0x02;
	public static final int BIT9_ODD = 0x03;
	public static final int SEL_SL = 0x00;
	public static final int SEL_DL0 = 0x08;
	public static final int SEL_DL1 = 0x10;
	public static final int SEL_DL2 = 0x18;
	public static final int SET_DB20 = 0x04;
	public static final int UN_DB20 = 0x00;

	// /////////////////////////////////////////////////////////////////////////////
	// 通讯控制字3设定
	// /////////////////////////////////////////////////////////////////////////////
	public static final int ONEBYONE = 0x80;
	public static final int INVERTBYTE = 0x40;
	public static final int ORIGNALBYTE = 0x00;

	// /////////////////////////////////////////////////////////////////////////////
	// 接受命令类型定义
	// /////////////////////////////////////////////////////////////////////////////
	public static final int WR_DATA = 0x00;
	public static final int WR_LINK = 0xFF;
	public static final int STOP_REC = 0x04;
	public static final int STOP_EXECUTE = 0x08;
	public static final int SET_UPBAUD = 0x0C;
	public static final int UP_9600BPS = 0x00;
	public static final int UP_19200BPS = 0x01;
	public static final int UP_38400BPS = 0x02;
	public static final int UP_57600BPS = 0x03;
	public static final int UP_115200BPS = 0x04;
	public static final int RESET = 0x10;
	public static final int GET_CPU = 0x14;
	public static final int GET_TIME = 0x18;
	public static final int GET_SET = 0x1C;
	public static final int GET_LINK = 0x20;
	public static final int GET_BUF = 0x24;
	public static final int GET_CMD = 0x28;
	public static final int GET_PORT = 0x2C;
	public static final int GET_BOXID = 0x30;
	public static final int DO_BAT_C = 0x34;
	public static final int DO_BAT_CN = 0x38;
	public static final int DO_BAT_L = 0x3C;
	public static final int DO_BAT_LN = 0x40;
	public static final int SET55_BAUD = 0x44;
	public static final int SET_ONEBYONE = 0x48;
	public static final int SET_BAUD = 0x4C;
	public static final int RUN_LINK = 0x50;
	public static final int STOP_LINK = 0x54;
	public static final int CLEAR_LINK = 0x58;
	public static final int GET_PORT1 = 0x5C;
	public static final int SEND_DATA = 0x60;
	public static final int SET_CTRL = 0x64;
	public static final int SET_PORT0 = 0x68;
	public static final int SET_PORT1 = 0x6C;
	public static final int SET_PORT2 = 0x70;
	public static final int SET_PORT3 = 0x74;
	public static final int DELAYSHORT = 0x78;
	public static final int DELAYTIME = 0x7C;
	public static final int DELAYDWORD = 0x80;
	public static final int SETBYTETIME = 0x88;
	public static final int SETVPWSTART = 0x08; // 最终要将SETVPWSTART转换成SETBYTETIME
	public static final int SETWAITTIME = 0x8C;
	public static final int SETLINKTIME = 0x90;
	public static final int SETRECBBOUT = 0x94;
	public static final int SETRECFROUT = 0x98;
	public static final int SETVPWRECS = 0x14; // 最终要将SETVPWRECS转换成SETRECBBOUT

	public static final int COPY_BYTE = 0x9C;
	public static final int UPDATE_BYTE = 0xA0;
	public static final int INC_BYTE = 0xA4;
	public static final int DEC_BYTE = 0xA8;
	public static final int ADD_BYTE = 0xAC;
	public static final int SUB_BYTE = 0xB0;
	public static final int INVERT_BYTE = 0xB4;
	public static final int REC_FR = 0xE0;
	public static final int REC_LEN_1 = 0xE1;
	public static final int REC_LEN_2 = 0xE2;
	public static final int REC_LEN_3 = 0xE3;
	public static final int REC_LEN_4 = 0xE4;
	public static final int REC_LEN_5 = 0xE5;
	public static final int REC_LEN_6 = 0xE6;
	public static final int REC_LEN_7 = 0xE7;
	public static final int REC_LEN_8 = 0xE8;
	public static final int REC_LEN_9 = 0xE9;
	public static final int REC_LEN_10 = 0xEA;
	public static final int REC_LEN_11 = 0xEB;
	public static final int REC_LEN_12 = 0xEC;
	public static final int REC_LEN_13 = 0xED;
	public static final int REC_LEN_14 = 0xEE;
	public static final int REC_LEN_15 = 0xEF;
	public static final int RECEIVE = 0xF0;
	public static final int RECV_ERR = 0xAA; // 接收错误
	public static final int RECV_OK = 0x55; // 接收正确
	public static final int BUSY = 0xBB; // 开始执行
	public static final int READY = 0xDD; // 执行结束
	public static final int ERROR = 0xEE; // 执行错误

	// RF多对一的设定接口,最多16个
	public static final int RF_RESET = 0xD0;
	public static final int RF_SETDTR_L = 0xD1;
	public static final int RF_SETDTR_H = 0xD2;
	public static final int RF_SET_BAUD = 0xD3;
	public static final int RF_SET_ADDR = 0xD8;
	public static final int COMMBOXID_ERR = 1;
	public static final int DISCONNECT_COMM = 2;
	public static final int DISCONNECT_COMMBOX = 3;
	public static final int OTHER_ERROR = 4;

	// 錯誤標識
	public static final int ERR_OPEN = 0x01; // OpenComm() 失敗
	public static final int ERR_CHECK = 0x02; // CheckEcm() 失敗

	// 接頭標識定義
	public static final int OBDII_16 = 0x00;
	public static final int UNIVERSAL_3 = 0x01;
	public static final int BENZ_38 = 0x02;
	public static final int BMW_20 = 0x03;
	public static final int AUDI_4 = 0x04;
	public static final int FIAT_3 = 0x05;
	public static final int CITROEN_2 = 0x06;
	public static final int CHRYSLER_6 = 0x07;
	public static final int TOYOTA_17R = 0x20;
	public static final int TOYOTA_17F = 0x21;
	public static final int HONDA_3 = 0x22;
	public static final int MITSUBISHI = 0x23;
	public static final int HYUNDAI = 0x23;
	public static final int NISSAN = 0x24;
	public static final int SUZUKI_3 = 0x25;
	public static final int DAIHATSU_4 = 0x26;
	public static final int ISUZU_3 = 0x27;
	public static final int CANBUS_16 = 0x28;
	public static final int GM_12 = 0x29;
	public static final int KIA_20 = 0x30;

	// 常量定義
	public static final int TRYTIMES = 3;

	// 通訊通道定義
	public static final int SK0 = 0;
	public static final int SK1 = 1;
	public static final int SK2 = 2;
	public static final int SK3 = 3;
	public static final int SK4 = 4;
	public static final int SK5 = 5;
	public static final int SK6 = 6;
	public static final int SK7 = 7;
	public static final int SK_NO = 0xFF;
	public static final int RK0 = 0;
	public static final int RK1 = 1;
	public static final int RK2 = 2;
	public static final int RK3 = 3;
	public static final int RK4 = 4;
	public static final int RK5 = 5;
	public static final int RK6 = 6;
	public static final int RK7 = 7;
	public static final int RK_NO = 0xFF;

	// 協議常量標誌定義
	public static final int NO_PACK = 0x80; // 發送的命令不需要打包
	public static final int UN_PACK = 0x08; // 接收到的數據解包處理
	public static final int MFR_1 = 0x00;
	public static final int MFR_2 = 0x02;
	public static final int MFR_3 = 0x03;
	public static final int MFR_4 = 0x04;
	public static final int MFR_5 = 0x05;
	public static final int MFR_6 = 0x06;
	public static final int MFR_7 = 0x07;
	public static final int MFR_N = 0x01;

	private static int[] password;

	static {
		password = new int[] { 0x0C, 0x22, 0x17, 0x41, 0x57, 0x2D, 0x43, 0x17,
				0x2D, 0x4D };
	}

	private int timeUnit; // 1/10000 seconds
	private int timeBaseDB; // standard time times
	private int timeExternDB; // expand time times
	private byte[] port;
	private byte[] buf;
	private int bufPos;
	private boolean isLink; // is heartbeat block
	private int runFlag;
//	private int startPos;
	private int boxVer;
	private boolean isOpen;
	private SerialPort serialPort;
	private boolean isDB20;
	private boolean isDoNow;
//	private Timer reqByteToByte;
//	private Timer reqWaitTime;
//	private Timer resByteToByte;
	private Timer resWaitTime;
	private int buffId;
	private byte[] sendCmdData;
	private byte[] getCmdDataCS;
	private byte[] doCmdData;
	private byte[] initCheckBoxBuff;
	Random initBoxRnd;
	private byte[] ctrlWord;
	private byte[] timeBuff;

	public Commbox() {
		timeUnit = 0;
		timeBaseDB = 0;
		timeExternDB = 0;
		port = new byte[MAXPORT_NUM];
		buf = new byte[MAXBUFF_LEN];
		bufPos = 0;
		isLink = false;
		runFlag = 0;
//		startPos = 0;
		boxVer = 0;
		isOpen = false;
		serialPort = new SerialPort();
		isDB20 = false;
		isDoNow = true;
//		reqByteToByte = Timer.fromMilliseconds(0);
//		reqWaitTime = Timer.fromMilliseconds(0);
//		resByteToByte = Timer.fromMilliseconds(0);
		resWaitTime = Timer.fromMilliseconds(0);
		buffId = 0;
		sendCmdData = new byte[256];
		getCmdDataCS = new byte[1];
		doCmdData = new byte[256];
		initCheckBoxBuff = new byte[32];
		initBoxRnd = new Random();
		ctrlWord = new byte[3];
		timeBuff = new byte[2];
	}

	public void setBuffId(int id) {
		buffId = id;
	}

	private void getLinkTime(int type, Timer time) {
		switch (type) {
		case SETBYTETIME:
//			reqByteToByte = time;
			break;
		case SETWAITTIME:
//			reqWaitTime = time;
			break;
		case SETRECBBOUT:
//			resByteToByte = time;
			break;
		case SETRECFROUT:
			resWaitTime = time;
			break;
		}
	}

	private boolean checkSend() {
		try {
			int b = 0;
			serialPort.setReadTimeout(Timer.fromMilliseconds(200));

			b = serialPort.readByte();
			if (b != RECV_OK)
				return false;

			return true;
		} catch (IOException e) {
			return false;
		}

	}

	private boolean sendCmd(int cmd, byte[] buffer, int offset, int count) {
		int cs = cmd;
		int pos = 0;
//		byte[] sendCmdData = new byte[count + 2];

		sendCmdData[pos++] = Utils.loByte(cmd + runFlag);
		if (buffer != null) {
			for (int i = 0; i < count; i++) {
				cs += buffer[offset + i] & 0xFF;
			}
			System.arraycopy(buffer, offset, sendCmdData, pos, count);
			pos += count;
		}

		sendCmdData[pos++] = Utils.loByte(cs);

		for (int i = 0; i < 3; i++) {
			try {
				checkIdle();
				serialPort.write(sendCmdData, 0, pos);
			} catch (CommboxException e) {
				continue;
			} catch (IOException e) {
				continue;
			}

			if (checkSend())
				return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private boolean sendCmd(int cmd, byte... buffer) {
		return sendCmd(cmd, buffer, 0, buffer.length);
	}

	private boolean sendCmd(int cmd) {
		return sendCmd(cmd, null, 0, 0);
	}

	private int recvBytes(byte[] buff, int offset, int count) {
		return readData(buff, offset, count, Timer.fromMilliseconds(500));
	}

	private boolean getCmdData(byte[] buff, int offset, int maxlen) {
		if (recvBytes(buff, 0, 1) != 1)
			return false;
		if (recvBytes(buff, 1, 1) != 1)
			return false;
		int len = buff[1] & 0xFF;
		if (len > maxlen) {
			len = maxlen;
		}
		if (recvBytes(buff, 0, len) != len)
			return false;
		
		getCmdDataCS[0] = 0;
		if (recvBytes(getCmdDataCS, 0, 1) != 1)
			return false;
		return len > 0;
	}

	private boolean doCmd(int cmd, byte[] buffer, int offset, int count) {
//		startPos = 0;
		int pos = 0;
		if (cmd != WR_DATA && cmd != SEND_DATA)
			cmd |= count; // 加上长度位
		if (isDoNow) {
			// 发送到BOX执行
			switch (cmd) {
			case WR_DATA:
				if (count == 0)
					return false;
//				doCmdData = new byte[2 + count];
				if (isLink)
					doCmdData[pos++] = Utils.loByte(0xFF); // 写链路保持
				else
					doCmdData[pos++] = 0x00; // 写通讯命令
				doCmdData[pos++] = Utils.loByte(count);
				System.arraycopy(buffer, offset, doCmdData, pos, count);
				pos += count;
				return sendCmd(WR_DATA, doCmdData, 0, pos);
			case SEND_DATA:
				if (count == 0)
					return false;
//				doCmdData = new byte[4 + count];
				doCmdData[pos++] = 0; // 写入位置
				doCmdData[pos++] = Utils.loByte(count + 2); // 数据包长度
				doCmdData[pos++] = Utils.loByte(SEND_DATA); // 命令
				doCmdData[pos++] = Utils.loByte(count - 1); // 命令长度-1
				System.arraycopy(buffer, offset, doCmdData, pos, count);
				pos += count;
				if (!sendCmd(WR_DATA, doCmdData, 0, pos))
					return false;

				return sendCmd(DO_BAT_C);
			default:
				return sendCmd(cmd, buffer, offset, count);
			}
		} else {
			// 写命令到缓冲区
			buf[bufPos++] = Utils.loByte(cmd);
			if (cmd == SEND_DATA)
				buf[bufPos++] = Utils.loByte(count - 1);
//			startPos = pos;
			if (count > 0) {
				System.arraycopy(buffer, offset, buf, bufPos, count);
				bufPos += count;
			}
			return true;
		}
	}

	private boolean doCmd(int cmd) {
		return doCmd(cmd, null, 0, 0);
	}

	private boolean doSet(int cmd, byte[] buffer, int offset, int count) {
		boolean result = false;
		try {
			result = doCmd(cmd, buffer, offset, count);
			if (result && isDoNow)
				checkResult(Timer.fromMilliseconds(150));
		} catch (CommboxException e) {
			result = false;
		}
		return result;
	}

	private boolean doSet(int cmd) {
		return doSet(cmd, null, 0, 0);
	}

	private boolean doSet(int cmd, byte... buffer) {
		return doSet(cmd, buffer, 0, buffer.length);
	}

	private boolean initBox() {
		isDoNow = true;

		int i;
		for (i = 1; i < 4; i++)
			initCheckBoxBuff[i] = Utils.loByte(initBoxRnd.nextInt());

		int run = 0;
		for (i = 0; i < password.length; i++)
			run += password[i] ^ (initCheckBoxBuff[i % 3 + 1] & 0xFF);

		run = run & 0xFF;
		if (run == 0)
			run = 0x55;

		if (!doCmd(GET_CPU, initCheckBoxBuff, 1, 3))
			return false;

		if (!getCmdData(initCheckBoxBuff, 0, 32))
			return false;

		runFlag = 0;
		timeUnit = 0;

		for (i = 0; i < 3; i++)
			timeUnit = timeUnit * 256 + (initCheckBoxBuff[i] & 0xFF);
		timeBaseDB = initCheckBoxBuff[i++] & 0xFF;
		timeExternDB = initCheckBoxBuff[i++] & 0xFF;

		for (i = 0; i < MAXPORT_NUM; i++)
			port[i] = Utils.loByte(0xFF);
		bufPos = 0;
		isDB20 = false;
		return true;
	}

	private boolean checkBox() {
		if (!doCmd(GET_BOXID))
			return false;
		if (!getCmdData(initCheckBoxBuff, 0, 32))
			return false;
		boxVer = (initCheckBoxBuff[10] << 8) | initCheckBoxBuff[11];
		return true;
	}

	@SuppressWarnings("unused")
	private boolean updateBuff(int type, int addr, int data) {
		byte[] buff = new byte[3];
		int len = 0;
		buff[0] = Utils.loByte(addr);
		buff[1] = Utils.loByte(data);

		switch (type) {
		case INC_BYTE:
		case DEC_BYTE:
		case INVERT_BYTE:
			len = 1;
			break;
		case UPDATE_BYTE:
		case ADD_BYTE:
		case SUB_BYTE:
			len = 2;
			break;
		case COPY_BYTE:
			len = 3;
			break;
		}

		return doSet(type, buff, 0, len);
	}

	@SuppressWarnings("unused")
	private boolean copyBuff(int dest, int src, int len) {
		byte[] buff = new byte[3];
		buff[0] = Utils.loByte(dest);
		buff[1] = Utils.loByte(src);
		buff[2] = Utils.loByte(len);
		return doSet(COPY_BYTE, buff);
	}

	public void clear() {
		try {
			serialPort.discardInBuffer();
			serialPort.discardOutBuffer();
		} catch (IOException e) {
		}
	}

	private boolean reset() {
		try {
			stopNow(true);
		} catch (CommboxException e) {
		}
		clear();
		for (int i = 0; i < MAXPORT_NUM; i++)
			port[i] = Utils.loByte(0xFF);
		return doCmd(RESET);
	}

	public int getBoxVer() {
		return boxVer;
	}

	public void checkIdle() throws CommboxException {
		int avail;
		try {
			avail = serialPort.getBytesToRead();
			if (avail > 20) {
				clear();
				return;
			}

			int b = READY;
			serialPort.setReadTimeout(Timer.fromMilliseconds(200));
			while ((avail = serialPort.getBytesToRead()) != 0) {
				b = serialPort.readByte();
			}

			if (b == READY || b == ERROR)
				return;
		} catch (IOException e) {
			throw new CommboxException("CheckIdle fail!");
		}
	}

	public void checkResult(Timer time) throws CommboxException {
		try {
			serialPort.setReadTimeout(time);
			int rb = 0;
			if ((rb = serialPort.readByte()) != -1) {
				if (rb == READY || rb == ERROR) {
					clear();
					return;
				}
			}
		} catch (IOException e) {
		} 

		throw new CommboxException("CheckResult fail!");
	}

	public int readData(byte[] buffer, int offset, int count, Timer time) {
		try {
			serialPort.setReadTimeout(time);

			int len = serialPort.read(buffer, offset, count);
			if (len < count) {
				int avail = serialPort.getBytesToRead();
				if (avail > 0) {
					if (avail <= (count - len)) {
						len += serialPort.read(buffer, offset + len, avail);
					} else {
						len += serialPort.read(buffer, offset + len, count
								- len);
					}
				}
			}
			return len;
		} catch (IOException e) {
		}

		return 0;
	}

	public void stopNow(boolean isStop) throws CommboxException {
		int cmd = isStop ? STOP_EXECUTE : STOP_REC;
		for (int i = 0; i < 3; i++) {
			try {
				serialPort.write(Utils.loByte(cmd));
				if (checkSend()) {
					if (isStop) {
						checkResult(Timer.fromMilliseconds(200));
						return;
					}
					return;
				}
			} catch (CommboxException e) {
			} catch (IOException e) {
			}
		}
		throw new CommboxException("StopNow fail!");
	}

	public void newBatch() throws CommboxException {
		bufPos = 0;
		isLink = (buffId == LINKBLOCK ? true : false);
		isDoNow = false;
	}

	public void delBatch() throws CommboxException {
		isDoNow = true;
		bufPos = 0;
	}

	public void endBatch() throws CommboxException {
		int i = 0;
		isDoNow = true;
		buf[bufPos++] = 0; // 命令块以0x00标记结束
		if (isLink) {
			// 修改UpdateBuff使用到的地址
			while (buf[i] != 0) {
				int tmp = MAXBUFF_LEN - bufPos;
				switch (buf[i] & 0xFC) {
				case COPY_BYTE:
					buf[i + 3] = Utils.loByte((buf[i + 3] & 0xFF) + tmp);
					buf[i + 2] = Utils.loByte((buf[i + 2] & 0xFF) + tmp);
					buf[i + 1] = Utils.loByte((buf[i + 1] & 0xFF) + tmp);
					break;
				case SUB_BYTE:
					buf[i + 2] = Utils.loByte((buf[i + 2] & 0xFF) + tmp);
					buf[i + 1] = Utils.loByte((buf[i + 1] & 0xFF) + tmp);
					break;
				case UPDATE_BYTE:
				case INVERT_BYTE:
				case ADD_BYTE:
				case DEC_BYTE:
				case INC_BYTE:
					buf[i + 1] = Utils.loByte((buf[i + 1] & 0xFF) + tmp);
					break;
				}

				if ((buf[i] & 0xFF) == SEND_DATA)
					i += 1 + (buf[i + 1] & 0xFF) + 1 + 1;
				else if ((buf[i] & 0xFF) >= REC_LEN_1
						&& (buf[i] & 0xFF) <= REC_LEN_15)
					i += 1; // 特殊
				else
					i += (buf[i] & 0x03) + 1;
			}
		}

		if (!doCmd(WR_DATA, buf, 0, bufPos))
			throw new CommboxException("EndBatch fail!");
	}

	public void setLineLevel(int valueLow, int valueHigh)
			throws CommboxException {
		// 设定端口1
		port[1] &= (byte) (~valueLow);
		port[1] |= (byte) valueHigh;
		if (!doSet(SET_PORT1, port[1]))
			throw new CommboxException("SetLineLevel fail!");
	}

	public void setCommCtrl(int valueOpen, int valueClose)
			throws CommboxException {
		// 设定端口2
		port[2] &= (byte) (~valueOpen);
		port[2] |= (byte) valueClose;
		if (!doSet(SET_PORT2, port[2]))
			throw new CommboxException("SetCommCtrl fail!");
	}

	public void setCommLine(int sendLine, int recvLine) throws CommboxException {
		// 设定端口0
		if (sendLine > 7)
			sendLine = 0x0F;
		if (recvLine > 7)
			recvLine = 0x0F;
		port[0] = (byte) (sendLine | (recvLine << 4));
		if (!doSet(SET_PORT0, port[0]))
			throw new CommboxException("SetCommLine fail!");
	}

	public void turnOverOneByOne() throws CommboxException {
		// 将原有的接受一个发送一个的标志翻转
		if (!doSet(SET_ONEBYONE))
			throw new CommboxException("TurnOverOneByOne fail!");
	}

	public void keepLink(boolean isRunLink) throws CommboxException {
		if (!doSet(isRunLink ? RUN_LINK : STOP_LINK))
			throw new CommboxException("KeepLink fail!");
	}

	public void setCommLink(int ctrlWord1, int ctrlWord2, int ctrlWord3)
			throws CommboxException {
		int modeControl = ctrlWord1 & 0xE0;
		int length = 3;
		ctrlWord[0] = Utils.loByte(ctrlWord1);

		if ((ctrlWord1 & 0x04) != 0)
			isDB20 = true;
		else
			isDB20 = false;

		if (modeControl == SET_VPW || modeControl == SET_PWM) {
			if (!doSet(SET_CTRL, ctrlWord[0]))
				throw new CommboxException("SetCommLink fail!");
		}

		ctrlWord[1] = Utils.loByte(ctrlWord2);
		ctrlWord[2] = Utils.loByte(ctrlWord3);
		if (ctrlWord3 == 0) {
			length--;
			if (ctrlWord2 == 0) {
				length--;
			}
		}

		if (modeControl == EXRS_232 && length < 2)
			throw new CommboxException("SetCommLink fail!");

		if (!doSet(SET_CTRL, ctrlWord, 0, length))
			throw new CommboxException("SetCommLink fail!");
	}

	public void setCommBaud(double baud) throws CommboxException {
		double instructNum = 1000000000000.0 / (timeUnit * baud);
		if (isDB20)
			instructNum /= 20;
		instructNum += 0.5;
		if (instructNum > 65535 || instructNum < 10)
			throw new CommboxException("SetCommBaud fail!");
		timeBuff[0] = Utils.hiByte((long) instructNum);
		timeBuff[1] = Utils.loByte((long) instructNum);

		if (timeBuff[0] == 0) {
			if (!doSet(SET_BAUD, timeBuff[1]))
				throw new CommboxException("SetCommBaud fail!");
		} else {
			if (!doSet(SET_BAUD, timeBuff))
				throw new CommboxException("SetCommBaud fail!");
		}
	}

	public void setCommTime(int type, Timer time) throws CommboxException {
		getLinkTime(type, time);

		long microTime = time.toMicroseconds();
		if (type == SETVPWSTART || type == SETVPWRECS) {
			if (type == SETVPWRECS)
				microTime = (microTime * 2) / 3;
			type = type + (SETBYTETIME & 0xF0);
			microTime = (long) ((microTime * 1000000.0) / timeUnit);
		} else {
			microTime = (long) ((microTime * 1000000.0) / (timeBaseDB * timeUnit));
		}
		timeBuff[0] = Utils.hiByte(microTime);
		timeBuff[1] = Utils.loByte(microTime);

		if (timeBuff[0] == 0) {
			if (!doSet(type, timeBuff[1]))
				throw new CommboxException("SetCommTime fail!");
		} else {
			if (!doSet(type, timeBuff))
				throw new CommboxException("SetCommTime fail!");
		}
	}

	public void runReceive(int type) throws CommboxException {
		if (type == GET_PORT1)
			isDB20 = false;
		if (!doCmd(type))
			throw new CommboxException("RunReceive fail!");
	}

	public void commboxDelay(Timer time) throws CommboxException {
		int delayWord = DELAYSHORT;
		long microTime = (long) (time.toMicroseconds() / (timeUnit / 1000000.0));

		if (microTime == 0)
			throw new CommboxException("CommboxDelay fail!");
		if (microTime > 65535) {
			microTime = microTime / timeBaseDB;
			if (microTime > 65535) {
				microTime = (microTime * timeBaseDB) / timeExternDB;
				if (microTime > 65535)
					throw new CommboxException("CommboxDelay fail!");
				delayWord = DELAYDWORD;
			} else {
				delayWord = DELAYTIME;
			}
		}

		timeBuff[0] = Utils.hiByte(microTime);
		timeBuff[1] = Utils.loByte(microTime);

		if (timeBuff[0] == 0) {
			if (!doSet(delayWord, timeBuff[1]))
				throw new CommboxException("CommboxDelay fail!");
		} else {
			if (!doSet(delayWord, timeBuff))
				throw new CommboxException("CommboxDelay fail!");
		}
	}

	public void sendOutData(byte... bs) throws CommboxException {
		sendOutData(bs, 0, bs.length);
	}

	public void sendOutData(byte[] buff, int offset, int count)
			throws CommboxException {
		if (!doSet(SEND_DATA, buff, offset, count))
			throw new CommboxException("SendOutData fail!");
	}

	public void runBatch(boolean repeat) throws CommboxException {
		int cmd;
		if (buffId == LINKBLOCK)
			cmd = repeat ? DO_BAT_LN : DO_BAT_L;
		else
			cmd = repeat ? DO_BAT_CN : DO_BAT_C;
		if (!doCmd(cmd))
			throw new CommboxException("RunBatch fail!");
	}

	public int readBytes(byte[] buff, int offset, int count) {
		return readData(buff, offset, count, resWaitTime);
	}

	public void connect() throws CommboxException {

		String[] infos = SerialPort.getPortNames();

		for (String info : infos) {
			try {
				serialPort.close();
				serialPort.setPortName(info);
				serialPort.setBaudRate(115200);
				serialPort.setParity(SerialPort.PARITY_NONE);
				serialPort.setDataBits(8);
				serialPort.setStopBits(SerialPort.STOPBITS_TWO);
				serialPort.setReadTimeout(500);
				
				serialPort.open();

				serialPort.setDtrEnable(false);
				Thread.sleep(50);
				serialPort.setDtrEnable(true);
				Thread.sleep(50);

				for (int i = 0; i < 3; i++) {
					if (initBox() && checkBox()) {
						clear();
						isOpen = true;
						return;
					}
				}
			} catch (InterruptedException e) {
			} catch (IOException e) {
			} catch (Exception e) {
			}
		}

		throw new CommboxException("Connect fail!");
	}

	public void disconnect() {
		if (isOpen) {
			reset();
			serialPort.close();
			isOpen = false;
		}
	}

	public boolean isOpen() {
		return isOpen;
	}
}
