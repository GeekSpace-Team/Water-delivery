package com.android.waterdelivery;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OrdersTable implements DatabaseTable {

	  // Database table
	  public static final String TABLE_NAME = "orders";
	  
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_CLIENT_CODE = "client_code";
	  public static final String COLUMN_ORDER_STATE = "order_state";
	  public static final String COLUMN_DELIVERY_STATE = "delivery_state";
	  public static final String COLUMN_PHONE = "phone";
	  public static final String COLUMN_ORDERED_COUNTS = "ordered_counts";
	  public static final String COLUMN_DELIVERED_COUNTS = "delivered_counts";
	  public static final String COLUMN_ADDR = "address";
	  public static final String COLUMN_ORDER_TIME = "order_time";
	  public static final String COLUMN_DELIVER_TIME = "deliver_time";
	  public static final String COLUMN_SMS_STATE = "sms_state";
	  public static final String COLUMN_LATITUDE = "latitude";
	  public static final String COLUMN_LONGITUDE = "longitude";
	  
	  public static final String[] COLUMNS = {
		  COLUMN_ID,
		  COLUMN_CLIENT_CODE,
		  COLUMN_ORDER_STATE,
		  COLUMN_DELIVERY_STATE,
		  COLUMN_PHONE,
		  COLUMN_ORDERED_COUNTS,
		  COLUMN_DELIVERED_COUNTS,
		  COLUMN_ADDR,
		  COLUMN_ORDER_TIME,
		  COLUMN_DELIVER_TIME,
		  COLUMN_SMS_STATE,
		  COLUMN_LATITUDE,
		  COLUMN_LONGITUDE};

	  // Database creation SQL statement  
	  private static final String TABLE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_CLIENT_CODE + " integer not null, " 
	      + COLUMN_ORDER_STATE + " integer, " 
	      + COLUMN_DELIVERY_STATE + " integer, " 
	      + COLUMN_PHONE + " text, "
	      + COLUMN_ORDERED_COUNTS + " blob, "
	      + COLUMN_DELIVERED_COUNTS + " blob, "
	      + COLUMN_ADDR + " text, "
	      + COLUMN_ORDER_TIME + " integer, "
	      + COLUMN_DELIVER_TIME + " text, "
	      + COLUMN_SMS_STATE + " integer default 0,"
	      + COLUMN_LATITUDE + " text,"
	      + COLUMN_LONGITUDE + " text"
	      + ");";
	  
	  private static final String INDEX_CREATE = "create index "
			  + TABLE_NAME + "_" + COLUMN_CLIENT_CODE + "_INDEX"
			  + " on " + TABLE_NAME + "("
			  + COLUMN_CLIENT_CODE
			  + ");";

	  private static final String DATABASE_CREATE = TABLE_CREATE + INDEX_CREATE;
	  public void onCreate(SQLiteDatabase db) {
	    db.execSQL(DATABASE_CREATE);
	  }

	  public void onUpgrade(SQLiteDatabase db, int oldVersion,
	      int newVersion) {
	    Log.w(OrdersTable.class.getName(), "Upgrading database from version "
	        + oldVersion + " to " + newVersion
	        + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(db);
	  }



	public String[] getColumns() {
		return COLUMNS;
	}

	public String getName() {
		return TABLE_NAME;
	}



}
