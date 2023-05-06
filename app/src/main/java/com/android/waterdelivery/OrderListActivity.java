package com.android.waterdelivery;

import static com.android.waterdelivery.OrderDetails.SMS_REQUEST_CODE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class OrderListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private WaterDeliveryDatabaseHelper waterDB;
    public static class BindersList implements ViewBinder {
        private ArrayList<ViewBinder> list = new ArrayList<ViewBinder>();

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            for (ViewBinder vb : list) {
                if (vb.setViewValue(view, cursor, columnIndex))
                    return true;
            }
            return false;
        }

        void addBinder(ViewBinder vb) {
            list.add(vb);
        }
    }

    private Button reOrder;

    public static abstract class MyViewBinder implements ViewBinder {
        private int idx = -1;
        private String mColName;

        MyViewBinder(String column) {
            mColName = column;
        }

        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//            HorizontalScrollView horizontalScrollView=view.findViewById(R.id.horizontalScroll);
//            horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        // Do stuff
//                        Log.e("Click",columnIndex+"");
//                    }
//                    return false;
//                }
//            });
            if (idx == -1)
                idx = cursor.getColumnIndex(mColName);
            if (columnIndex != idx)
                return false;
            else
                return mySetViewValue(view, cursor, columnIndex);
        }

        public abstract boolean mySetViewValue(View view, Cursor cursor, int columnIndex);

        public int getIdx() {
            return idx;
        }
    }

    public static Location deliveryLocation = null;

    public static class IntArrayViewBinder extends MyViewBinder {
        private static final int[] VIEWS = {R.id.count_20, R.id.count_20m, R.id.count_5, R.id.count_15, R.id.count_06};

        IntArrayViewBinder(String column) {
            super(column);
        }

        @Override
        public boolean mySetViewValue(View view, Cursor cursor, int columnIndex) {
            int idx = getIdx();
            int[] vals = BlobUtils.blobToIntArr(cursor.getBlob(idx));
            for (int i = 0; i < VIEWS.length; ++i) {
                TextView v = (TextView) view.findViewById(VIEWS[i]);
                if (vals[i] == 0)
                    v.setText("");
                else
                    v.setText(String.valueOf(vals[i]));
            }
            return true;
        }

    }

    public static class IconViewBinder extends MyViewBinder {
        IconViewBinder(String column) {
            super(column);
        }

        @Override
        public boolean mySetViewValue(View view, Cursor cursor, int columnIndex) {
            int idx = getIdx();
            int val = cursor.getInt(idx);
            if (val == OrdersContentProvider.STATE_SMS_ERROR) {
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_delete, 0);
            }
            return true;
        }

    }

    public static class ColorViewBinder extends MyViewBinder {
        private static final String[] STATES = {"", "Измененная", "Отказ", "Клиент отсутствует", "Клиент дома, на месте", "Ускорить заявку", "Повтор (вчерашняя)"};

        ColorViewBinder(String column) {
            super(column);
        }

        private static final int[] colorMap = {
                Color.argb(0x00, 0xff, 0xff, 0xff),
                Color.argb(0x00, 0xff, 0xff, 0xff),
                Color.argb(0x00, 0xff, 0xff, 0xff),
                Color.argb(0x00, 0xff, 0xff, 0xff),
                Color.argb(0xff, 0xd6, 0xad, 0xeb),
                Color.argb(0xff, 0xff, 0xd9, 0x80),
                Color.argb(0xff, 0xff, 0x94, 0x94)
        };

        @Override
        public boolean mySetViewValue(View view, Cursor cursor, int columnIndex) {
            int idx = getIdx();
            int val = cursor.getInt(idx);
            view.setBackgroundColor(colorMap[val]);
            ((TextView) view).setText(STATES[val]);
            return true;
        }

    }

    public static class TimeViewBinder extends MyViewBinder {
        private static SimpleDateFormat shortDateFormat = new SimpleDateFormat("HH:mm");
        private static SimpleDateFormat longDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");

        public TimeViewBinder(String column) {
            super(column);
        }

        @Override
        public boolean mySetViewValue(View view, Cursor cursor, int columnIndex) {
            Log.e("Idx",getIdx()+"");
            long val = cursor.getLong(getIdx());
//            for(String string:cursor.getColumnNames()){
//                Log.e("Columns", string);
//            }
//            Log.e("Type string",cursor.getString(getIdx()));
            Date mOrderDate = new Date(val);

            Calendar orderDate = Calendar.getInstance();
            orderDate.setTime(mOrderDate);
            SimpleDateFormat dstFormat = (DateUtils.isToday(orderDate.getTimeInMillis())) ? shortDateFormat : longDateFormat;
            int color = 0xFFFF4444;
            //int textColot = Color.parseColor("#FF4444");

            Calendar ref = Calendar.getInstance();
            ref.setTime(new Date());
//            Log.e("Dates: ",val+"");
            do {
                if (ref.before(orderDate)) {
                    color = 0xFF33B5E5;
                    Log.e("If condition: ","1");
                    break;
                }
                ref.add(Calendar.HOUR, -1);
                if (ref.before(orderDate)) {
                    color = Color.WHITE;
                    Log.e("If condition: ","2");
                    break;
                }
                ref.add(Calendar.HOUR, -1);
                if (ref.before(orderDate)) {
                    color = 0xFF99CC00;
                    Log.e("If condition: ","3");
                    break;
                }
                ref.add(Calendar.HOUR, -1);
                if (ref.before(orderDate)) {
                    color = 0xFFFFBB33;
                    Log.e("If condition: ","4");
                    break;
                }
            } while (false);

            ((TextView) view).setText(dstFormat.format(mOrderDate));





            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((View) view.getParent().getParent()).setBackgroundTintList(ColorStateList.valueOf(color));
                ((View) view.getParent().getParent()).setBackground(view.getContext().getResources().getDrawable(R.drawable.row_bg));
            } else{
                ((View)view.getParent().getParent()).setBackgroundColor(color);

            }


            return true;
        }

    }

    // private Cursor cursor;
    private SimpleCursorAdapter adapter;
    public static final BindersList viewBinders = new BindersList();

    static {
        viewBinders.addBinder(new IntArrayViewBinder(OrdersTable.COLUMN_ORDERED_COUNTS));
        viewBinders.addBinder(new IconViewBinder(OrdersTable.COLUMN_SMS_STATE));
        viewBinders.addBinder(new ColorViewBinder(OrdersTable.COLUMN_ORDER_STATE));
        viewBinders.addBinder(new TimeViewBinder(OrdersTable.COLUMN_ORDER_TIME));
    }

    private class UiUpdater implements Runnable {

        private boolean mCanceled = false;

        public void run() {
            //Log.i(this.getClass().getName(), "run()");
            if (mCanceled)
                return;
            //Log.i(this.getClass().getName(), "update");
            OrderListActivity.this.adapter.notifyDataSetChanged();
            Handler h = new Handler();
            h.postDelayed(this, 60 * 1000);
        }

        public void cancel() {
            mCanceled = true;
        }
    }

    private UiUpdater mUiUpdater;


    private IndeterminateProgressIndicator indicator = new EmptyIndeterminateProgressIndicator();

    public IndeterminateProgressIndicator getProgressIndicator() {
        return indicator;
    }

    private void showErrorDialog(String msg) {
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button
        ad.setMessage(msg);
        ad.setButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();

    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
//		Intent lsint = new Intent(getApplicationContext(), LocationService.class);
//		startService(lsint);
        indicator = SimpleIndeterminateProgressIndicator.createIndicator(this);
        setContentView(R.layout.activity_order_list);
        FrameLayout head = (FrameLayout) findViewById(R.id.my_header);
		reOrder=findViewById(R.id.reOrder);
        Log.i("OrderListActivity", "inflate");
        View.inflate(this, R.layout.rowlayout, head);
        head.setBackgroundColor(Color.LTGRAY);
        waterDB=new WaterDeliveryDatabaseHelper(this);
        fillData();

        reOrder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				startActivity(new Intent(getApplicationContext(),OrderListActivity.class));
			}
		});
    }

    private void fillData() {

        String[] from = new String[]{
                OrdersTable.COLUMN_ADDR,
                OrdersTable.COLUMN_ORDERED_COUNTS,
                OrdersTable.COLUMN_PHONE,
                OrdersTable.COLUMN_ORDER_TIME,
                OrdersTable.COLUMN_DELIVER_TIME,
                OrdersTable.COLUMN_SMS_STATE,
                OrdersTable.COLUMN_ORDER_STATE
        };
        // Fields on the UI to which we map
        int[] to = new int[]{R.id.addr, R.id.counts, R.id.phone, R.id.order_time, R.id.deliver_time, R.id.addr, R.id.order_state};

        adapter = new SimpleCursorAdapter(this, R.layout.rowlayout, null, from,
                to, 0);
        adapter.setViewBinder(viewBinders);
        setListAdapter(adapter);


        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(OrdersContentProvider.ORDERS_URI, id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, OrderDetails.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent iData) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_order_list, menu);
        return true;
    }

    private void requestForSpecificPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean checkIfAlreadyhavePermission(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scan_inbox:
                if (checkIfAlreadyhavePermission(Manifest.permission.SEND_SMS) && checkIfAlreadyhavePermission(Manifest.permission.RECEIVE_SMS)) {
                    scan();
                } else {
                    requestForSpecificPermission(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_REQUEST_CODE);
                }
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(Intent.ACTION_EDIT, SettingsContentProvider.SETTINGS_URI, this, SettingsActivity.class));
                return true;
            case R.id.menu_stop:
