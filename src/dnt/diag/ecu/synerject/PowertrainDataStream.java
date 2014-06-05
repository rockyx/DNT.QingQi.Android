package dnt.diag.ecu.synerject;

import java.util.Locale;

import dnt.diag.Timer;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataItemCalc;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;

public class PowertrainDataStream extends DataStreamFunction {

	Powertrain ecu;

	public PowertrainDataStream(Powertrain ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		this.ecu = ecu;

		setReadInterval(Timer.fromMilliseconds(20));
		LiveDataList lds = null;

		switch (ecu.getModel()) {
		case QM125T_8H:
			if (!queryLiveData("QM125T-8H")) {
				throw new DiagException("Cannot find live datas");
			}

			lds = getLiveDataItems();

			for (LiveDataItem item : lds) {
				item.setEnabled(true);
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
			}

			lds.get("CRASH").setEnabled(false);
			lds.get("DIST_ACT_MIL").setEnabled(false);
			lds.get("ISA_AD_T_DLY").setEnabled(false);
			lds.get("ISA_ANG_DUR_MEC").setEnabled(false);
			lds.get("ISA_CTL_IS").setEnabled(false);
			lds.get("ISC_ISA_AD_MV").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM_ACT").setEnabled(false);
			lds.get("LV_IMMO_PROG").setEnabled(false);
			lds.get("LV_IMMO_ECU_PROG").setEnabled(false);
			lds.get("LV_LOCK_IMOB").setEnabled(false);
			lds.get("LV_VIP").setEnabled(false);
			lds.get("LV_EOP").setEnabled(false);
			lds.get("TCOPWM").setEnabled(false);
			lds.get("VS_8").setEnabled(false);
			lds.get("V_TPS_1_BAS").setEnabled(false);
			lds.get("LV_SAV").setEnabled(false);
			lds.get("LV_CUT_OUT").setEnabled(false);

			break;
		case QM250GY:
			if (!queryLiveData("QM250GY")) {
				throw new DiagException("Cannot find live datas");
			}

			lds = getLiveDataItems();

			for (LiveDataItem item : lds) {
				item.setEnabled(true);
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
			}

			lds.get("CRASH").setEnabled(false);
			lds.get("DIST_ACT_MIL").setEnabled(false);
			// lds.get("ISA_AD_T_DLY").setEnabled(false);
			// lds.get("ISA_ANG_DUR_MEC").setEnabled(false);
			// lds.get("ISA_CTL_IS").setEnabled(false);
			// lds.get("ISC_ISA_AD_MV").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM_ACT").setEnabled(false);
			lds.get("LV_IMMO_PROG").setEnabled(false);
			lds.get("LV_IMMO_ECU_PROG").setEnabled(false);
			lds.get("LV_LOCK_IMOB").setEnabled(false);
			lds.get("LV_LSH_UP_1").setEnabled(false);
			lds.get("LV_VIP").setEnabled(false);
			lds.get("LV_EOP").setEnabled(false);
			lds.get("TCOPWM").setEnabled(false);
			lds.get("VS_8").setEnabled(false);
			lds.get("LV_SAV").setEnabled(false);
			lds.get("LV_CUT_OUT").setEnabled(false);
			break;
		case QM250T:
			if (!queryLiveData("QM250T")) {
				throw new DiagException("Cannot find live datas");
			}

			lds = getLiveDataItems();

			for (LiveDataItem item : lds) {
				item.setEnabled(true);
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
			}

			lds.get("CRASH").setEnabled(false);
			lds.get("DIST_ACT_MIL").setEnabled(false);
			// lds.get("ISA_AD_T_DLY").setEnabled(false);
			// lds.get("ISA_ANG_DUR_MEC").setEnabled(false);
			// lds.get("ISA_CTL_IS").setEnabled(false);
			// lds.get("ISC_ISA_AD_MV").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM").setEnabled(false);
			lds.get("LV_EOL_EFP_PRIM_ACT").setEnabled(false);
			lds.get("LV_IMMO_PROG").setEnabled(false);
			lds.get("LV_IMMO_ECU_PROG").setEnabled(false);
			lds.get("LV_LOCK_IMOB").setEnabled(false);
			lds.get("LV_LSH_UP_1").setEnabled(false);
			lds.get("LV_VIP").setEnabled(false);
			lds.get("LV_EOP").setEnabled(false);
			lds.get("VS_8").setEnabled(false);
			lds.get("V_TPS_1_BAS").setEnabled(false);
			lds.get("LV_SAV").setEnabled(false);
			lds.get("LV_CUT_OUT").setEnabled(false);
			break;
		default:
			throw new DiagException("Unsupport model!");
		}

		setBeginRead(ecu.startCommunication);
		setEndRead(ecu.endCommunication);
	}

