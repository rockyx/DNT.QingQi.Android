package dnt.qingqi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dnt.diag.data.TroubleCodeItem;
import dnt.diag.ecu.DiagException;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TroubleCodeResultActivity extends Activity {

	private DialogInterface.OnClickListener listener;

	class ReadTroubleCodes extends AsyncTask<Void, Void, String> {
		@Override
		protected void onPreExecute() {
			app.showStatus(TroubleCodeResultActivity.this, app.Communicating);
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				app.connectCommbox();
				app.getECU().channelInit();
				if (!isHistory) {
					troubleCodes = app.getECU().getTroubleCode().readCurrent();
				} else {
					troubleCodes = app.getECU().getTroubleCode().readHistory();
				}
				return null;
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
			showResult(result);
		}
	}

	private App app;
	private ListView listView;
	private boolean isHistory;
	private List<TroubleCodeItem> troubleCodes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trouble_code_result);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		app = (App) getApplicationContext();

		listView = (ListView) findViewById(R.id.trouble_code_result_list);
		isHistory = getIntent().getExtras().getBoolean("IsHistory");
		listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				TroubleCodeResultActivity.this.finish();
			}
		};

		new ReadTroubleCodes().execute();
	}

	private void showTroubleCode() {
		if (troubleCodes == null || troubleCodes.size() == 0) {
//			listView.setAdapter(new ArrayAdapter<String>(this,
//					android.R.layout.simple_list_item_1,
//					new String[] { app.NoneTroubleCode }));
			 app.showFatal(this, app.NoneTroubleCode, listener);
		} else {
			List<String> arrays = new ArrayList<String>();

			for (TroubleCodeItem item : troubleCodes) {
				arrays.add(item.getCode() + ": " + item.getContent());
			}

			listView.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, arrays));

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Toast.makeText(TroubleCodeResultActivity.this,
							troubleCodes.get(position).getDescription(),
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private void showResult(String result) {
		app.hideStatus();
		app.backMenu();
		if (result == null) {
			showTroubleCode();
		} else {
			app.showFatal(this, result, listener);
		}

	}
}
