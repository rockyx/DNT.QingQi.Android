package dnt.diag.attribute;

public class Attribute {

	public enum KLineParity {
		None, //
		Even, //
		Odd; //
	}

	public enum KWP2KStartType {
		Fast, //
		Addr; //
	}

	public enum KWP2KMode {
		Mode8X, //
		Mode80, //
		ModeXX, //
		Mode00, //
		ModeCX; //
	}

	public enum CanBaudRate {
		B1000K(1000000), //
		B800K(800000), //
		B666K(666000), //
		B500K(500000), //
		B400K(400000), //
		B250K(250000), //
		B200K(200000), //
		B125K(125000), //
		B100K(100000), //
		B80K(80000), //
		B50K(50000), //
		B40K(40000), //
		B20K(20000), //
		B10K(10000), //
		B5K(5000); //

		int value;

		CanBaudRate(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum CanFilterMask {
		Enable(0x0F), //
		Disable(0x00); //

		int value;

		CanFilterMask(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public enum CanFrameType {
		Data(0x00), //
		Remote(0x40); //

		int value;

		CanFrameType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	public enum CanIDMode {
		Standard(0x00), //
		Extendsion(0x80); //

		int value;

		CanIDMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}

	// Common
	public byte[] heartbeat;

	// K Line
	public KLineParity klineParity;
	public boolean klineL;
	public int klineAddrCode;
	public int klineTargetAddress;
	public int klineSourceAddress;
	public int klineBaudRate;
	public int klineComLine;

	// KWP 2000
	public KWP2KStartType kwp2KStartType;
	public KWP2KMode kwp2kHeartbeatMode;
	public KWP2KMode kwp2kMsgMode;
	public KWP2KMode kwp2kCurrentMode;
	public byte[] kwp2kFastCmd;

	// ISO9141
	public int isoHeader;

	// Canbus
	public int canbusIdForEcuRecv;
	public CanBaudRate canbusBaudRate;
	public CanIDMode canbusIDMode;
	public CanFilterMask canbusFilterMask;
	public int canbusHighPin;
	public int canbusLowPin;
	public int[] canbusIDRecvFilters;
	public byte[] canbusFlowControl;
}