	@Override
	protected void initCalcFunctions() {
		// recv is the ReadDataByLocalIdentifier positive Response
		// Copy from protocol document
		// Data Byte Parameter Name Cvt Hex Value Mnemonic
		// #1d readDataByLocalIdentifier Response Service Id M #61h RDBLIPR
		// #2d recordLocalIdentifier M #XXh RLI
		// #3d recordValue#1 M #XXh RV
		// ... ... M ... ...
		// #nd recordValue#n M #XXh RV
		// Because in array we are starts by 0 index, so recv[0] is #1d, recv[1]
		// is #2d,
		// recv[2] is the #3d, recv[3] is the #4d, and so on.

		// Name Size Conversion (hex/bin) Conversion (physical) Resol.

		LiveDataList lds = getLiveDataItems();
		LiveDataItem item = lds.get("AMP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(2)) || (buff[1] != buffer.get(3))) {
					buff[0] = buffer.get(2);
					buff[1] = buffer.get(3);
					return true;
				}
				return false;
			}

			@Override
			protected String calc() {
				// AMP 2 0...9F6H 0...2550 1
				int value = (buff[0] & 0xFF) * 256 + (buff[1] & 0xFF);
				return Integer.toString(value);
			}
		});

		item = lds.get("CRASH");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// CRASH 2 0...3FFH 0...4.9951 5/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 5 / 1024;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(4)) || (buff[1] != buffer.get(4))) {
					buff[0] = buffer.get(4);
					buff[1] = buffer.get(5);
					return true;
				}
				return false;
			}
		});

		item = lds.get("CTR_ERR_DYN_NR");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// CTR_ERR_DYN_NR 1 0...FFH 0...255 1
				int value = buff & 0xFF;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(6)) {
					buff = buffer.get(6);
					return true;
				}
				return false;
			}
		});

		item = lds.get("CUR_IGC_DIAG_cyl1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// CUR_IGC_DIAG_cyl1 2 0...3FFH 0...4.9951 5/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 5 / 1024;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(7)) || (buff[1] != buffer.get(8))) {
					buff[0] = buffer.get(7);
					buff[1] = buffer.get(8);
					return true;
				}
				return false;
			}
		});

		item = lds.get("DIST_ACT_MIL");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// DIST_ACT_MIL 2 0...FFFFH 0...65535 1
				int value = (buff[0] & 0xFF) * 256 + (buff[1] & 0xFF);
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(9)) || (buff[1] != buffer.get(10))) {
					buff[0] = buffer.get(9);
					buff[1] = buffer.get(10);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ENG_HOUR");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// ENG_HOUR 2 0...FFFFH 0...5461.25 1/12
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 12;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(11)) || (buff[1] != buffer.get(12))) {
					buff[0] = buffer.get(11);
					buff[1] = buffer.get(12);
					return true;
				}
				return false;
			}
		});

		item = lds.get("IGA_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// IGA_1 1 0...FFH -30...89.53 15/32
				double value = (buff & 0xFF) * 15 / 32 - 30;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(13)) {
					buff = buffer.get(13);
					return true;
				}
				return false;
			}
		});

		item = lds.get("IGA_CTR_IS");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// IGA_CTL_IS 1 0...FFH -30...89.53 15/32
				double value = (buff & 0xFF) * 15 / 32 - 30;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(14)) {
					buff = buffer.get(14);
					return true;
				}
				return false;
			}
		});

		item = lds.get("INH_IV");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// INH_IV 1 0...3FH 0...63 1
				if (buff != 0) {
					return getDB().queryText("Fuel - Cut", "System");
				} else {
					return getDB().queryText("Fuel - Not Cut", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(15) & 0x01;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("INJ_MODE");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// INJ_MODE 1 0...6H 0...6 1
				switch (buff) {
				case 0:
					return getDB().queryText("Ban", "Synerject");
				case 1:
					return getDB().queryText("Static", "Synerject");
				case 2:
					return getDB().queryText("Early Fuel Injection",
							"Synerject");
				case 3:
					return getDB().queryText("Early Phase Jet", "Synerject");
				case 4:
					return getDB().queryText("2 Stoke", "Synerject");
				case 5:
					return getDB().queryText("4 Stoke", "Synerject");
				case 6:
					return getDB().queryText("4 Stoke Undetermined Phase",
							"Synerject");
				default:
					return "";
				}
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(16)) {
					buff = buffer.get(16);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ISA_AD_T_DLY");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// ISA_AD_T_DLY 1 0...FFH -12.8...12.7 0.1
				double value = (buff & 0xFF) / 10 - 12.8;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(17)) {
					buff = buffer.get(17);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ISA_ANG_DUR_MEC");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// ISA_ANG_DUR_MEC 2 0...600H 0...720.00 15/32
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 15 / 30;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(18)) || (buff[1] != buffer.get(19))) {
					buff[0] = buffer.get(18);
					buff[1] = buffer.get(19);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ISA_CTL_IS");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// ISA_CTL_IS 1 0...FFH -120...119.06 15/16
				double value = (buff & 0xFF) * 15 / 16 - 120;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(20)) {
					buff = buffer.get(20);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ISC_ISA_AD_MV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// ISC_ISA_AD_MV 1 0...FFH -120...119.06 15/16
				double value = (buff & 0xFF) * 15 / 16 - 120;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(21)) {
					buff = buffer.get(21);
					return true;
				}
				return false;
			}
		});

		item = lds.get("LAMB_SP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// LAMB_SP 1 0...FFH 0.5...1.4960 1/256
				double value = (buff & 0xFF) / 256 + 0.5;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(22)) {
					buff = buffer.get(22);
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_AFR");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_AFR 1 0...1H 0...1 1
				// bit 7
				if (buff != 0) {
					return getDB().queryText("Thick", "System");
				} else {
					return getDB().queryText("Thin", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x80;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_CELP");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_CELP 1 0...1H 0...1 1
				// bit 6
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x40;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_CUT_OUT");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_CUT_OUT 1 0...1H 0...1 1
				// bit 5
				if (buff != 0) {
					return getDB().queryText("Oil - Cut", "System");
				} else {
					return getDB().queryText("Oil - Not Cut", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x20;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_EOL_EFP_PRIM");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_EOL_EFP_PRIM 1 0...1H 0...1 1
				// bit 4
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x10;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}

		});

		item = lds.get("LV_EOL_EFP_PRIM_ACT");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_EOL_EFP_PRIM_ACT 1 0...1H 0...1 1
				// bit 3
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x08;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_IMMO_PROG");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_IMMO_PROG 1 0...1H 0...1 1
				// bit 2
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x04;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_IMMO_ECU_PROG");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_IMMO_ECU_PROG 1 0...1H 0...1 1
				// bit 1
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x02;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_LOCK_IMOB");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_LOCK_IMOB 1 0...1H 0...1 1
				// bit 0
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(23) & 0x01;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_LSCL_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_LSCL_1 1 0...1H 0...1 1
				// bit 4
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(24) & 0x10;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_LSH_UP_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_LSH_UP_1 1 0...1H 0...1 1
				// bit 3
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(24) & 0x08;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_REQ_ISC");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_REQ_ISC 1 0...1H 0...1 1
				// bit 2
				if (buff != 0) {
					return getDB().queryText("Idle Controlling", "System");
				} else {
					return getDB().queryText("Idle Not Controlling", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(24) & 0x04;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_VIP");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_VIP 1 0...1H 0...1 1
				// bit 1
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(24) & 0x02;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("LV_EOP");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				// LV_EOP
				// bit 0
				if (buff != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(24) & 0x01;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
		});

		item = lds.get("MAF");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MAF 2 0...FFFFH 0...1023.984 1/64
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 64;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(25)) || (buff[1] != buffer.get(26))) {
					buff[0] = buffer.get(25);
					buff[1] = buffer.get(26);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MAF_THR");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MAF_THR 2 0...FFFFH 0...1023.984 1/64
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 64;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(27)) || (buff[1] != buffer.get(28))) {
					buff[0] = buffer.get(27);
					buff[1] = buffer.get(28);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MAP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MAP 2 0...9F6H 0...2550 1
				double value = (buff[0] & 0xFF) * 256 + (buff[1] & 0xFF);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(29)) || (buff[1] != buffer.get(30))) {
					buff[0] = buffer.get(29);
					buff[1] = buffer.get(30);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MAP_UP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MAP_UP 2 0...9F6H 0...2550 1
				int value = (buff[0] & 0xFF) * 256 + (buff[1] & 0xFF);
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(31)) || (buff[1] != buffer.get(32))) {
					buff[0] = buffer.get(31);
					buff[1] = buffer.get(32);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_AD_ADD_MMV_REL");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MFF_AD_ADD_MMV_REL 2 0...FFFFH -128...127.9960 1/256
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 256 - 128;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(33)) || (buff[1] != buffer.get(34))) {
					buff[0] = buffer.get(33);
					buff[1] = buffer.get(34);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_AD_FAC_MMV_REL");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MFF_AD_FAC_MMV_REL 2 0...FFFFH -32...31.99902 1/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 1024 - 32;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(35)) || (buff[1] != buffer.get(36))) {
					buff[0] = buffer.get(35);
					buff[1] = buffer.get(36);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_AD_ADD_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MFF_AD_ADD_MMV 2 0...FFFFH -128...127.9960 1/256
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 256 - 128;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(37)) || (buff[1] != buffer.get(38))) {
					buff[0] = buffer.get(37);
					buff[1] = buffer.get(38);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_AD_FAC_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MFF_AD_FAC_MMV 2. 0...FFFFH -32...32.99902 1/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 1024 - 32;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(39)) || (buff[1] != buffer.get(40))) {
					buff[0] = buffer.get(39);
					buff[1] = buffer.get(40);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_INJ_HOM");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// MFF_INJ_HOM 2 0...FFFFH 0...255.9960 1/256
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 256;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(41)) || (buff[1] != buffer.get(42))) {
					buff[0] = buffer.get(41);
					buff[1] = buffer.get(42);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MFF_WUP_COR");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// MFF_WUP_COR 1 0...FFH 0...0.9960 1/256
				double value = (buff & 0xFF) / 256;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(43)) {
					buff = buffer.get(43);
					return true;
				}
				return false;
			}
		});

		item = lds.get("MOD_IGA");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// MOD_IGA 1 0H/1H NOT_PHASE/PHASED 1
				if (buff == 0) {
					return getDB().queryText("Undetermined Phase", "System");
				} else {
					return getDB().queryText("Phase", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(44)) {
					buff = buffer.get(44);
					return true;
				}
				return false;
			}
		});

		item = lds.get("N");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// N 2 0..4650H 0...18000 1
				int value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF));
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(45)) || (buff[1] != buffer.get(46))) {
					buff[0] = buffer.get(45);
					buff[1] = buffer.get(46);
					return true;
				}
				return false;
			}
		});

		item = lds.get("N_MAX_THD");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// N_MAX_THD 2 0...4650H 0...18000 1
				int value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF));
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(47)) || (buff[1] != buffer.get(48))) {
					buff[0] = buffer.get(47);
					buff[1] = buffer.get(48);
					return true;
				}
				return false;
			}
		});

		item = lds.get("N_SP_ISC");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// N_SP_ISC 2 0...FFFFH -32768...32767 1
				int value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) - 32768;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(49)) || (buff[1] != buffer.get(50))) {
					buff[0] = buffer.get(49);
					buff[1] = buffer.get(50);
					return true;
				}
				return false;
			}
		});

		item = lds.get("SOI_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// SOI_1 2 0...600H -180...540.00 15/32
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 15 / 32 - 180;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(51)) || (buff[1] != buffer.get(52))) {
					buff[0] = buffer.get(51);
					buff[1] = buffer.get(52);
					return true;
				}
				return false;
			}
		});

		item = lds.get("STATE_EFP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// STATE_EFP 1 0H/1H/2H EFP_OFF/EFP_ON/EFP_PRIME 1
				switch (buff) {
				case 0:
					return getDB().queryText("Close", "System");
				case 1:
					return getDB().queryText("Open", "System");
				default:
					return getDB().queryText("Prime Pump", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(53)) {
					buff = buffer.get(53);
					return true;
				}
				return false;
			}
		});

		item = lds.get("STATE_ENGSTATE");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// STATE_ENGSTATE 1 0H/1H/2H/3H/4H/5H ES/ST/IS/PL/PU/PUC 1
				switch (buffer.get(54)) {
				case 0:
					return getDB().queryText("Stopped", "System");
				case 1:
					return getDB().queryText("Running", "System");
				case 2:
					return getDB().queryText("Idle", "System");
				case 3:
					return getDB().queryText("Part Load", "System");
				case 4:
					return getDB().queryText("Inverted", "System");
				case 5:
					return getDB().queryText("Inverted - Cut", "System");
				default:
					return "";
				}
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(54)) {
					buff = buffer.get(54);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TCO");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// TCO 1 0..FFH -40...215 1
				int value = (buff & 0xFF) - 40;
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(55)) {
					buff = buffer.get(55);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TCOPWM");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// TCOPWM 1 0...FFH 0...99.6 25/64
				double value = (buff & 0xFF) * 25 / 64;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(56)) {
					buff = buffer.get(56);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TD_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// TD_1 2 0...FFFFH 0...262.140 0.004
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 0.004;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(57)) || (buff[1] != buffer.get(58))) {
					buff[0] = buffer.get(57);
					buff[1] = buffer.get(58);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TI_HOM_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// TI_HOM_1 2 0...FFFFH 0...262.140 0.004
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 0.004;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(59)) || (buff[1] != buffer.get(60))) {
					buff[0] = buffer.get(59);
					buff[1] = buffer.get(60);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TI_LAM_COR");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// TI_LAM_COR 2 0...FFFFH -32...32.99902 1/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 1024 - 32;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(61)) || (buff[1] != buffer.get(62))) {
					buff[0] = buffer.get(61);
					buff[1] = buffer.get(62);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TIA");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// TIA 1 0...FFH -40...215 1
				int value = (buff & 0xFF) - 40;
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(63)) {
					buff = buffer.get(63);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TIA_CYL");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// TIA_CYL 1 0...FFH -40...215 1
				int value = buff & 0xFF;
				value -= 40;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(64)) {
					buff = buffer.get(64);
					return true;
				}
				return false;
			}
		});

		item = lds.get("TPS_MTC_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// TPS_MTC_1 2 0...FFFFH 0...127.9980 1/512
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) / 512;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(65)) || (buff[1] != buffer.get(66))) {
					buff[0] = buffer.get(65);
					buff[1] = buffer.get(66);
					return true;
				}
				return false;
			}
		});

		item = lds.get("V_TPS_AD_BOL_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// V_TPS_AD_BOL_1 2 0...3FFH 0...4.9951 5/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 5 / 1024;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(67)) || (buff[1] != buffer.get(68))) {
					buff[0] = buffer.get(67);
					buff[1] = buffer.get(68);
					return true;
				}
				return false;
			}
		});

		item = lds.get("VBK_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// VBK_MMV 1 0...FFH 4...19.937 1/16
				double value = buff & 0xFF;
				value /= 16;
				value += 4;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(69)) {
					buff = buffer.get(69);
					return true;
				}
				return false;
			}
		});

		item = lds.get("VLS_UP_1");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// VLS_UP_1 2 0...3FFH 0...4.9951 5/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 5 / 1024;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(70)) || (buff[1] != buffer.get(71))) {
					buff[0] = buffer.get(70);
					buff[1] = buffer.get(71);
					return true;
				}
				return false;
			}
		});

		item = lds.get("VS_8");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// VS_8 1 0...FFH 0...255 1
				int value = buff & 0xFF;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(72)) {
					buff = buffer.get(72);
					return true;
				}
				return false;
			}
		});

		item = lds.get("V_TPS_1_BAS");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				// V_TPS_1_BAS 2 0...3FFH 0...4.9951 5/1024
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 5 / 1024;
				return String.format(Locale.getDefault(), "%.4f", value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(73)) || (buff[1] != buffer.get(74))) {
					buff[0] = buffer.get(73);
					buff[1] = buffer.get(74);
					return true;
				}
				return false;
			}

		});

		item = lds.get("LV_SAV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				// LV_SAV 1 0...1H 0...1 -
				if (buff == 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(75)) {
					buff = buffer.get(75);
					return true;
				}
				return false;
			}
		});
	}
}
