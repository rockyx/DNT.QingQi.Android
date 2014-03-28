package dnt.qingqi;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceInfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.device_info);

		ListView list = (ListView) findViewById(R.id.device_info_list_view);

		App app = (App) getApplicationContext();
		String[] arrays = new String[2];
		arrays[0] = app.queryText("Version", "QingQi") + " : 2.0";
		arrays[1] = app.queryText("Device ID", "QingQi") + " : ABC";
		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arrays));
	}

}
