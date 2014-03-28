package dnt.qingqi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ActiveTestActivity extends Activity {

	private ListView listView;
	private App app;
	private String model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.active_test);
		listView = (ListView) findViewById(R.id.active_test_list);

		listView.setOnItemClickListener(null);

		app = (App) getApplicationContext();

		model = getIntent().getExtras().getString("Model");

		if (model.equals(app.QM125T_8H) || model.equals(app.QM250GY)
				|| model.equals(app.QM250T)) {
			new AsyncTask<Void, Void, String>() {

				private String[] arrays;

				@Override
				protected String doInBackground(Void... params) {
					arrays = new String[3];
					arrays[0] = app.Injector;
					arrays[1] = app.IgnitionCoil;
					arrays[2] = app.FuelPump;

					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					listView.setAdapter(new ArrayAdapter<String>(
							ActiveTestActivity.this,
							android.R.layout.simple_expandable_list_item_1,
							arrays));
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							Intent intent = new Intent(ActiveTestActivity.this,
									ActiveTest2Activity.class);
							String sub = (String) ((TextView) view).getText();
							intent.putExtra("Model", model);
							intent.putExtra("Sub", sub);
							startActivity(intent);
						}
					});
				}
			}.execute();
		}
	}
}
