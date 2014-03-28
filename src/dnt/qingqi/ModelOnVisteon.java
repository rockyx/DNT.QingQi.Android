package dnt.qingqi;

import java.util.ArrayList;
import java.util.List;

import dnt.diag.ecu.visteon.PowertrainModel;
import dnt.diag.ecu.visteon.Powertrain;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ModelOnVisteon extends AsyncTask<String, Void, Void> {

	private Context context;
	private ListView listView;
	private App app;
	private String model;
	private Powertrain ecu;
	private List<String> arrays;

	public ModelOnVisteon(Context context, ListView listView) {
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
		arrays.add(app.ReadFreezeFrame);

		if (model.equals(app.QM250J_2L)) {
			ecu = new Powertrain(app.getDB(), app.getCommbox(),
					PowertrainModel.QM250J_2L);
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

				String text = (String) ((TextView) view).getText();
				if (text.equals(app.ReadTroubleCode)) {
					Intent intent = new Intent(context,
							TroubleCodeResultActivity.class);
					intent.putExtra("IsHistory", false);
					context.startActivity(intent);
				} else if (text.equals(app.ClearTroubleCode)) {
					new ModelClearTroubleCodeFunc(context).execute();
				} else if (text.equals(app.ReadDataStream)) {
					new ModelDataStreamFunc(context, model).run();
				} else if (text.equals(app.ReadFreezeFrame)) {
					new DataStreamFunc(context, model, true).run();
				}
			}
		});
		app.hideStatus();
	}
}
