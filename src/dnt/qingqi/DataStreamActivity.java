package dnt.qingqi;

import java.io.IOException;
import java.util.List;
import dnt.diag.channel.ChannelException;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataList;
import dnt.diag.data.LiveDataValueChanged;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DataStreamActivity extends Activity {
	private App app;
	private boolean isFreeze;
	private TableLayout layout;
	private DataStreamFunction ds;
	private LiveDataValueChanged valueChanged;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.data_stream);

		layout = (TableLayout) findViewById(R.id.data_stream_table);
		layout.removeAllViews();

		app = (App) getApplicationContext();

		valueChanged = new LiveDataValueChanged() {

			@Override
			public void OnValueChanged(final LiveDataItem item) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						TableRow row = (TableRow) layout.getChildAt(item
								.getPosition());
						TextView view = (TextView) row.getChildAt(1);
						view.setText(item.getValue());
						if (item.isOutOfRange()) {
							view.setTextColor(android.graphics.Color.RED);
						} else {
							view.setTextColor(android.graphics.Color.BLUE);
						}
					}
				});
			}
		};

		isFreeze = getIntent().getExtras().getBoolean("IsFreeze");
		ds = isFreeze ? app.getECU().getFreezeStream() : app.getECU()
				.getDataStream();
		getIntent().getExtras().getString("Model");

		// run model funcs
		new AsyncTask<Void, Void, String>() {

			@Override
			protected void onPreExecute() {
				app.showStatus(DataStreamActivity.this, app.Communicating);
			}

			@Override
			protected String doInBackground(Void... params) {
				try {

					app.disconnectCommbox();
					app.connectCommbox();

					app.getECU().channelInit();

					LiveDataList lds = ds.getLiveDataItems();
					
					for (LiveDataItem item : lds) {
						item.setDisplay(true);
					}

					lds.makeDisplayItems();

					ds.start();

					return null;
				} catch (ChannelException ex) {
					try {
						ds.stop();
					} catch (ChannelException e) {
//						e.printStackTrace();
					}
					return ex.getMessage();
				} catch (DiagException ex) {
					try {
						ds.stop();
					} catch (ChannelException e) {
//						e.printStackTrace();
					}
					return ex.getMessage();
				} catch (IOException ex) {
					try {
						ds.stop();
					} catch (ChannelException e) {
//						e.printStackTrace();
					}
					return app.OpenCommboxFail;
				}
			}

			@Override
			protected void onPostExecute(String result) {
				if (result != null) {
					showFault(result);
				} else {
					preparePage();
				}
			}
		}.execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			app.showStatus(this, app.Communicating);

			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					try {
						ds.stop();
					} catch (ChannelException e) {
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					app.hideStatus();
					DataStreamActivity.this.finish();
				}

			}.execute();
			return true;
		}
		return super.onKeyDown(keyCode, e);
	}

	private void preparePage() {
		LiveDataList lds = ds.getLiveDataItems();

		List<LiveDataItem> items = lds.getDisplayItems();
		int i = 0;
		
		layout.removeAllViews();
		for (LiveDataItem item : items) {
			item.setPosition(i++);
			TextView content = new TextView(DataStreamActivity.this);
			content.setText(item.getContent());
			content.setMaxWidth(220);

			TextView unit = new TextView(DataStreamActivity.this);
			unit.setText(item.getUnit());

			TextView value = new TextView(DataStreamActivity.this);
			value.setText(item.getValue());
			value.setTextColor(android.graphics.Color.BLUE);

			TableRow row = new TableRow(DataStreamActivity.this);
			row.addView(content);
			row.addView(value);
			row.addView(unit);
			layout.addView(row);

			item.setOnValueChanged(valueChanged);
		}

		app.hideStatus();
	}

	private void showFault(String result) {
		app.showFatal(this, result, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				DataStreamActivity.this.finish();
			}
		});
	}
}
