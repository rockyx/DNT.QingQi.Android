package dnt.qingqi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dnt.diag.ecu.DiagException;
import dnt.diag.ecu.mikuni.PowertrainECU300;
import dnt.diag.ecu.mikuni.PowertrainModel;
import dnt.diag.ecu.mikuni.PowertrainVersion;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ModelOnMikuniECU300 extends AsyncTask<String, Void, Void> {

	private Context context;
	private ListView listView;
	private String model;
	private App app;
	private PowertrainECU300 ecu;
	private List<String> arrays;

	public ModelOnMikuniECU300(Context context, ListView listView) {
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
		arrays.add(app.ReadTroubleCode);
		arrays.add(app.ClearTroubleCode);
		arrays.add(app.ReadDataStream);
		arrays.add(app.TPSIdleLearningValueSetting);
		arrays.add(app._02F_BLongTermLearningValueReset);
		arrays.add(app.DSVISCLearningValueSetting);
		arrays.add(app.EcuVersion);

		if (model.equals(app.QM48QT_8)) {
			ecu = new PowertrainECU300(app.getDB(), app.getCommbox(),
					PowertrainModel.QM48QT_8);
			app.setECU(ecu);
		}
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
				app.selectMenu(text);

				if (text.equals(app.ReadTroubleCode)) {
					new ModelReadTroubleCodeFunc(context).run();
				} else if (text.equals(app.ClearTroubleCode)) {
					new ModelClearTroubleCodeFunc(context).execute();
				} else if (text.equals(app.ReadDataStream)) {
					new ModelDataStreamFunc(context, model).run();
				} else if (text.equals(app.TPSIdleLearningValueSetting)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.connectCommbox();
								ecu.channelInit();

								ecu.tpsIdleLearningValueSetting();
								return app.TPSIdleSettingSuccess;
							} catch (DiagException ex) {
								return ex.getMessage();
							} catch (IOException ex) {
								return app.OpenCommboxFail;
							} finally {
								try {
									app.disconnectCommbox();
								} catch (IOException e) {
								}
							}

						}

						@Override
						protected void onPostExecute(String result) {
							app.hideStatus();
							app.backMenu();
							app.showFatal(context, result, null);
						}
					}.execute();
				} else if (text.equals(app._02F_BLongTermLearningValueReset)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.connectCommbox();
								ecu.channelInit();

								ecu.longTermLearningValueReset();
								return app.LongTermLearnValueZoneInitializationSuccess;
							} catch (DiagException ex) {
								return ex.getMessage();
							} catch (IOException ex) {
								return app.OpenCommboxFail;
							} finally {
								try {
									app.disconnectCommbox();
								} catch (IOException e) {
								}
							}
						}

						@Override
						protected void onPostExecute(String result) {
							app.hideStatus();
							app.backMenu();
							app.showFatal(context, result, null);
						}
					}.execute();
				} else if (text.equals(app.DSVISCLearningValueSetting)) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.connectCommbox();
								ecu.channelInit();

								ecu.dsvISCLearningValueSetting();
								return app.DSVISCLearningValueSettingSuccess;
							} catch (DiagException ex) {
								return ex.getMessage();
							} catch (IOException ex) {
								return app.OpenCommboxFail;
							} finally {
								try {
									app.disconnectCommbox();
								} catch (IOException e) {
								}
							}
						}

						@Override
						protected void onPostExecute(String result) {
							app.hideStatus();
							app.backMenu();
							app.showFatal(context, result, null);
						}
					}.execute();
				} else if (text.equals(app.EcuVersion)) {
					new AsyncTask<Void, Void, String>() {
						PowertrainVersion version;

						@Override
						protected void onPreExecute() {
							app.showStatus(context, app.Communicating);
						}

						@Override
						protected String doInBackground(Void... params) {
							try {
								app.connectCommbox();
								ecu.channelInit();

								version = ecu.readVersion();
								return String.format("%s:%s\n%s:%s\n%s:%s",
										app.CustomerID, version.model, 
										app.ManageNumber, version.hardware,
										app.SoftwareVersion, version.software);
//								return version.software;
							} catch (DiagException ex) {
								return ex.getMessage();
							} catch (IOException ex) {
								return app.OpenCommboxFail;
							} finally {
								try {
									app.disconnectCommbox();
								} catch (IOException e) {
								}
							}
						}

						@Override
						protected void onPostExecute(String result) {
							app.hideStatus();
							app.backMenu();
							app.showFatal(context, result, null);
						}
					}.execute();
				}
			}
		});
		app.hideStatus();
	}
}
