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
			break;
		default:
			throw new DiagException("Unsupport model");
		}

		for (LiveDataItem item : lds) {
			item.setFormattedCommand(getFormat().pack(item.getCommand()));
			item.setEnabled(true);
		}

		setReadInterval(Timer.fromMilliseconds(10));
	}

	@Override
	protected void initCalcFunctions() {
		LiveDataList lds = getLiveDataItems();
		LiveDataItem item = lds.get("ER");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				int value = buffer.get(1) & 0xFF;
				value *= 256;
				value += buffer.get(2) & 0xFF;
				value /= 256;
				value *= 500;
				return Integer.toString(value);
			}
		});

		item = lds.get("BV");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				double value = buffer.get(1) & 0xFF;
				value *= 256;
				value += buffer.get(2) & 0xFF;
				value *= 18.75;
				value /= 65536;
				return String.format(Locale.getDefault(), "%.1f", value);
			}
		});
		
		item = lds.get("TPS");
		item.setCalc(new LiveDataItemCalc(item){

			@Override
			public String calc() {
				double value = buffer.get(1) & 0xFF;
				value *= 256;
				value += buffer.get(2) & 0xFF;
				value *= 100;
				value /= 4096;
				return String.format(Locale.getDefault(), "%.1f", value);
			}
		});
		
		item = lds.get("ET");
		item.setCalc(new LiveDataItemCalc(item){

			@Override
			public String calc() {
				double value = buffer.get(1) & 0xFF;
				value *= 256;
				value += buffer.get(2) & 0xFF;
				value /= 256;
				value -= 50;
				return String.format(Locale.getDefault(), "%.1f", value);
			}
			
		});
		
		item = lds.get("TS");
		item.setCalc(new LiveDataItemCalc(item) {

			@Override
			public String calc() {
				if ((buffer.get(1) & 0x02) != 0) {
					return getDB().queryText("Tilt", "System");
				} else {
					return getDB().queryText("No Tilt", "System");
				}
			}
			
		});
		
		item = lds.get("ERF");
		item.setCalc(new LiveDataItemCalc(item){

			@Override
			public String calc() {
				if (buffer.get(1) == 1) {
					return getDB().queryText("Running", "System");
				} else {
					return getDB().queryText("Stopped", "System");
				}
			}
			
		});
	}
}
