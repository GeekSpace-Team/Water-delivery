package com.android.waterdelivery;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface DatabaseTable {
	void onCreate(SQLiteDatabase db);
	void onUpgrade(SQLiteDatabase db, int oldVersion,
		      int newVersion);
	String[] getColumns();
	String getName();

}
