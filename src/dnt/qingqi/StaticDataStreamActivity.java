package dnt.qingqi;

import java.io.IOException;
import java.util.List;
import dnt.diag.channel.ChannelException;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.DataStreamFunction;
import dnt.diag.ecu.DiagException;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StaticDataStreamActivity extends Activity {

	class SDFunc extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			app.showStatus(StaticDataStreamActivity.this, app.Communicating);
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				app.disconnectCommbox();
				app.connectCommbox();

				app.getECU().channelInit();
				ds = app.getECU().getDataStream();

				LiveDataList lds = ds.getLiveDataItems();

				
				if (model.equals(app.QM200J_3L) || model.equals(app.QM200_3D) || model.equals(app.QM200J_3L)) {
					for (LiveDataItem item : lds) {
						item.setDisplay(true);
					}

					lds.get("TS").setDisplay(false);
					lds.get("ERF").setDisplay(false);
					lds.get("IS").setDisplay(false);
				} else if (model.equals(app.QM125T_8H) || model.equals(app.QM250GY) || model.equals(app.QM250T)) {
					for (LiveDataItem item : lds) {
						item.setDisplay(false);
					}

					lds.get("N").setDisplay(true);
					lds.get("VBK_MMV").setDisplay(true);
					lds.get("STATE_EFP").setDisplay(true);
					lds.get("TI_LAM_COR").setDisplay(true);
					lds.get("IGA_1").setDisplay(true);
					lds.get("VLS_UP_1").setDisplay(true);
					lds.get("AMP").setDisplay(true);
					lds.get("TCO").setDisplay(true);
					lds.get("TPS_MTC_1").setDisplay(true);
				} else {
					for (LiveDataItem item : lds) {
						item.setDisplay(true);
					}
				}

				lds.makeDisplayItems();

				ds.startOnce();

				List<LiveDataItem> items = ds.getLiveDataItems()
						.getDisplayItems();
				arrays = new String[items.size()];

				int i = 0;
				for (LiveDataItem item : items) {
					arrays[i++] = String.format("%s : %s %s",
							item.getContent(), item.getValue(), item.getUnit());
				}

				return null;
			} catch (DiagException ex) {
				return ex.getMessage();
			} catch (ChannelException ex) {
				return ex.getMessage();
			} catch (IOException ex) {
				return app.OpenCommboxFail;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			showResult(result);
		}

	}

	private ListView listView;
	private App app;
	private DataStreamFunction ds;
	private String model;
	private String[] arrays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.static_data_stream);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		listView = (ListView) findViewById(R.id.static_data_stream_list);

		app = (App) getApplicationContext();
		
		model = getIntent().getExtras().getString("Model");

		new SDFunc().execute();
	}

	private void showResult(String result) {
		if (result == null) {

			listView.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, arrays));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(StaticDataStreamActivity.this,
							DetailDataStreamActivity.class);

					LiveDataList lds = ds.getLiveDataItems();
					List<LiveDataItem> items = lds.getDisplayItems();
					LiveDataItem item = items.get(position);

					intent.putExtra("Content", item.getContent());
					intent.putExtra("Value", item.getValue());
					intent.putExtra("Unit", item.getUnit());
					intent.putExtra("DefaultValue", item.getDefaultValue());
					intent.putExtra("Description", item.getDescription());

					startActivity(intent);
				}
			});

			app.hideStatus();
		} else {
			app.showFatal(this, result, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					StaticDataStreamActivity.this.finish();
				}
			});
		}
	}
}
