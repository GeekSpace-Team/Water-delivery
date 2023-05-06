package com.android.waterdelivery;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSService extends Service {

	public static final String RECEIVE_CMD = "receive";
	public static final String SEND_CMD = "send";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Bundle extras = intent.getExtras();
			String cmd = extras.getString("cmd");
			String[] sms_bodys = extras.getStringArray("sms_body");
			long time = extras.getLong("time");
			if (cmd.equals(RECEIVE_CMD)) {
				saveSms(sms_bodys[0], time);
			}
			if (cmd.equals(SEND_CMD)) {
				String phone_number = SettingsHelper.getOutPhone(getApplicationContext());
				if (phone_number == null)
					return START_NOT_STICKY;
				Uri orderUri = Uri.parse(extras.getString("order_uri"));
				//Log.i("SMSService", "Send to "+phone_number+": "+sms_bodys[0] + "; " + sms_bodys[1]);
				for (String sms_body : sms_bodys)
					sendSMS(phone_number, sms_body, orderUri);
			}
		}
		return START_NOT_STICKY;
	}

	private Uri saveSms(String sms_body, long time) {
		ContentValues cv = SMSParser.parse(sms_body, getString(R.string.delimiter));
		cv.put(OrdersTable.COLUMN_ORDER_TIME, time);
		//time
		return getApplicationContext().getContentResolver().insert(OrdersContentProvider.ORDERS_URI, cv);
	}

	private void sendSMS(String phoneNumber, String message, Uri orderUri)
	{        
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		//---when the SMS has been sent---
		registerReceiver(new UriHoldBroadcastReceiver(orderUri){
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("SMSService", "sentPI onReceive");
				ContentValues values = new ContentValues();
				Uri uri = getUri();//Uri.parse(intent.getExtras().getString("order_uri"));
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					values.put(OrdersTable.COLUMN_SMS_STATE, OrdersContentProvider.STATE_SMS_SENDED);
					Toast.makeText(getBaseContext(), "SMS sent", 
							Toast.LENGTH_SHORT).show();
					int deleted = getContentResolver().delete(uri, null, null);
					Log.i(getClass().getName(), String.format("Deleted: %d", deleted));
					Log.i("SMSService", "Send ok");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_NO_SERVICE:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					values.put(OrdersTable.COLUMN_SMS_STATE, OrdersContentProvider.STATE_SMS_ERROR);
					Toast.makeText(getBaseContext(), "SMS Sending error!", 
							Toast.LENGTH_LONG).show();
					Log.i("SMSService", "Send fail");
					break;
				}
				getContentResolver().update(uri, values, null, null);
				unregisterReceiver(this);
			}
		}, new IntentFilter(SENT));

		//---when the SMS has been delivered---
		registerReceiver(new UriHoldBroadcastReceiver(orderUri){
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i("SMSService", "deliveredPI onReceive");
				Uri uri = getUri();//Uri.parse(intent.getExtras().getString("order_uri"));
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Log.i("SMSService", "Deliver: RESULT_OK");
					Toast.makeText(getBaseContext(), "SMS delivered", 
							Toast.LENGTH_SHORT).show();
					int deleted = getContentResolver().delete(uri, null, null);
					Log.i(getClass().getName(), String.format("Delted: %d", deleted));
					break;
				case Activity.RESULT_CANCELED:
					Log.i("SMSService", "Deliver: RESULT_CANCELED");
					Toast.makeText(getBaseContext(), "SMS not delivered", 
							Toast.LENGTH_SHORT).show();
					ContentValues values = new ContentValues();
					values.put(OrdersTable.COLUMN_SMS_STATE, OrdersContentProvider.STATE_SMS_ERROR);
					getContentResolver().update(uri, values, null, null);
					break;                        
				}
				unregisterReceiver(this);
			}
		}, new IntentFilter(DELIVERED));        

		SmsManager sms = SmsManager.getDefault();
		Log.i("SMSService", "message length: "+String.valueOf(message.length()));
		Log.i("SMSService", "message size: "+String.valueOf(message.toCharArray().length));
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
	}

	private static abstract class UriHoldBroadcastReceiver extends BroadcastReceiver {
		private Uri mUri;
		
		UriHoldBroadcastReceiver(Uri uri) {
			mUri = uri;
		}
		
		public Uri getUri() {
			return mUri;
		}
	}
}
