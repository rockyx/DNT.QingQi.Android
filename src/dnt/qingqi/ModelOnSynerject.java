package dnt.qingqi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.synerject.Powertrain;
import dnt.diag.ecu.synerject.PowertrainModel;

public class ModelOnSynerject extends AsyncTask<String, Void, Void> {

	private Context context;
	private ListView listView;
	private App app;
	private String model;
	private Powertrain ecu;
	private List<String> arrays;

	public ModelOnSynerject(Context context, ListView listView) {
		this.context = context;
		this.listView = listView;
		this.app = (App) context.getApplicationContext();
	}

	@Override
	protected void onPreExecute() {
		app.showStatus(context, app.LoadingPleaseWait);
	}

	@Override
	protected Void doInBackground(String... params) {
		model = params[0];
		arrays = new ArrayList<String>();
		if (model.equals(app.QM125T_8H)) {
			arrays.add(app.ReadTroubleCode);
			arrays.add(app.ClearTroubleCode);
			arrays.add(app.ReadDataStream);
			arrays.add(app.EcuVersion);
			ecu = new Powertrain(app.getDB(), app.getCommbox(),
					PowertrainModel.QM125T_8H);
		} else if (model.equals(app.QM250GY)) {
			arrays.add(app.ReadTroubleCode);
			arrays.add(app.ClearTroubleCode);
			arrays.add(app.ReadDataStream);
			arrays.add(app.ActiveTest);
			arrays.add(app.EcuVersion);
			ecu = new Powertrain(app.getDB(), app.getCommbox(),
					PowertrainModel.QM250GY);
		} else if (model.equals(app.QM250T)) {
			arrays.add(app.ReadTroubleCode);
			arrays.add(app.ClearTroubleCode);
			arrays.add(app.ReadDataStream);
			arrays.add(app.ActiveTest);
			arrays.add(app.EcuVersion);
			ecu = new Powertrain(app.getDB(), app.getCommbox(),
					PowertrainModel.QM250T);
		} else {
			return null; // throw exception?
		}

		app.setECU(ecu);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		listView.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, arrays));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String name = (String) ((TextView) view).getText();
				if (name.equals(app.ReadTroubleCode)) {
					new ModelReadTroubleCodeFunc(context).run();
				} else if (name.equals(app.ClearTroubleCode)) {
					new ModelClearTroubleCodeFunc(context).execute();
				} else if (name.equals(app.ReadDataStream)) {
					new ModelDataStreamFunc(context, model).run();
				} else if (name.equals(app.ActiveTest)) {
					Intent intent = new Intent(context,
							ActiveTestActivity.class);
					intent.putExtra("Model", model);
					context.startActivity(intent);
				} else if (name.equals(app.EcuVersion)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context,
									app.ReadingECUVersion);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.disconnectCommbox();
								app.connectCommbox();
								ecu.channelInit();
								return ecu.readVersion();
							} catch (DiagException ex) {
								return ex.getMessage();
							} catch (IOException ex) {
								return app.OpenCommboxFail;
							}
						}

						@Override
						protected void onPostExecute(String result) {
							app.hideStatus();
							app.showFatal(context, result, null);
						}
					}.execute();
				}
			}
		});
		app.hideStatus();
	}
}
