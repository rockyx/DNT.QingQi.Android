package dnt.diag.ecu.visteon;

import dnt.diag.Timer;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataItemCalc;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.TroubleCodeFunction;

class PowertrainDataStream extends DataStreamFunction {

	protected PowertrainDataStream(Powertrain ecu, boolean isFreeze) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		switch (ecu.getModel()) {
		case QM250J_2L:
			if (isFreeze) {
				if (!queryLiveData("Visteon Freeze")) {
					throw new DiagException("No such live data!!!");
				}
			} else {
				if (!queryLiveData("Visteon")) {
					throw new DiagException("No such live data!!!");
				}
			}
			setReadInterval(Timer.fromMilliseconds(50));
			break;
		default:
			throw new DiagException("Unsupport model!!!");
		}
	}

	@Override
	protected void initCalcFunctions() {
		LiveDataList lds = getLiveDataItems();

		LiveDataItem item = lds.get("ECT");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				int value = buffer.get(2) & 0xFF;
				value -= 40;
				return Integer.toString(value);
			}

		});

		item = lds.get("IMAP");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				int value = buffer.get(2) & 0xFF;
				value *= 3;
				return Integer.toString(value);
			}
		});

		item = lds.get("ER");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				double value = buffer.get(2) & 0xFF;
				value *= 256;
				value += buffer.get(3) & 0xFF;
				value *= 0.25;
				return Double.toString(value);
			}
		});

		item = lds.get("ITA#1");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {

				@Override
				public String calc() {
					double value = buffer.get(2) & 0xFF;
					value /= 2;
					value -= 64;
					return Double.toString(value);
				}
			});
		}

		item = lds.get("IAT");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {

				@Override
				public String calc() {
					int value = buffer.get(2) & 0xFF;
					value -= 40;
					return Integer.toString(value);
				}
			});
		}

		item = lds.get("ATP");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				double value = buffer.get(2) & 0xFF;
				value *= 100;
				value /= 255;
				return Double.toString(value);
			}

		});

		item = lds.get("DTC");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {

				@Override
				public String calc() {
					return TroubleCodeFunction.calcStdObdTroubleCode(
							buffer.getBuff(), 0, 0, 3);
				}
			});
		}
	}

}