//			stopService(new Intent(getApplicationContext(), LocationService.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scan() {
        String phone = SettingsHelper.getIncomPhone(this);
        if (phone == null) {
            showErrorDialog(getString(R.string.no_sms_num));
        } else {
            new SMSProcessor().execute(phone);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case SMS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    scan();
                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(), "Sms ugradylmak ucin bu programma rugsat berilmedik", Toast.LENGTH_SHORT).show();
                    requestForSpecificPermission(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_REQUEST_CODE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                OrdersTable.COLUMN_ID,
                OrdersTable.COLUMN_ADDR,
                OrdersTable.COLUMN_ORDERED_COUNTS,
                OrdersTable.COLUMN_PHONE,
                OrdersTable.COLUMN_ORDER_STATE,
                OrdersTable.COLUMN_ORDER_TIME,
                OrdersTable.COLUMN_DELIVER_TIME,
                OrdersTable.COLUMN_SMS_STATE,
                OrdersTable.COLUMN_LATITUDE,
                OrdersTable.COLUMN_LONGITUDE
        };
        CursorLoader cursorLoader = new CursorLoader(this,
                OrdersContentProvider.ORDERS_URI, projection, OrdersTable.COLUMN_SMS_STATE + "<" + OrdersContentProvider.STATE_SMS_DELIVERED,
                null, OrdersTable.COLUMN_ORDER_STATE + " DESC, " + OrdersTable.COLUMN_ORDER_TIME + " ASC");
        getProgressIndicator().setVisibility(true);
        return cursorLoader;
    }



    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<OrderModel> orderModels = new ArrayList<>();
        String[] columns = {
                OrdersTable.COLUMN_ID,
                OrdersTable.COLUMN_ADDR,
                OrdersTable.COLUMN_ORDERED_COUNTS,
                OrdersTable.COLUMN_PHONE,
                OrdersTable.COLUMN_ORDER_STATE,
                OrdersTable.COLUMN_ORDER_TIME,
                OrdersTable.COLUMN_DELIVER_TIME,
                OrdersTable.COLUMN_SMS_STATE,
                OrdersTable.COLUMN_LATITUDE,
                OrdersTable.COLUMN_LONGITUDE
        };

        MatrixCursor matrixCursor = new MatrixCursor(columns);
        startManagingCursor(matrixCursor);

        boolean isRestart=false;


        if(data.getCount()>0){
            while (data.moveToNext()){
                long val=data.getLong(5);
                Date mOrderDate = new Date(val);
                SimpleDateFormat longDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
                Calendar orderDate = Calendar.getInstance();
                orderDate.setTime(mOrderDate);

                Calendar ref = Calendar.getInstance();
                ref.setTime(new Date());


                long diff = ref.getTime().getTime() - orderDate.getTime().getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                Log.e("DIFF: ",ref.getTime().toString()+"-"+orderDate.getTime().toString()+" / HOURS:"+hours+"");

                if(hours>=48){
                    isRestart=true;
                    waterDB.updateStatus(data.getInt(0)+"",OrdersContentProvider.STATE_SMS_EXPIRE_TIME);
                }


            }

            if(isRestart){
                finish();
                startActivity(new Intent(getApplicationContext(),OrderListActivity.class));
            }
        }

        data.moveToPosition(-1);


