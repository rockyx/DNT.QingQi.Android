package dnt.qingqi;

import android.content.Context;
import android.content.Intent;

public class ModelDataStreamFunc implements Delegate {

	private Context context;
	private String model;

	public ModelDataStreamFunc(Context context, String model) {
		this.context = context;
		this.model = model;
	}
	
	@Override
	public void run() {
		Intent intent = new Intent(context, DataStreamSelectedActivity.class);
		intent.putExtra("Model", model);
		context.startActivity(intent);
	}
}
