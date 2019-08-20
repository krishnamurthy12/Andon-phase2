package com.vvtech.andonphase2.broadcastreceivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//context.startActivity(new Intent(context,LoginActivity.class));

		Intent startIntent = context
				.getPackageManager()
				.getLaunchIntentForPackage(context.getPackageName());

		startIntent.setFlags(
				Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
						Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
		);
		context.startActivity(startIntent);
	}
}