//


        if (deliveryLocation != null) {
            while (data.moveToNext()) {
                Double orderLat = 0.0;
                Double orderLong = 0.0;
                Double destination = 0.0;
                Log.e("IDS",data.getInt(0)+"");

                try {
                    orderLat = Double.parseDouble(data.getString(8));
                    orderLong = Double.parseDouble(data.getString(9));

                    Location startPoint = new Location("");
                    startPoint.setLatitude(orderLat);
                    startPoint.setLongitude(orderLong);

                    Location endPoint = new Location("");
                    endPoint.setLatitude(deliveryLocation.getLatitude());
                    endPoint.setLongitude(deliveryLocation.getLongitude());
                    double distance = startPoint.distanceTo(endPoint);
                    destination = distance;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    orderLat = 0.0;
                    orderLong = 0.0;
                    destination = 0.0;
                }

                orderModels.add(new OrderModel(
                        data.getInt(0),
                        data.getString(1),
                        data.getBlob(2),
                        data.getString(3),
                        data.getInt(4),
                        data.getLong(5),
                        data.getString(6),
                        data.getInt(7),
                        destination,
                        data.getString(8),
                        data.getString(9)));
            }
            data.moveToFirst();

			Collections.sort(orderModels, new CustomComparator());

			for(OrderModel model:orderModels){
				matrixCursor.addRow(new Object[] {
						model.getCOLUMN_ID(),
						model.getCOLUMN_ADDR(),
						model.getCOLUMN_ORDERED_COUNTS(),
						model.getCOLUMN_PHONE(),
						model.getCOLUMN_ORDER_STATE(),
						model.getCOLUMN_ORDER_TIME(),
						model.getCOLUMN_DELIVER_TIME(),
						model.getCOLUMN_SMS_STATE(),
						model.getCOLUMN_LATITUDE(),
						model.getCOLUMN_LONGITUDE()
				});
			}

			getProgressIndicator().setVisibility(false);
			Log.e("Matrix",matrixCursor.getCount()+"");
			adapter.swapCursor(matrixCursor);
			if (mUiUpdater != null)
				mUiUpdater.cancel();
			mUiUpdater = new UiUpdater();
			mUiUpdater.run();

        } else {
            Toast.makeText(getApplicationContext(), "Что-то пошло не так при определении вашего местоположения", Toast.LENGTH_LONG).show();
			getProgressIndicator().setVisibility(false);
			adapter.swapCursor(data);
			if (mUiUpdater != null)
				mUiUpdater.cancel();
			mUiUpdater = new UiUpdater();
			mUiUpdater.run();
        }


    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        getProgressIndicator().setVisibility(false);
        mUiUpdater.cancel();
        adapter.swapCursor(null);
    }

	public class CustomComparator implements Comparator<OrderModel> {
		@Override
		public int compare(OrderModel o1, OrderModel o2) {
			return o1.getDESTINATION().compareTo(o2.getDESTINATION());
		}
	}

    private static final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
    private static final Uri SMS_DELETE = Uri.parse("content://sms/");

    private class SMSProcessor extends AsyncTask<String, String, Boolean> {

        private class Pair {
            private int mId;
            private ContentValues mVal;

            Pair(int id, ContentValues v) {
                mId = id;
                mVal = v;
            }

            public int getId() {
                return mId;
            }

            public ContentValues getVal() {
                return mVal;
            }
        }

        @Override
        protected void onPreExecute() {
            getProgressIndicator().setVisibility(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            getProgressIndicator().setVisibility(false);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ContentResolver cr = getContentResolver();
            Cursor data = cr.query(SMS_INBOX, null, "address=?", new String[]{params[0]}, null);
            if (data.getCount() == 0) {
                data.close();
                return true;
            }
            data.moveToFirst();
            Log.i(getClass().getName(), DatabaseUtils.dumpCurrentRowToString(data));
            ArrayList<Pair> orders = new ArrayList<Pair>();
            do {
                if (isCancelled()) {
                    data.close();
                    return false;
                }
                int idx = data.getColumnIndex("body");
                ContentValues parsed = SMSParser.parse(data.getString(idx), getString(R.string.delimiter));
                idx = data.getColumnIndex("date_sent");
                parsed.put(OrdersTable.COLUMN_ORDER_TIME, data.getLong(idx));
                orders.add(new Pair(data.getInt(0), parsed));
            } while (data.moveToNext());

            for (Pair p : orders) {
                cr.insert(OrdersContentProvider.ORDERS_URI, p.getVal());
                if (cr.delete(SMS_DELETE, "_id=" + p.getId(), null) != 1) {
                    data.close();
                    return false;
                }
            }
            publishProgress(String.format("Импортировано %d сообщений", orders.size()));
            data.close();

            return true;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            Toast.makeText(getApplicationContext(), progress[0], Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!statusOfGPS){
            turnOnGsp();
        }
    }

    private void turnOnGsp() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle("Внимание!");
        alert.setMessage("Пожалуйста, включите GPS!");
        alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("Включите GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alert.show();
    }
}
