package dnt.qingqi;

import java.io.IOException;

import dnt.diag.Settings;
import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class SplashActivity extends Activity {

	VehicleDB db;

	private class BackgroundTask extends AsyncTask<Void, Void, Boolean> {

		String errMsg;

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				Settings.language = Settings.zh_CN;
				db = new VehicleDB(SplashActivity.this);
				Commbox box = new Commbox();
				App app = (App) getApplicationContext();
				app.setResources(db, box);
				return true;
			} catch (IOException e) {
				errMsg = e.getMessage();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(
						SplashActivity.this,
						String.format("Open database fail! error message = %s",
								errMsg), Toast.LENGTH_LONG).show();
			} else {
				Intent intent = new Intent(SplashActivity.this,
						MainActivity.class);
				SplashActivity.this.startActivity(intent);
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		new BackgroundTask().execute();
	}
}
