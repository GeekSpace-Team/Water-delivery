package com.android.waterdelivery;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;


public class OrderDetails extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static class LongTimeViewBinder extends OrderListActivity.MyViewBinder {
        private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        public LongTimeViewBinder(String column) {
            super(column);
        }

        @Override
        public boolean mySetViewValue(View view, Cursor cursor, int columnIndex) {
            long val = cursor.getLong(getIdx());
            ((TextView) view).setText(dateFormat.format(new Date(val/* - TimeZone.getDefault().getOffset(val)*/)));
            return true;
        }

    }

    public final static int REQUST_COUNT = 100500;
    public final static String ORDERED_COUNTS_RESULT = "ORDERED_COUNTS_RESULT";
    public final static String DELYVERY_STATE = "DELYVERY_STATE";

    public static Location cameLocation = null;
    public String latitudeOrder = "0";
    public String longitudeOrder = "0";

    public static final int LOCATION_REQUEST_CODE = 6754;
    public static final int SMS_REQUEST_CODE = 4324;
    private Button ok, btnOk2,phoneCall,showOnMap;
    private String phoneNumber="";


    static final String[] viewMapKeys = {OrdersTable.COLUMN_PHONE, OrdersTable.COLUMN_ORDERED_COUNTS, OrdersTable.COLUMN_ADDR,
            OrdersTable.COLUMN_DELIVER_TIME, OrdersTable.COLUMN_ORDER_TIME, OrdersTable.COLUMN_LATITUDE, OrdersTable.COLUMN_LONGITUDE};
    static final int[] viewMapVals = {R.id.clientPhone, R.id.orderDetailsTable, R.id.clientAddr,
            R.id.deliverTime, R.id.orderTime, R.id.latitudeValue, R.id.longitudeValue};

    private Uri mUri;
    private final static OrderListActivity.BindersList mViewBinder = new OrderListActivity.BindersList();

    static {
        mViewBinder.addBinder(new OrderListActivity.IntArrayViewBinder(OrdersTable.COLUMN_ORDERED_COUNTS));
        mViewBinder.addBinder(new LongTimeViewBinder(OrdersTable.COLUMN_ORDER_TIME));
    }

    private TextView distanceValue;
    Cursor mData;

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    private IndeterminateProgressIndicator indicator = new EmptyIndeterminateProgressIndicator();

    public IndeterminateProgressIndicator getProgressIndicator() {
        return indicator;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        indicator = SimpleIndeterminateProgressIndicator.createIndicator(this);
        setContentView(R.layout.activity_order_details);
        ok = (Button) findViewById(R.id.btnOk);
        btnOk2 = (Button) findViewById(R.id.btnOk2);
        phoneCall = (Button) findViewById(R.id.phoneCall);
        showOnMap = (Button) findViewById(R.id.showOnMap);
        distanceValue = findViewById(R.id.distanceValue);
        if (checkIfAlreadyhavePermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkIfAlreadyhavePermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkIfAlreadyhavePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {

        } else {
            requestForSpecificPermission(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_REQUEST_CODE);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mUri = getIntent().getData();
        try {
            Log.e("Order uri", mUri.toString());
            String[] step = mUri.toString().split("/");
            Log.e("Order id", step[step.length - 1]);
            getLocations(step[step.length - 1]);
        } catch (Exception ex) {
            ex.printStackTrace();
            ok.setEnabled(false);
            ok.setVisibility(View.GONE);
            btnOk2.setVisibility(View.VISIBLE);
        }
        getLoaderManager().initLoader(1, null, this);


        ok.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (checkIfAlreadyhavePermission(Manifest.permission.SEND_SMS) && checkIfAlreadyhavePermission(Manifest.permission.RECEIVE_SMS)) {
                    startSendResult();
                } else {
                    requestForSpecificPermission(new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_REQUEST_CODE);
                }

//				ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(OrderDetails.this, R.layout.my_simple_list_item, R.id.lst_item_text, choiceList);
//				builder.setAdapter(itemsAdapter, new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialog, int mwhich) {
//						int which = choiceListMap[mwhich];
//						if (which != 1 && which != 4)
//							executeOrder(which, null);
////						else {
////							Intent intent = new Intent(Intent.ACTION_EDIT, mUri, OrderDetails.this, OrderExecutionActivity.class);
////							intent.putExtra(DELYVERY_STATE, which);
////							startActivityForResult(intent, REQUST_COUNT);
////						}
//					}
//				});
//				builder.create().show();
            }
        });

        btnOk2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Вы не можете выполнить этот заказ!", Toast.LENGTH_SHORT).show();
            }
        });

        phoneCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_VIEW);
                callIntent.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(callIntent);
            }
        });

        showOnMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(latitudeOrder.isEmpty() || longitudeOrder.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Адресная информация неверна!", Toast.LENGTH_SHORT).show();
                } else {
                    String url="http://maps.google.com/maps?z=12&t=m&q=loc:"+latitudeOrder+","+longitudeOrder+"";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });


    }

    private void getLocations(String id) {
        WaterDeliveryDatabaseHelper db = new WaterDeliveryDatabaseHelper(getApplicationContext());
        Cursor cursor = db.getSelect(id);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                latitudeOrder = cursor.getString(0);
                longitudeOrder = cursor.getString(1);
                phoneNumber = cursor.getString(2);
            }

            calculateDistance();
        }
    }

    private void calculateDistance() {
        try {
            Location startPoint = new Location("");
            startPoint.setLatitude(Double.parseDouble(latitudeOrder));
            startPoint.setLongitude(Double.parseDouble(longitudeOrder));

            Location endPoint = new Location("");
            if (cameLocation != null) {
                endPoint.setLatitude(cameLocation.getLatitude());
                endPoint.setLongitude(cameLocation.getLongitude());
            }


//			double distance = meterDistanceBetweenPoints(Float.parseFloat(latitudeOrder),Float.parseFloat(longitudeOrder),Float.parseFloat(endPoint.getLatitude()+""),Float.parseFloat(endPoint.getLongitude()+""));//startPoint.distanceTo(endPoint);
//			double distance = SphericalUtil.computeDistanceBetween(new LatLng(startPoint.getLatitude(),startPoint.getLongitude()), new LatLng(endPoint.getLatitude(),endPoint.getLongitude()));

            double distance = startPoint.distanceTo(endPoint);
			String dst=Utils.fixNumber(distance);
            if (distance > 50) {
                ok.setVisibility(View.GONE);
                btnOk2.setVisibility(View.VISIBLE);
                btnOk2.setText("Не могу щелкнуть, Расстояние="+dst+" meters  >  50 meters");
            } else {
                ok.setEnabled(true);
                ok.setVisibility(View.VISIBLE);
                btnOk2.setVisibility(View.GONE);
            }

            Log.e("Distance", startPoint.getLatitude() + " / " + startPoint.getLongitude() + " == " + endPoint.getLatitude() + " / " + endPoint.getLongitude());

            distanceValue.setText(dst + " meters");
        } catch (Exception ex) {
            ex.printStackTrace();
            ok.setEnabled(true);
            ok.setVisibility(View.VISIBLE);
            btnOk2.setVisibility(View.GONE);
            distanceValue.setText("не могу рассчитать");
        }
    }

    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f / Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    private void startSendResult() {
        if (!ok.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Вы не можете выполнить этот заказ!", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder =
                new AlertDialog.Builder(OrderDetails.this);
        builder.setTitle("Статус выполнения");

        final String[] choiceList =
                {
                        "Доставлено всё",
//						"Доставлена часть (нехватка)",
//						"Доставлена часть (отказ)",
//						"Отказ",
//						"Клиент недоступен"
                };
        final int[] choiceListMap = {0, 4, 1, 2, 3};
        executeOrder(0, null);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //not granted
                    finish();
                }
                break;
            case SMS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    startSendResult();
                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(), "Sms ugradylmak ucin bu programma rugsat berilmedik", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//		Intent intent = new Intent(this, LocationService.class);
//		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
//		unbindService(mConnection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUST_COUNT) {
            if (resultCode == RESULT_OK) {
                int[] counts = data.getIntArrayExtra(ORDERED_COUNTS_RESULT);
                int state = data.getIntExtra(DELYVERY_STATE, OrdersContentProvider.STATE_DCHANGED);
                realExecuteOrder(state, counts);
            }
        }
    }

    private void realExecuteOrder(int state, int[] counts) {
        byte[] deliveredCounts = null;// = getCounts(DELIVERED_EDITS);
        if (counts == null && state != 1) {
            if (mData != null) {
                deliveredCounts = mData.getBlob(mData.getColumnIndex(OrdersTable.COLUMN_ORDERED_COUNTS));
            } else
                throw new IllegalArgumentException();
        } else
            deliveredCounts = BlobUtils.intArrToBlob(counts);

        Location loc = cameLocation;

        ContentValues cv = new ContentValues();
        cv.put(OrdersTable.COLUMN_DELIVERED_COUNTS, deliveredCounts);
        cv.put(OrdersTable.COLUMN_DELIVERY_STATE, state);
        if (loc != null) {
            cv.put(OrdersTable.COLUMN_LATITUDE, loc.getLatitude());
            cv.put(OrdersTable.COLUMN_LONGITUDE, loc.getLongitude());
        } else {
            cv.put(OrdersTable.COLUMN_LATITUDE, 85.);
            cv.put(OrdersTable.COLUMN_LONGITUDE, 0);
        }

        getContentResolver().update(mUri, cv, null, null);
        NavUtils.navigateUpFromSameTask(this);
    }

    private void executeOrder(final int state, final int[] counts) {
        if (state == 0 || state == 2 || state == 3) {
            AlertDialog.Builder bldr = new AlertDialog.Builder(this);
            final String[] states =
                    {
                            "Доставлено всё",
//					"",
//					"Отказ",
//					"Клиент недоступен"
                    };
            bldr.setTitle(states[state]);
            bldr.setPositiveButton("Выполнить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    realExecuteOrder(state, counts);
                }
            });
            bldr.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            bldr.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_order_details, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getProgressIndicator().setVisibility(true);
        CursorLoader cursorLoader = new CursorLoader(this,
                mUri, viewMapKeys, null, null, null);
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mData = data;
        Button ok = (Button) findViewById(R.id.btnOk);
        if (data.getCount() != 0) {
//            ok.setEnabled(true);
//            ok.setVisibility(View.VISIBLE);
//            btnOk2.setVisibility(View.GONE);
            data.moveToFirst();
            for (int idx = 0; idx < data.getColumnCount(); ++idx) {
                int i = Arrays.asList(viewMapKeys).indexOf(data.getColumnName(idx));
                if (i != -1) {


                    View v = findViewById(viewMapVals[i]);
                    if (!getViewBinder().setViewValue(v, data, idx)) {
                        ((TextView) v).setText(data.getString(idx));
                    }
                }
            }
        } else {
//            ok.setVisibility(View.GONE);
//            btnOk2.setVisibility(View.VISIBLE);
        }
        getProgressIndicator().setVisibility(false);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        getProgressIndicator().setVisibility(false);
        Button ok = (Button) findViewById(R.id.btnOk);
//        ok.setVisibility(View.GONE);
//        btnOk2.setVisibility(View.VISIBLE);
    }

//	private LocationService.LocationServiceBinder mBinder = null;
//
//	/** Defines callbacks for service binding, passed to bindService() */
//    private ServiceConnection mConnection = new ServiceConnection() {
//
//        public void onServiceConnected(ComponentName className,
//                IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            mBinder = (LocationService.LocationServiceBinder) service;
//        }
//
//        public void onServiceDisconnected(ComponentName arg0) {
//            mBinder = null;
//        }
//    };
}
