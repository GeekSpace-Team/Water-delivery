package com.android.waterdelivery;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WaterDeliveryDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "waterdelivery.db";
	private static final int DATABASE_VERSION = 1;
	private static final DatabaseTable[] TABLES = {new OrdersTable(), new SettingsTable()}; 

	public static DatabaseTable getTable(String name) {
		for (DatabaseTable t : TABLES)
		{
			if (t.getName() == name)
				return t;
		}
		return null;
	}
	public static DatabaseTable[] getTables() {
		return TABLES;
	}
	
	public WaterDeliveryDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (DatabaseTable t : TABLES)
		{
			t.onCreate(db);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (DatabaseTable t : TABLES)
		{
			t.onUpgrade(db, oldVersion, newVersion);
		}
	}

	public Cursor getSelect(String id){
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT "+OrdersTable.COLUMN_LATITUDE+","+OrdersTable.COLUMN_LONGITUDE+", "+OrdersTable.COLUMN_PHONE+" FROM " + OrdersTable.TABLE_NAME + " WHERE "+OrdersTable.COLUMN_ID+" = '" + id + "'", null);
		return cursor;
	}

	public Cursor getOrderTime(String id){
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("SELECT "+OrdersTable.COLUMN_LATITUDE+","+OrdersTable.COLUMN_LONGITUDE+" FROM " + OrdersTable.TABLE_NAME + " WHERE "+OrdersTable.COLUMN_ID+" = '" + id + "'", null);
		return cursor;
	}

	public boolean updateStatus(String id, int smsState){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(OrdersTable.COLUMN_SMS_STATE,smsState);
		db.update(OrdersTable.TABLE_NAME,values,OrdersTable.COLUMN_ID+"=?",new String[]{id});
		return true;
	}

}
