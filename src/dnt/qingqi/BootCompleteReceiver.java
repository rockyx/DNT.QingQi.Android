package dnt.qingqi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context cxt, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent activity = new Intent(cxt, SplashActivity.class);
			activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			cxt.startActivity(activity);
		}
	}

}
