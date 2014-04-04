package dnt.qingqi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TroubleCodeMenuActivity extends Activity {
	private ListView listView;
	private String[] arrays;
	private App app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trouble_code_menu);

		listView = (ListView) findViewById(R.id.trouble_code_menu_list);

		app = (App) getApplicationContext();

		arrays = new String[2];
		arrays[0] = app.ReadCurrentTroubleCode;
		arrays[1] = app.ReadHistoryTroubleCode;

		listView.setAdapter(new ArrayAdapter<String>(
				TroubleCodeMenuActivity.this,
				android.R.layout.simple_list_item_1, arrays));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String name = (String) ((TextView) view).getText();
				app.selectMenu(name);
				Intent intent = null;
				switch (position) {
				case 0: // Current Trouble Code
					intent = new Intent(TroubleCodeMenuActivity.this,
							TroubleCodeResultActivity.class);
					intent.putExtra("IsHistory", false);
					startActivity(intent);
					break;
				case 1: // History Trouble Code
					intent = new Intent(TroubleCodeMenuActivity.this,
							TroubleCodeResultActivity.class);
					intent.putExtra("IsHistory", true);
					startActivity(intent);

					break;
				}
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.backMenu();
		}
		return super.onKeyDown(keyCode, event);
	}

	
	
}
