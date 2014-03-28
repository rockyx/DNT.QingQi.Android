package dnt.qingqi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DataStreamSelectedActivity extends Activity {
	private String model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.data_stream_selected);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		ListView listView = (ListView) findViewById(R.id.data_stream_selected_list);

		App app = (App) getApplicationContext();

		String[] arrays = new String[2];
		arrays[0] = app.DynamicDataStream;
		arrays[1] = app.StaticDataStream;

		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arrays));

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					new DataStreamFunc(DataStreamSelectedActivity.this, model, false).run();
					break;
				case 1:
					Intent intent = new Intent(DataStreamSelectedActivity.this,
							StaticDataStreamActivity.class);
					intent.putExtra("Model", model);
					startActivity(intent);
					break;
				}
			}
		});

		model = getIntent().getExtras().getString("Model");
	}
}
