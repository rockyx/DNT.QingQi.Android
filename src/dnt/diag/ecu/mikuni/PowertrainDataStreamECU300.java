package dnt.diag.ecu.mikuni;

import java.util.Locale;

import dnt.diag.Timer;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataItemCalc;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;

public class PowertrainDataStreamECU300 extends DataStreamFunction {
	private PowertrainModel model;

	public PowertrainDataStreamECU300(PowertrainECU300 ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());

		model = ecu.getModel();

		LiveDataList lds;
		switch (model) {
		case QM48QT_8:
			if (!queryLiveData("QingQi Mikuni ECU300")) {
				throw new DiagException("Cannot find live datas");
			}

			lds = getLiveDataItems();
			
			for (LiveDataItem item : lds) {
				item.setFormattedCommand(getFormat().pack(item.getCommand()));
				item.setEnabled(true);
			}
			lds.get("TS").setEnabled(false);

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
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];
			
			@Override
			protected String calc() {
				int value = (((buff[0] & 0xFF) * 256) + (buff[1] & 0xFF)) * 500 / 256;
				return Integer.toString(value);
			}
			
			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(1)) || (buff[1] != buffer.get(2))) {
					buff[0] = buffer.get(1);
					buff[1] = buffer.get(2);
					return true;
				}
				return false;
			}
		});

		item = lds.get("BV");
		item.setCalc(new LiveDataItemCalc(item) {
			private byte[] buff = new byte[2];
			
			@Override
			protected String calc() {
				double value = ((double)((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF))) * 18.75 / 65536;
				return String.format(Locale.getDefault(), "%.1f", value);				
			}

			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(1)) || (buff[1] != buffer.get(2))) {
					buff[0] = buffer.get(1);
					buff[1] = buffer.get(2);
					return true;
				}
				return false;
			}
		});
		
		item = lds.get("TPS");
		item.setCalc(new LiveDataItemCalc(item){
			private byte[] buff = new byte[2];
			
			@Override
			protected String calc() {
				double value = ((double)((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF))) * 100 / 4096;
				return String.format(Locale.getDefault(), "%.1f", value);
			}
			
			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(1)) || (buff[1] != buffer.get(2))) {
					buff[0] = buffer.get(1);
					buff[1] = buffer.get(2);
					return true;
				}
				return false;
			}
		});
		
		item = lds.get("ET");
		item.setCalc(new LiveDataItemCalc(item){
			private byte[] buff = new byte[2];

			@Override
			protected String calc() {
				double value = ((double)((buff[0] & 0xFF) * 256 + (buff[1] & 0xFF))) / 256 - 50;
				return String.format(Locale.getDefault(), "%.1f", value);
			}
			
			@Override
			protected boolean dataChanged() {
				if ((buff[0] != buffer.get(1)) || (buff[1] != buffer.get(2))) {
					buff[0] = buffer.get(1);
					buff[1] = buffer.get(2);
					return true;
				}
				return false;
			}
			
		});
		
		item = lds.get("TS");
		item.setCalc(new LiveDataItemCalc(item) {
			private int buff = 0;

			@Override
			protected String calc() {
				if (buff != 0) {
					return getDB().queryText("Tilt", "System");
				} else {
					return getDB().queryText("No Tilt", "System");
				}
			}
			
			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(1) & 0x02;
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
			
		});
		
		item = lds.get("ERF");
		item.setCalc(new LiveDataItemCalc(item){
			private int buff = 0;

			@Override
			protected String calc() {
				if (buff == 1) {
					return getDB().queryText("Running", "System");
				} else {
					return getDB().queryText("Stopped", "System");
				}
			}
			
			@Override
			protected boolean dataChanged() {
				int temp = buffer.get(1);
				if (buff != temp) {
					buff = temp;
					return true;
				}
				return false;
			}
			
		});
	}
}
