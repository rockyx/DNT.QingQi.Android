package dnt.qingqi;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ListView;

public class ModelFunctionsActivity extends Activity {

	private String model = "";
	private ListView listView;
	private App app = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.model_functions);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		app = (App) getApplicationContext();
		listView = (ListView) findViewById(R.id.model_functions_list);

		model = getIntent().getStringExtra("Model");

		if (model.equals(app.QM125T_8H) || model.equals(app.QM250GY)
				|| model.equals(app.QM250T)) {
			new ModelOnSynerject(this, listView).execute(model);
		} else if (model.equals(app.QM200GY_F) || model.equals(app.QM200_3D)
				|| model.equals(app.QM200J_3L)) {
			new ModelOnMikuniECU200(this, listView).execute(model);
		} else if (model.equals(app.QM250J_2L)) {
			new ModelOnVisteon(this, listView).execute(model);
		} else if (model.equals(app.QM48QT_8)) {
			new ModelOnMikuniECU300(this, listView).execute(model);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.backMenu();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
