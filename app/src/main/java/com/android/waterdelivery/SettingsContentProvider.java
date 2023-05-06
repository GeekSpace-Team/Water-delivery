package com.android.waterdelivery;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class SettingsContentProvider extends ContentProvider {

	// database
	private WaterDeliveryDatabaseHelper database;

	// Used for the UriMacher
	private static final int TABLE = 10;
	private static final int TABLE_ID = 20;

	private static final String AUTHORITY = "com.qnax.waterdelivery.settingsprovider";

	public static final Uri SETTINGS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + SettingsTable.TABLE_NAME);

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, "settings", TABLE);
		sURIMatcher.addURI(AUTHORITY, "settings/#", TABLE_ID);
	}
	@Override
	public boolean onCreate() {
		database = new WaterDeliveryDatabaseHelper(getContext());
		return false;
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(SettingsTable.TABLE_NAME);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case TABLE:
			break;
		case TABLE_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(SettingsTable.COLUMN_SET_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}
	@Override
	public String getType(Uri uri) {
		return null;
	}
	@Override
	public Uri insert(Uri uri, ContentValues values) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    long id = 0;
	    switch (uriType) {
	    case TABLE:
	      id = sqlDB.insert(SettingsTable.TABLE_NAME, null, values);
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return ContentUris.withAppendedId(uri, id);
	}
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case TABLE:
	      rowsDeleted = sqlDB.delete(SettingsTable.TABLE_NAME, selection,
	          selectionArgs);
	      break;
	    case TABLE_ID:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(SettingsTable.TABLE_NAME,
	        		SettingsTable.COLUMN_SET_ID + "=" + id, 
	            null);
	      } else {
	        rowsDeleted = sqlDB.delete(SettingsTable.TABLE_NAME,
	        		SettingsTable.COLUMN_SET_ID + "=" + id 
	            + " and " + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	}
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsUpdated = 0;
	    switch (uriType) {
	    case TABLE:
	      rowsUpdated = sqlDB.update(SettingsTable.TABLE_NAME, 
	          values, 
	          selection,
	          selectionArgs);
	      break;
	    case TABLE_ID:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(SettingsTable.TABLE_NAME, 
	            values,
	            SettingsTable.COLUMN_SET_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = sqlDB.update(SettingsTable.TABLE_NAME, 
	            values,
	            SettingsTable.COLUMN_SET_ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = SettingsTable.COLUMNS;
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}
}
