package com.android.waterdelivery;

import static com.android.waterdelivery.App.CHANNEL_ID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class OrdersContentProvider extends ContentProvider {

	private static final String SMS_FROMAT = "%d/%s/%d/%d/%d/%d/%d/%f/%f";
	//private static final String SMS_FROMAT = "%dn%sn%dn%dn%dn%dn%d";

	// database
	private WaterDeliveryDatabaseHelper database;

	// Order states
	public static final int STATE_NEW = 0;
	public static final int STATE_CHANGE = 1;
	public static final int STATE_CANCEL = 2;
	public static final int STATE_NA = 3;
	public static final int STATE_ATHOME = 4;
	public static final int STATE_PRIORITY = 5;
	public static final int STATE_REDO = 6;

	// Delivery states
	public static final int STATE_DOK = 0;
	public static final int STATE_DCHANGED = 1;
	public static final int STATE_DCANCELED = 2;
	public static final int STATE_DNA = 3;
	public static final int STATE_DPART = 4;

	// SMS states
	public static final int STATE_SMS_NOT_SENDED = 0;
	public static final int STATE_SMS_ERROR = 1;
	public static final int STATE_SMS_DELIVERED = 2;
	public static final int STATE_SMS_SENDED = 3;
	public static final int STATE_SMS_EXPIRE_TIME = 4;

	// Used for the UriMacher
	private static final int TABLE = 10;
	private static final int TABLE_ID = 20;

	public static final String AUTHORITY = "com.qnax.waterdelivery.ordersprovider";

	public static final Uri ORDERS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + OrdersTable.TABLE_NAME);


	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, "*", TABLE);
		sURIMatcher.addURI(AUTHORITY, "*/#", TABLE_ID);
	}

	/*private static class MyContentObserver extends ContentObserver {

		private Uri mUri = null;
		
		public MyContentObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			if (mUri != null) {
				Log.i(this.getClass().getName(), "onChange: " + mUri.toString());
			}
		}

		public void setUri(Uri uri) {
			mUri = uri;
		}
	}
	
	private static MyContentObserver mObserver = new MyContentObserver(null);*/
	
	private void showNotification(String title, String text, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
		Context context = getContext();
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Notification.Builder builder = new Notification.Builder(context)
		.setContentTitle(title)
		.setContentText(text)
		.setContentIntent(contentIntent)
		.setSmallIcon(R.drawable.ic_launcher)
		.setAutoCancel(true)
		.setSound(soundUri)
		.setTicker(title);

		NotificationManager mNotificationManager  = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					title,
					title,
					NotificationManager.IMPORTANCE_HIGH);
			mNotificationManager.createNotificationChannel(channel);
			builder.setChannelId(title);
		}

		final int min = 1;
		final int max = 1000;
		final int random = new Random().nextInt((max - min) + 1) + min;

		mNotificationManager.notify(random, builder.build());
		/*getContext().getContentResolver().unregisterContentObserver(mObserver);
		mObserver.setUri(uri);
		getContext().getContentResolver().registerContentObserver(uri, false, mObserver);*/
	}

	private void createNotificationChannel(Notification notification) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library

	}

	private String getTableName(Uri uri) {
		List<String> path = uri.getPathSegments();
		if (path.size() > 2)
			throw new IllegalArgumentException("Unknown URI: " + uri);

		String table = path.get(0);
		/*for (DatabaseTable t : WaterDeliveryDatabaseHelper.getTables()) {
			if (table.equals(t.getName()))
				return table;
		}*/
		if (table.equals(OrdersTable.TABLE_NAME))
			return table;
		throw new IllegalArgumentException("Unknown table: " + table);
	}

	@Override
	public boolean onCreate() {
		database = new WaterDeliveryDatabaseHelper(getContext());
		return false;
	}

	@Override
	synchronized public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case TABLE:
			break;
		case TABLE_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(OrdersTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		String table = getTableName(uri);
		// Check if the caller has requested a column which does not exists
		checkColumns(table, projection);

		// Set the table
		queryBuilder.setTables(table);
		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	private void checkColumns(String table, String[] projection) {
		String[] available = null;
		for (DatabaseTable t : WaterDeliveryDatabaseHelper.getTables())
		{
			if (table.equals(t.getName()))
				available = t.getColumns();
		}

		if (available == null)
			throw new IllegalArgumentException("Unknown table");

		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case TABLE:
			return "vnd.android.cursor.dir/vnd.qnax.order";
		case TABLE_ID:
			return "vnd.android.cursor.item/vnd.qnax.order";
		}
		return null;
	}

	@Override
	synchronized public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long id = -1;
		switch (uriType) {
		case TABLE:
			SQLiteDatabase sqlDB = database.getWritableDatabase();
			String table = getTableName(uri);


			int state = values.getAsInteger(OrdersTable.COLUMN_ORDER_STATE);
			String code = values.getAsString(OrdersTable.COLUMN_CLIENT_CODE);
			switch (state) {
			case STATE_CANCEL:
			case STATE_NA:
				Cursor c1 = sqlDB.query(table, null, OrdersTable.COLUMN_CLIENT_CODE + '=' + code + " AND " + OrdersTable.COLUMN_DELIVERY_STATE +" is null", null, null, null, null);
				if (c1.getCount() == 0) {
					//TODO: What to do?
				}
				else {
					id = -2;
					c1.moveToFirst();
					sqlDB.delete(table, OrdersTable.COLUMN_ID + "=" + c1.getString(c1.getColumnIndex(OrdersTable.COLUMN_ID)), null);
				}
				c1.close();
				break;
			case STATE_NEW:
			case STATE_ATHOME:
			case STATE_PRIORITY:
			case STATE_REDO:
			case STATE_CHANGE:
				Cursor c = sqlDB.query(table, null, OrdersTable.COLUMN_CLIENT_CODE + '=' + code + " AND " + OrdersTable.COLUMN_DELIVERY_STATE +" is null", null, null, null, null);
				if (c.getCount() == 0) {
					id = sqlDB.insert(table, null, values);
				}
				else {
					c.moveToFirst();
					id = c.getLong(c.getColumnIndex(OrdersTable.COLUMN_ID));
					if (state == STATE_CHANGE)
						values.remove(OrdersTable.COLUMN_ORDER_STATE);
					Log.i("OrdersContentProvider", "values: " + values);
					ContentValues c_vals = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(c, c_vals);
					Log.i("OrdersContentProvider", "cursor: " + c_vals);
					int updated = sqlDB.update(table, values, OrdersTable.COLUMN_ID + '=' + id, null);
					assert(updated == 1);
				}
				c.close();
				break;
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);

		if (id == -1) {
//			showNotification("Ошибка при обновлении заказов", "", ORDERS_URI);
			return null;
		}
		else
		{
			Uri uri_i = (id > 0) ? ContentUris.withAppendedId(uri, id) : uri;
//			showNotification("Обновление заказов", values.getAsString(OrdersTable.COLUMN_ADDR), uri_i);
			return uri_i;
		}
	}

	@Override
	synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		String table = getTableName(uri);
		switch (uriType) {
		case TABLE:
			rowsDeleted = sqlDB.delete(table, selection,
					selectionArgs);
			break;
		case TABLE_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(table,
						OrdersTable.COLUMN_ID + "=" + id, 
						null);
			} else {
				rowsDeleted = sqlDB.delete(table,
						OrdersTable.COLUMN_ID + "=" + id 
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
	synchronized public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		if (uriType != TABLE_ID)
			throw new IllegalArgumentException("Unknown URI: " + uri);

		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		String table = getTableName(uri);
		String id = uri.getLastPathSegment();

		String[] deliveredKeys = {OrdersTable.COLUMN_DELIVERY_STATE, OrdersTable.COLUMN_DELIVERED_COUNTS, OrdersTable.COLUMN_LATITUDE, OrdersTable.COLUMN_LONGITUDE};
		HashSet<String> deliveredKeysSet = new HashSet<String>(Arrays.asList(deliveredKeys));
		if (deliveredKeysSet.containsAll(values.keySet()) && values.keySet().containsAll(deliveredKeysSet))
		{
			Cursor c = sqlDB.query(OrdersTable.TABLE_NAME, null, OrdersTable.COLUMN_ID + "=" + id, null, null, null, null);
			c.moveToFirst();
			int[] orderedCounts = BlobUtils.blobToIntArr(c.getBlob(c.getColumnIndex(OrdersTable.COLUMN_ORDERED_COUNTS)));
			String client_code = c.getString(c.getColumnIndex(OrdersTable.COLUMN_CLIENT_CODE));
			int[] deliveredCounts = BlobUtils.blobToIntArr(values.getAsByteArray(OrdersTable.COLUMN_DELIVERED_COUNTS));
			int state = values.getAsInteger(OrdersTable.COLUMN_DELIVERY_STATE);	
			String sms_body2 = null;
			switch (state) {
			case STATE_DCHANGED:
				int[] deliveredCounts2 = deliveredCounts;
				deliveredCounts = BlobUtils.sub(orderedCounts, deliveredCounts);
				sms_body2 = String.format(SMS_FROMAT, STATE_DOK, client_code,
						deliveredCounts2[0],
						deliveredCounts2[1],
						deliveredCounts2[2],
						deliveredCounts2[3],
						deliveredCounts2[4],
						values.getAsDouble(OrdersTable.COLUMN_LATITUDE),
						values.getAsDouble(OrdersTable.COLUMN_LONGITUDE)
						);
				break;
			case STATE_DOK:
				deliveredCounts = orderedCounts;
				values.put(OrdersTable.COLUMN_DELIVERED_COUNTS,c.getBlob(c.getColumnIndex(OrdersTable.COLUMN_ORDERED_COUNTS)));
				break;
			case STATE_DCANCELED:
			case STATE_DNA:
				deliveredCounts = orderedCounts;
				values.putNull(OrdersTable.COLUMN_DELIVERED_COUNTS);
				break;
			case STATE_DPART:
				ContentValues newCV = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(c, newCV);
				int[] newOrderedCounts = BlobUtils.sub(orderedCounts, deliveredCounts);
				newCV.putNull(OrdersTable.COLUMN_DELIVERED_COUNTS);
				newCV.putNull(OrdersTable.COLUMN_DELIVERY_STATE);
				newCV.put(OrdersTable.COLUMN_ORDERED_COUNTS, BlobUtils.intArrToBlob(newOrderedCounts));
				newCV.remove(OrdersTable.COLUMN_ID);
				sqlDB.insert(table, null, newCV);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}	

			c.close();
			String sms_body = String.format(SMS_FROMAT, state, client_code,
					deliveredCounts[0],
					deliveredCounts[1],
					deliveredCounts[2],
					deliveredCounts[3],
					deliveredCounts[4],
					values.getAsDouble(OrdersTable.COLUMN_LATITUDE),
					values.getAsDouble(OrdersTable.COLUMN_LONGITUDE)
					);
			Log.i("OrdersContentProvider", sms_body);
			Context context = getContext();
			Intent mIntent = new Intent(context, SMSService.class);
			mIntent.putExtra("cmd", SMSService.SEND_CMD);
			mIntent.putExtra("order_uri", uri.toString());
			if (sms_body2 == null)
				mIntent.putExtra("sms_body", new String[]{sms_body});
			else
				mIntent.putExtra("sms_body", new String[]{sms_body,sms_body2});
			mIntent.putExtra("phone_number", SettingsHelper.getOutPhone(getContext()));
			context.startService(mIntent);
		} else if (values.size() == 1 && values.containsKey(OrdersTable.COLUMN_SMS_STATE)) {
			Log.i("OrdersContentProvider", "Update SMS state: " + values);
		} else
			throw new IllegalArgumentException("Wrong values");

		Log.i("OrdersContentProvider", OrdersTable.COLUMN_ID + "=" + id);
		rowsUpdated = sqlDB.update(table, 
				values,
				OrdersTable.COLUMN_ID + "=" + id, 
				null);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

}
