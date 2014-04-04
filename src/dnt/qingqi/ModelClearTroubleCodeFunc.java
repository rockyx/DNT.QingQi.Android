package dnt.qingqi;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import dnt.diag.ecu.DiagException;

class ModelClearTroubleCodeFunc extends AsyncTask<Void, Void, String> {
	Context context;
	App app;

	public ModelClearTroubleCodeFunc(Context context) {
		this.context = context;
		app = (App) context.getApplicationContext();
	}

	@Override
	protected void onPreExecute() {
		app.showStatus(context, app.ClearingTroubleCode);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			app.connectCommbox();
			app.getECU().channelInit();
			app.getECU().getTroubleCode().clear();
			return app.ClearTroubleCodeFinish;
		} catch (DiagException ex) {
			return ex.getMessage();
		} catch (IOException ex) {
			return app.OpenCommboxFail;
		} finally {
			try {
				app.disconnectCommbox();
			} catch (IOException e) {
			}
		}
	}

	@Override
	protected void onPostExecute(String result) {
		app.hideStatus();
		app.backMenu();
		app.showFatal(context, result, null);
	}
}
