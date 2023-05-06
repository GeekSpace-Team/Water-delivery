package com.android.waterdelivery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class MyLocationService extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATE = "com.android.waterdelivery.UPDATE_LOCATION";

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATE.equals(action)) {

                LocationResult result = LocationResult.extractResult(intent);

                if (result != null) {
                    Location location = result.getLastLocation();
                    String value = location.getLatitude() + " / " + location.getLongitude();
                    // Toast.makeText(context,value,Toast.LENGTH_SHORT).show();
                    try {
                        OrderDetails.cameLocation=location;
                        OrderListActivity.deliveryLocation=location;
                        Log.e("LOCATION CHANGE",location.getLatitude()+" / "+location.getLongitude());
                    } catch (Exception ex) {
                        Log.e("LOCATION CHANGE ERROR",ex.getMessage());
                        // Toast.makeText(context,ex.getMessage(),Toast.LENGTH_SHORT).show();


                    }
                } else {


                    LocationManager lm = (LocationManager)
                            context.getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = false;
                    boolean network_enabled = false;
                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!gps_enabled && !network_enabled) {


//                        Intent intent1=new Intent("android.location.GPS_ENABLED_CHANGE");
//                        intent.putExtra("enabled", true);
//                        context.sendBroadcast(intent);

//                        new AlertDialog.Builder(context )
//                                .setMessage( "GPS Enable" )
//                                .setPositiveButton( "Settings" , new
//                                        DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick (DialogInterface paramDialogInterface , int paramInt) {
//                                                context.startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS )) ;
//                                            }
//                                        })
//                                .setNegativeButton( "Cancel" , null )
//                                .show() ;
                    }

                }
            }
        }

    }


}
