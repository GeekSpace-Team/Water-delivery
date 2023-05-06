package com.android.waterdelivery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSMonitor extends BroadcastReceiver {
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && intent.getAction() != null &&
				ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
			Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
			Log.i(this.getClass().getName(), String.format("Len1: %d", pduArray.length));
			SmsMessage[] messages = new SmsMessage[pduArray.length];
			Log.i(this.getClass().getName(), String.format("Len2: %d", messages.length));
			//Log.i(this.getClass().getName(), messages[0].toString());
			for (int i = 0; i < pduArray.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
			}
			String sms_from = messages[0].getDisplayOriginatingAddress();
			if (sms_from.equalsIgnoreCase(SettingsHelper.getIncomPhone(context))) {
				StringBuilder bodyText = new StringBuilder();
				for (int i = 0; i < messages.length; i++) {
			      bodyText.append(messages[i].getMessageBody());
			    }
				String body = bodyText.toString();
				Intent mIntent = new Intent(context, SMSService.class);
				mIntent.putExtra("cmd", SMSService.RECEIVE_CMD);
				mIntent.putExtra("sms_body", new String[]{body});
				mIntent.putExtra("time", messages[0].getTimestampMillis());
				context.startService(mIntent);

				abortBroadcast();
			}
		}
	}

}
