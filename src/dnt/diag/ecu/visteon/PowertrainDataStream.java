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
			private byte buff = 0;

			@Override
			protected String calc() {
				int value = (buff & 0xFF) - 40;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(2)) {
					buff = buffer.get(2);
					return true;
				}
				return false;
			}

		});

		item = lds.get("IMAP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				int value = (buff & 0xFF) * 3;
				return Integer.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(2)) {
					buff = buffer.get(2);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ER");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				double value = ((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF)) * 0.25;
				return Double.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(2)) | (buff[1] != buffer.get(3))) {
					buff[0] = buffer.get(2);
					buff[1] = buffer.get(3);
					return true;
				}
				return false;
			}
		});

		item = lds.get("ITA#1");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {
				private byte buff = 0;

				@Override
				protected String calc() {
					double value = (buff & 0xFF) / 2 - 64;
					return Double.toString(value);
				}

				@Override
				protected boolean dataChanged() {
					if (buff != buffer.get(2)) {
						buff = buffer.get(2);
						return true;
					}
					return false;
				}
			});
		}

		item = lds.get("IAT");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {
				private byte buff = 0;

				@Override
				protected String calc() {
					int value = (buff & 0xFF) - 40;
					return Integer.toString(value);
				}

				@Override
				protected boolean dataChanged() {
					if (buff != buffer.get(2)) {
						buff = buffer.get(2);
						return true;
					}
					return false;
				}
			});
		}

		item = lds.get("ATP");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte buff = 0;

			@Override
			protected String calc() {
				double value = (buff & 0xFF) * 100 / 255;
				return Double.toString(value);
			}

			@Override
			protected boolean dataChanged() {
				if (buff != buffer.get(2)) {
					buff = buffer.get(2);
					return true;
				}
				return false;
			}

		});

		item = lds.get("DTC");
		if (item != null) {
			item.setCalc(new LiveDataItemCalc(item) {
				private byte[] buff = new byte[2];

				@Override
				protected String calc() {
					return TroubleCodeFunction.calcStdObdTroubleCode(buff, 0,
							0, 0);
				}

				@Override
				protected boolean dataChanged() {
					if ((buff[0] != buffer.get(3))
							|| (buff[1] != buffer.get(4))) {
						buff[0] = buffer.get(3);
						buff[1] = buffer.get(4);
						return true;
					}
					return false;
				}
			});
		}
	}

}
