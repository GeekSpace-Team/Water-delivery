package com.android.waterdelivery;

import android.content.Context;
import android.database.Cursor;

public class SettingsHelper {
	public static String getIncomPhone(Context context) {
		Cursor c = null;
		try {
			c = context.getContentResolver().query(
					SettingsContentProvider.SETTINGS_URI,
					new String[] {SettingsTable.COLUMN_SET_VALUE}, 
					SettingsTable.COLUMN_SET_ID + "=" + SettingsTable.INCOMIND_PHONE_NUM_ID, 
					null,null);
			if (c.getCount() > 0)
			{
				c.moveToFirst();
				return c.getString(0);
			}
		} finally {
			if (c != null)
				c.close();
		}
		return null;
	}
	public static String getOutPhone(Context context) {
		Cursor c = null;
		try {
			c = context.getContentResolver().query(
					SettingsContentProvider.SETTINGS_URI,
					new String[] {SettingsTable.COLUMN_SET_VALUE}, 
					SettingsTable.COLUMN_SET_ID + "=" + SettingsTable.OUTGOING_PHONE_NUM_ID, 
					null,null);
			if (c.getCount() > 0)
			{
				c.moveToFirst();
				return c.getString(0);
			}
		} finally {
			if (c != null)
				c.close();
		}
		return null;
	}
}
