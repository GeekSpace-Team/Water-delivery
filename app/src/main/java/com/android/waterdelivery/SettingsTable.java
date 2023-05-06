package com.android.waterdelivery;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SettingsTable implements DatabaseTable {

	  // Database table
	  public static final String TABLE_NAME = "settings";
	  
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_SET_ID = "set_id";
	  public static final String COLUMN_SET_VALUE = "set_value";
	  
	  public static final String[] COLUMNS = {COLUMN_ID,COLUMN_SET_ID,COLUMN_SET_VALUE};

	  public static final int INCOMIND_PHONE_NUM_ID = 10;
	  public static final int OUTGOING_PHONE_NUM_ID = 20;
	  
	  // Database creation SQL statement  
	  private static final String TABLE_CREATE = "create table " 
	      + TABLE_NAME
	      + " (" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_SET_ID + " integer not null unique on conflict replace, " 
	      + COLUMN_SET_VALUE + " text "
	      + ");";
	  
	  private static final String INDEX_CREATE = "create index "
			  + TABLE_NAME + "_" + COLUMN_SET_ID + "_INDEX"
			  + " on " + TABLE_NAME + "("
			  + COLUMN_SET_ID
			  + ");";

	  private static final String DATABASE_CREATE = TABLE_CREATE + INDEX_CREATE;
	  
	  public void onCreate(SQLiteDatabase db) {
	    db.execSQL(DATABASE_CREATE);
	  }

	  public void onUpgrade(SQLiteDatabase db, int oldVersion,
	      int newVersion) {
	    Log.w(this.getClass().getName(), "Upgrading database from version "
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
