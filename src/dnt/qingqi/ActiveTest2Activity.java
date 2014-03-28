package dnt.qingqi;

import java.io.IOException;

import dnt.diag.channel.ChannelException;
import dnt.diag.ecu.ActiveState;
import dnt.diag.ecu.ActiveTestFunction;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ActiveTest2Activity extends Activity {
	private TextView noticeText;
	private Button positionBtn;
	private Button negativeBtn;
	private String model;
	private String sub;
	private App app;
	private ActiveTestFunction activeTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.active_test_2);

		noticeText = (TextView) findViewById(R.id.active_test_notice_text);

		positionBtn = (Button) findViewById(R.id.active_test_btn_positive);

		negativeBtn = (Button) findViewById(R.id.active_test_btn_negative);

		model = getIntent().getExtras().getString("Model");
		sub = getIntent().getExtras().getString("Sub");

		app = (App) getApplicationContext();
		
		activeTest = app.getECU().getActiveTest();

		new AsyncTask<Void, Void, String>() {
			@Override
			protected void onPreExecute() {
				app.showStatus(ActiveTest2Activity.this, app.Communicating);
				if (sub.equals(app.Injector)) {
					positionBtn.setText(app.InjectorOnTest);
					negativeBtn.setText(app.InjectorOffTest);
					noticeText.setText(app.InjectorNotice);
				} else if (sub.equals(app.IgnitionCoil)) {
					positionBtn.setText(app.IgnitionCoilOnTest);
					negativeBtn.setText(app.IgnitionCoilOffTest);
					noticeText.setText(app.IgnitionCoilNotice);
				} else if (sub.equals(app.FuelPump)) {
					positionBtn.setText(app.FuelPumpOnTest);
					negativeBtn.setText(app.FuelPumpOffTest);
					noticeText.setText(app.FuelPumpNotice);

				}

				positionBtn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						activeTest.changeState(ActiveState.Positive);
					}
				});

				negativeBtn.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						activeTest.changeState(ActiveState.Negative);
					}
				});
			}

			@Override
			protected String doInBackground(Void... params) {
				try {
					app.disconnectCommbox();
					app.connectCommbox();
					app.getECU().channelInit();
				} catch (ChannelException e) {
					return e.getMessage();
				} catch (IOException e) {
					return e.getMessage();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				app.hideStatus();
				if (result != null) {
					app.showFatal(ActiveTest2Activity.this, result, null);
				} else {
					if (model.equals(app.QM250T) || model.equals(app.QM250GY)) {
						new ActiveTestOnSynerject(ActiveTest2Activity.this,
								noticeText, positionBtn, negativeBtn, sub)
								.execute();
					}
				}
			}
		}.execute();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.showStatus(this, app.Communicating);
			activeTest.changeState(ActiveState.Stop);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
