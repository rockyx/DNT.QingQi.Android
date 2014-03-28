package dnt.qingqi;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DetailDataStreamActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detail_data_stream);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		String content = getIntent().getExtras().getString("Content");
		String value = getIntent().getExtras().getString("Value");
		String unit = getIntent().getExtras().getString("Unit");
		String defaultValue = getIntent().getExtras().getString("DefaultValue");
		String description = getIntent().getExtras().getString("Description");

		App app = (App) getApplicationContext();
		String[] arrays = new String[4];
		arrays[0] = content;
		arrays[1] = value + unit;
		arrays[2] = app.Range + " : " + defaultValue;
		arrays[3] = description;

		ListView listView = (ListView) findViewById(R.id.detail_data_stream_list);
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arrays));
	}

}
