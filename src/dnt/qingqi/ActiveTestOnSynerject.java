package dnt.qingqi;

import android.content.Context;
import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import dnt.diag.ecu.ActiveTestFunction;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.synerject.PowertrainActiveType;

public class ActiveTestOnSynerject extends AsyncTask<Void, Void, String> {

	private Context context;
	private String sub;
	private ActiveTestFunction activeTest;
	private App app;

	public ActiveTestOnSynerject(Context context, TextView noticeText,
			Button positionBtn, Button negativeBtn, String sub) {
		this.context = context;
		this.sub = sub;
		this.app = (App) context.getApplicationContext();
	}

	@Override
	protected String doInBackground(Void... params) {
		app.getECU().getDataStream();
		activeTest = app.getECU().getActiveTest();

		try {
			
			if (sub.equals(app.Injector))
				activeTest.execute(PowertrainActiveType.Injector.getValue());
			else if (sub.equals(app.IgnitionCoil))
				activeTest
						.execute(PowertrainActiveType.IgnitionCoil.getValue());
			else if (sub.equals(app.FuelPump))
				activeTest.execute(PowertrainActiveType.FuelPump.getValue());

			return null;
		} catch (DiagException e) {
			return e.getMessage();
		}
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			app.hideStatus();
			if (result != null) {
				app.showFatal(ActiveTestOnSynerject.this.context, result, null);
			}
			((Activity)context).finish();
		} catch (Exception e) {
			
		}

	}
}