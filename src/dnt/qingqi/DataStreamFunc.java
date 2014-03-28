package dnt.qingqi;

import android.content.Context;
import android.content.Intent;

public class DataStreamFunc implements Delegate {

	private Context context;
	private boolean isFreeze;
	private String model;

	public DataStreamFunc(Context context, String model, boolean isFreeze) {
		this.context = context;
		this.isFreeze = isFreeze;
		this.model = model;
	}
	
	@Override
	public void run() {
		Intent intent = new Intent(context, DataStreamActivity.class);
		intent.putExtra("IsFreeze", isFreeze);
		intent.putExtra("Model", model);
		context.startActivity(intent);				
	}
}
