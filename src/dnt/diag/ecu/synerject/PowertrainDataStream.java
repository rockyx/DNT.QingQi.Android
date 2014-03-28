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
			lds.get("ISA_AD_T_DLY").setEnabled(false);
			lds.get("ISA_ANG_DUR_MEC").setEnabled(false);
			lds.get("ISA_CTL_IS").setEnabled(false);
			lds.get("ISC_ISA_AD_MV").setEnabled(false);
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
			lds.get("ISA_AD_T_DLY").setEnabled(false);
			lds.get("ISA_ANG_DUR_MEC").setEnabled(false);
			lds.get("ISA_CTL_IS").setEnabled(false);
			lds.get("ISC_ISA_AD_MV").setEnabled(false);
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
			@Override
			public String calc() {
				// AMP 2 0...9F6H 0...2550 1
				int value = buffer.get(2) & 0xFF;
				value *= 256;
				value += buffer.get(3) & 0xFF;
				return Integer.toString(value);
			}
		});

		item = lds.get("CRASH");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// CRASH 2 0...3FFH 0...4.9951 5/1024
				double value = ((buffer.get(4) & 0xFF) * 256);
				value += buffer.get(5) & 0xFF;
				value *= 5;
				value /= 1024;

				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("CTR_ERR_DYN_NR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// CTR_ERR_DYN_NR 1 0...FFH 0...255 1
				int value = buffer.get(6) & 0xFF;
				return Integer.toString(value);
			}
		});

		item = lds.get("CUR_IGC_DIAG_cyl1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// CUR_IGC_DIAG_cyl1 2 0...3FFH 0...4.9951 5/1024
				double value = buffer.get(7) & 0xFF;
				value *= 256;
				value += buffer.get(8) & 0xFF;
				value *= 5;
				value /= 1024;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("DIST_ACT_MIL");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// DIST_ACT_MIL 2 0...FFFFH 0...65535 1
				int value = buffer.get(9) & 0xFF;
				value *= 256;
				value += buffer.get(10) & 0xFF;
				return Integer.toString(value);
			}
		});

		item = lds.get("ENG_HOUR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// ENG_HOUR 2 0...FFFFH 0...5461.25 1/12
				double value = buffer.get(11) & 0xFF;
				value *= 256;
				value += buffer.get(12) & 0xFF;
				value /= 12;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("IGA_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// IGA_1 1 0...FFH -30...89.53 15/32
				double value = buffer.get(13) & 0xFF;
				value *= 15;
				value /= 32;
				value -= 30;
				checkOutOfRange(value, item);
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("IGA_CTR_IS");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// IGA_CTL_IS 1 0...FFH -30...89.53 15/32
				double value = buffer.get(14) & 0xFF;
				value *= 15;
				value /= 32;
				value -= 30;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("INH_IV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// INH_IV 1 0...3FH 0...63 1
				if ((buffer.get(15) & 0x01) != 0) {
					return getDB().queryText("Fuel - Cut", "System");
				} else {
					return getDB().queryText("Fuel - Not Cut", "System");
				}
			}
		});

		item = lds.get("INJ_MODE");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// INJ_MODE 1 0...6H 0...6 1
				switch (buffer.get(16)) {
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
		});

		item = lds.get("ISA_AD_T_DLY");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// ISA_AD_T_DLY 1 0...FFH -12.8...12.7 0.1
				double value = buffer.get(17) & 0xFF;
				value /= 10;
				value -= 12.8;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("ISA_ANG_DUR_MEC");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// ISA_ANG_DUR_MEC 2 0...600H 0...720.00 15/32
				double value = buffer.get(18) & 0xFF;
				value *= 256;
				value += buffer.get(19) & 0xFF;
				value *= 15;
				value -= 30;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("ISA_CTL_IS");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// ISA_CTL_IS 1 0...FFH -120...119.06 15/16
				double value = buffer.get(20) & 0xFF;
				value *= 15;
				value /= 16;
				value -= 120;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("ISC_ISA_AD_MV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// ISC_ISA_AD_MV 1 0...FFH -120...119.06 15/16
				double value = buffer.get(21) & 0xFF;
				value *= 15;
				value /= 16;
				value -= 120;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("LAMB_SP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LAMB_SP 1 0...FFH 0.5...1.4960 1/256
				double value = buffer.get(22) & 0xFF;
				value /= 256;
				value += 0.5;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("LV_AFR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_AFR 1 0...1H 0...1 1
				// bit 7
				if ((buffer.get(23) & 0x80) != 0) {
					return getDB().queryText("Thick", "System");
				} else {
					return getDB().queryText("Thin", "System");
				}
			}
		});

		item = lds.get("LV_CELP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_CELP 1 0...1H 0...1 1
				// bit 6
				if ((buffer.get(23) & 0x40) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_CUT_OUT");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_CUT_OUT 1 0...1H 0...1 1
				// bit 5
				if ((buffer.get(23) & 0x20) != 0) {
					return getDB().queryText("Oil - Cut", "System");
				} else {
					return getDB().queryText("Oil - Not Cut", "System");
				}
			}
		});

		item = lds.get("LV_EOL_EFP_PRIM");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_EOL_EFP_PRIM 1 0...1H 0...1 1
				// bit 4
				if ((buffer.get(23) & 0x10) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_EOL_EFP_PRIM_ACT");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_EOL_EFP_PRIM_ACT 1 0...1H 0...1 1
				// bit 3
				if ((buffer.get(23) & 0x08) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_IMMO_PROG");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_IMMO_PROG 1 0...1H 0...1 1
				// bit 2
				if ((buffer.get(23) & 0x04) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_IMMO_ECU_PROG");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_IMMO_ECU_PROG 1 0...1H 0...1 1
				// bit 1
				if ((buffer.get(23) & 0x02) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_LOCK_IMOB");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_LOCK_IMOB 1 0...1H 0...1 1
				// bit 0
				if ((buffer.get(23) & 0x01) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_LSCL_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_LSCL_1 1 0...1H 0...1 1
				// bit 4
				if ((buffer.get(24) & 0x10) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_LSH_UP_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_LSH_UP_1 1 0...1H 0...1 1
				// bit 3
				if ((buffer.get(24) & 0x08) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_REQ_ISC");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_REQ_ISC 1 0...1H 0...1 1
				// bit 2
				if ((buffer.get(24) & 0x04) != 0) {
					return getDB().queryText("Idle Controlling", "System");
				} else {
					return getDB().queryText("Idle Not Controlling", "System");
				}
			}
		});

		item = lds.get("LV_VIP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_VIP 1 0...1H 0...1 1
				// bit 1
				if ((buffer.get(24) & 0x02) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("LV_EOP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_EOP
				// bit 0
				if ((buffer.get(24) & 0x01) != 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});

		item = lds.get("MAF");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MAF 2 0...FFFFH 0...1023.984 1/64
				double value = buffer.get(25) & 0xFF;
				value *= 256;
				value += buffer.get(26) & 0xFF;
				value /= 64;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MAF_THR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MAF_THR 2 0...FFFFH 0...1023.984 1/64
				double value = buffer.get(27) & 0xFF;
				value *= 256;
				value += buffer.get(28) & 0xFF;
				value /= 64;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MAP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MAP 2 0...9F6H 0...2550 1
				double value = buffer.get(29) & 0xFF;
				value *= 256;
				value += buffer.get(30);
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MAP_UP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MAP_UP 2 0...9F6H 0...2550 1
				int value = buffer.get(31) & 0xFF;
				value *= 256;
				value += buffer.get(32) & 0xFF;
				return Integer.toString(value);
			}
		});

		item = lds.get("MFF_AD_ADD_MMV_REL");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_AD_ADD_MMV_REL 2 0...FFFFH -128...127.9960 1/256
				double value = buffer.get(33) & 0xFF;
				value *= 256;
				value += buffer.get(34) & 0xFF;
				value /= 256;
				value -= 128;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MFF_AD_FAC_MMV_REL");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_AD_FAC_MMV_REL 2 0...FFFFH -32...31.99902 1/1024
				double value = buffer.get(35) & 0xFF;
				value *= 256;
				value += buffer.get(36) & 0xFF;
				value /= 1024;
				value -= 32;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MFF_AD_ADD_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_AD_ADD_MMV 2 0...FFFFH -128...127.9960 1/256
				double value = buffer.get(37) & 0xFF;
				value *= 256;
				value += buffer.get(38) & 0xFF;
				value /= 256;
				value -= 128;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MFF_AD_FAC_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_AD_FAC_MMV 2. 0...FFFFH -32...32.99902 1/1024
				double value = buffer.get(39) & 0xFF;
				value *= 256;
				value += buffer.get(40) & 0xFF;
				value /= 1024;
				value -= 32;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MFF_INJ_HOM");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_INJ_HOM 2 0...FFFFH 0...255.9960 1/256
				double value = buffer.get(41) & 0xFF;
				value *= 256;
				value += buffer.get(42) & 0xFF;
				value /= 256;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MFF_WUP_COR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MFF_WUP_COR 1 0...FFH 0...0.9960 1/256
				double value = buffer.get(43) & 0xFF;
				value /= 256;
				return String.format(Locale.getDefault(), "%.4f", value);
			}
		});

		item = lds.get("MOD_IGA");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// MOD_IGA 1 0H/1H NOT_PHASE/PHASED 1
				if (buffer.get(44) == 0) {
					return getDB().queryText("Undetermined Phase", "System");
				} else {
					return getDB().queryText("Phase", "System");
				}
			}
		});

		item = lds.get("N");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// N 2 0..4650H 0...18000 1
				int value = buffer.get(45) & 0xFF;
				value *= 256;
				value += buffer.get(46);
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}
		});

		item = lds.get("N_MAX_THD");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// N_MAX_THD 2 0...4650H 0...18000 1
				int value = buffer.get(47) & 0xFF;
				value *= 256;
				value += buffer.get(48);
				return Integer.toString(value);
			}
		});

		item = lds.get("N_SP_ISC");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// N_SP_ISC 2 0...FFFFH -32768...32767 1
				int value = buffer.get(49) & 0xFF;
				value *= 256;
				value += buffer.get(50) & 0xFF;
				value -= 32768;
				return Integer.toString(value);
			}
		});

		item = lds.get("SOI_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// SOI_1 2 0...600H -180...540.00 15/32
				double value = buffer.get(51) & 0xFF;
				value *= 256;
				value += buffer.get(52) & 0xFF;
				value *= 15;
				value /= 32;
				value -= 180;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("STATE_EFP");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// STATE_EFP 1 0H/1H/2H EFP_OFF/EFP_ON/EFP_PRIME 1
				switch (buffer.get(53)) {
				case 0:
					return getDB().queryText("Close", "System");
				case 1:
					return getDB().queryText("Open", "System");
				default:
					return getDB().queryText("Prime Pump", "System");
				}
			}
		});

		item = lds.get("STATE_ENGSTATE");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
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
		});

		item = lds.get("TCO");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TCO 1 0..FFH -40...215 1
				int value = buffer.get(55) & 0xFF;
				value -= 40;
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}
		});

		item = lds.get("TCOPWM");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TCOPWM 1 0...FFH 0...99.6 25/64
				double value = buffer.get(56) & 0xFF;
				value *= 25;
				value /= 64;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("TD_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TD_1 2 0...FFFFH 0...262.140 0.004
				double value = buffer.get(57) & 0xFF;
				value *= 256;
				value += buffer.get(58) & 0xFF;
				value *= 0.004;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("TI_HOM_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TI_HOM_1 2 0...FFFFH 0...262.140 0.004
				double value = buffer.get(59) & 0xFF;
				value *= 256;
				value += buffer.get(60) & 0xFF;
				value *= 0.004;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("TI_LAM_COR");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TI_LAM_COR 2 0...FFFFH -32...32.99902 1/1024
				double value = buffer.get(61) & 0xFF;
				value *= 256;
				value += buffer.get(62);
				value /= 1024;
				value -= 32;
				checkOutOfRange(value, item);
				return String.format("%.4f", value);
			}
		});

		item = lds.get("TIA");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TIA 1 0...FFH -40...215 1
				int value = buffer.get(63) & 0xFF;
				value -= 40;
				checkOutOfRange(value, item);
				return Integer.toString(value);
			}
		});

		item = lds.get("TIA_CYL");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TIA_CYL 1 0...FFH -40...215 1
				int value = buffer.get(64) & 0xFF;
				value -= 40;
				return Integer.toString(value);
			}
		});

		item = lds.get("TPS_MTC_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// TPS_MTC_1 2 0...FFFFH 0...127.9980 1/512
				double value = buffer.get(65) & 0xFF;
				value *= 256;
				value += buffer.get(66) & 0xFF;
				value /= 512;
				checkOutOfRange(value, item);
				return String.format("%.4f", value);
			}
		});

		item = lds.get("V_TPS_AD_BOL_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// V_TPS_AD_BOL_1 2 0...3FFH 0...4.9951 5/1024
				double value = buffer.get(67) & 0xFF;
				value *= 256;
				value += buffer.get(68) & 0xFF;
				value *= 5;
				value /= 1024;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("VBK_MMV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// VBK_MMV 1 0...FFH 4...19.937 1/16
				double value = buffer.get(69) & 0xFF;
				value /= 16;
				value += 4;
				checkOutOfRange(value, item);
				return String.format("%.4f", value);
			}
		});

		item = lds.get("VLS_UP_1");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// VLS_UP_1 2 0...3FFH 0...4.9951 5/1024
				double value = buffer.get(70) & 0xFF;
				value *= 256;
				value += buffer.get(71) & 0xFF;
				value *= 5;
				value /= 1024;
				checkOutOfRange(value, item);
				return String.format("%.4f", value);
			}
		});

		item = lds.get("VS_8");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// VS_8 1 0...FFH 0...255 1
				int value = buffer.get(72) & 0xFF;
				return Integer.toString(value);
			}
		});

		item = lds.get("V_TPS_1_BAS");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// V_TPS_1_BAS 2 0...3FFH 0...4.9951 5/1024
				double value = buffer.get(73) & 0xFF;
				value *= 256;
				value += buffer.get(74) & 0xFF;
				value *= 5;
				value /= 1024;
				return String.format("%.4f", value);
			}
		});

		item = lds.get("LV_SAV");
		item.setCalc(new LiveDataItemCalc(item) {
			@Override
			public String calc() {
				// LV_SAV 1 0...1H 0...1 -
				if (buffer.get(75) == 0) {
					return getDB().queryText("Yes", "System");
				} else {
					return getDB().queryText("No", "System");
				}
			}
		});
	}
}
