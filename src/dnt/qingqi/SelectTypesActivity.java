package dnt.qingqi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectTypesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.select_types);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		App app = (App) getApplicationContext();
		List<String> arrays = new ArrayList<String>();
		arrays.add(app.QM125T_8H);
		arrays.add(app.QM200J_3L);
		arrays.add(app.QM200GY_F);
		arrays.add(app.QM200_3D);
		arrays.add(app.QM250J_2L);
		arrays.add(app.QM250GY);
		arrays.add(app.QM250T);
		arrays.add(app.QM48QT_8);

		ListView listView = (ListView) findViewById(R.id.select_types_list);

		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, arrays));

		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(SelectTypesActivity.this,
						ModelFunctionsActivity.class);
				String model = (String) ((TextView) view).getText();
				intent.putExtra("Model", model);
				startActivity(intent);
			}
		});
	}

}
