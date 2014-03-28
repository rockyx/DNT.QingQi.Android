package dnt.qingqi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.mikuni.PowertrainECU200;
import dnt.diag.ecu.mikuni.PowertrainModel;
import dnt.diag.ecu.mikuni.PowertrainVersion;

public class ModelOnMikuniECU200 extends AsyncTask<String, Void, Void> {
	private Context context;
	private ListView listView;
	private PowertrainECU200 ecu;
	private String model;
	private List<String> arrays;
	private App app;

	public ModelOnMikuniECU200(Context context, ListView listView) {
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
		this.model = params[0];

		arrays = new ArrayList<String>();

		arrays.add(app.ReadTroubleCode);
		arrays.add(app.ClearTroubleCode);
		arrays.add(app.ReadDataStream);
		arrays.add(app.TPSIdleAdjustment);
		arrays.add(app.ISCLearnValueInitialize);
		arrays.add(app.LongTermLearnValueZoneInitialization);
		arrays.add(app.EcuVersion);

		if (model.equals(app.QM200GY_F))
			ecu = new PowertrainECU200(app.getDB(), app.getCommbox(),
					PowertrainModel.QM200GY_F);
		else if (model.equals(app.QM200_3D))
			ecu = new PowertrainECU200(app.getDB(), app.getCommbox(),
					PowertrainModel.QM200_3D);
		else if (model.equals(app.QM200J_3L)) {
			ecu = new PowertrainECU200(app.getDB(), app.getCommbox(),
					PowertrainModel.QM200J_3L);
		} else {
			return null; // may be throw a message?
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
				String text = (String) ((TextView) view).getText();
				if (text.equals(app.ReadTroubleCode)) {
					new ModelReadTroubleCodeFunc(context).run();
				} else if (text.equals(app.ClearTroubleCode)) {
					new ModelClearTroubleCodeFunc(context).execute();
				} else if (text.equals(app.ReadDataStream)) {
					new ModelDataStreamFunc(context, model).run();
				} else if (text.equals(app.TPSIdleAdjustment)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.disconnectCommbox();
								app.connectCommbox();
								ecu.channelInit();

								ecu.tpsIdleSetting();
								return app.TPSIdleSettingSuccess;
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
				} else if (text.equals(app.ISCLearnValueInitialize)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.disconnectCommbox();
								app.connectCommbox();
								ecu.channelInit();

								ecu.iscLearnValueInitialization();
								return app.ISCLearnValueInitializationSuccess;
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
				} else if (text
						.equals(app.LongTermLearnValueZoneInitialization)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.disconnectCommbox();
								app.connectCommbox();
								ecu.channelInit();

								ecu.longTermLearnValueZoneInitialization();
								return app.LongTermLearnValueZoneInitializationSuccess;
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
				} else if (text.equals(app.EcuVersion)) {
					new AsyncTask<Void, Void, String>() {
						PowertrainVersion version;

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.ReadingECUVersion);

						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.disconnectCommbox();
								app.connectCommbox();
								ecu.channelInit();

								version = ecu.readVersion();
								return String.format("%s\n%s\nV%s",
										version.model, version.hardware,
										version.software);
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
