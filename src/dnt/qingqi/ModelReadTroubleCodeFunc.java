package dnt.qingqi;

import android.content.Context;
import android.content.Intent;

public class ModelReadTroubleCodeFunc implements Delegate {

	private Context context;

	public ModelReadTroubleCodeFunc(Context context) {
		this.context = context;
	}
	
	@Override
	public void run() {
		Intent intent = new Intent(context, TroubleCodeMenuActivity.class);
		context.startActivity(intent);
	}
}
