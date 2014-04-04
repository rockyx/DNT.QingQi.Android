package dnt.diag.ecu.mikuni;

import java.util.Locale;

import dnt.diag.Timer;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataItemCalc;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;

class PowertrainDataStreamECU200 extends DataStreamFunction {

	abstract class MyLiveDataItemCalc extends LiveDataItemCalc {

		protected byte[] buff = new byte[16];

		public MyLiveDataItemCalc(LiveDataItem item) {
			super(item);
		}

		@Override
		protected boolean dataChanged() {
			int length = buffer.getLength();
			if (buff.length == length) {
				for (int i = 0; i < length; i++) {
					if (buff[i] != buffer.get(i)) {
						System.arraycopy(buffer.getBuff(), 0, buff, 0, length);
						return true;
					}
				}
			} else {
				buff = new byte[length];
				System.arraycopy(buffer.getBuff(), 0, buff, 0, length);
				return true;
			}
			return false;
		}
	}

	private PowertrainModel model;

	public PowertrainDataStreamECU200(PowertrainECU200 ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());

		model = ecu.getModel();

		LiveDataList lds;
		switch (model) {
		case DCJ_16A:
		case DCJ_16C:
		case DCJ_10:
			if (!queryLiveData("DCJ Mikuni ECU200")) {
				throw new DiagException("Cannot find live datas");
			}
			lds = getLiveDataItems();
			for (LiveDataItem item : lds) {
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
				item.setEnabled(true);
			}
			break;
		case QM200GY_F:
		case QM200_3D:
		case QM200J_3L:
			if (!queryLiveData("QingQi Mikuni ECU200")) {
				throw new DiagException("Cannot find live datas");
			}
			lds = getLiveDataItems();
			for (LiveDataItem item : lds) {
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
				item.setEnabled(true);
			}
			lds.get("TS").setEnabled(false);
			lds.get("ERF").setEnabled(false);
			lds.get("IS").setEnabled(false);

			break;
		default:
			throw new DiagException("Unsupport model");
		}

		setReadInterval(Timer.fromMilliseconds(10));
	}

	@Override
	protected void initCalcFunctions() {
		LiveDataList lds = getLiveDataItems();
		LiveDataItem item = lds.get("ER");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					int value = Integer.valueOf(cs.decode(bb).toString(), 16);
					value = (value * 500) / 256;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%d", value);
				} catch (NumberFormatException ex) {
					return "0";
				}
			}
		});

		item = lds.get("BV");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {

				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16)
							.doubleValue();
					value /= 512;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException ex) {
					return "0.0";
				}
			}

		});

		item = lds.get("TPS");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 512;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}

		});

		item = lds.get("MAT");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 256;
					value -= 50;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("ET");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 256;
					value -= 50;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("BP");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 512;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("MP");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 512;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("IT");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);

					value *= 15;
					value /= 256;
					value -= 22.5;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("IPW");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					double value = Integer
							.valueOf(cs.decode(bb).toString(), 16);
					value /= 2;

					DataStreamFunction.checkOutOfRange(value, item);
					return String.format(Locale.getDefault(), "%.1f", value);
				} catch (NumberFormatException e) {
					return "0.0";
				}
			}
		});

		item = lds.get("TS");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					// if ((buffer.get(0) & 0x40) != 0)
					if ((buff[0] & 0x40) != 0)
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("Tilt", "System");
					else
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("No Tilt", "System");
				} catch (Exception e) {
					return "";
				}
			}
		});

		item = lds.get("ERF");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					bb.clear();
					// int length = buffer.getLength();
					int length = buff.length;
					for (int i = 0; i < length; i++) {
						// bb.put(buffer.get(i));
						bb.put(buff[i]);
					}
					bb.flip();
					int value = Integer.valueOf(cs.decode(bb).toString(), 16);

					if ((value & 0x0001) == 1)
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("Running", "System");
					else
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("Stopped", "System");
				} catch (NumberFormatException e) {
					return "";
				}
			}
		});

		item = lds.get("IS");
		item.setCalc(new MyLiveDataItemCalc(item) {

			@Override
			protected String calc() {
				try {
					// if ((buffer.get(0) & 0x40) != 0)
					if ((buff[0] & 0x40) != 0)
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("Idle", "System");
					else
						return PowertrainDataStreamECU200.this.getDB()
								.queryText("Not Idle", "System");
				} catch (Exception e) {
					return "";
				}
			}
		});
	}
}