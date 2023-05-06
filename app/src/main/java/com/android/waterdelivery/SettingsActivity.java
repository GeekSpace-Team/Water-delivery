package com.android.waterdelivery;

import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.NavUtils;

public class SettingsActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	static final int[] viewMapKeys = {SettingsTable.INCOMIND_PHONE_NUM_ID, SettingsTable.OUTGOING_PHONE_NUM_ID};
	static final int[] viewMapVals =    {R.id.incomingNum, R.id.outgoingNum};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getLoaderManager().initLoader(2, null, this);
        View v = findViewById(R.id.btnOk);
        v.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				for (int viewId : viewMapVals)
				{
					int setId = viewMapKeys[indexOf(viewMapVals,viewId)];
					EditText et = (EditText)findViewById(viewId);
					String text = et.getText().toString();
					ContentValues cv = new ContentValues();
					cv.put(SettingsTable.COLUMN_SET_ID, setId);
					cv.put(SettingsTable.COLUMN_SET_VALUE, text);
					getContentResolver().insert(SettingsContentProvider.SETTINGS_URI, cv);
				}
				finish();
			}
		});
    }

    int indexOf(int[] arr, int val) {
    	for (int i = 0; i < arr.length; ++i) {
    		if (arr[i] == val)
    			return i;
    	}
    	return -1;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
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
		CursorLoader cursorLoader = new CursorLoader(this,
				SettingsContentProvider.SETTINGS_URI, null, null, null, null);
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.getCount() != 0) {
			data.moveToFirst();
			int setIdIdx = data.getColumnIndex(SettingsTable.COLUMN_SET_ID);
			int valueIdx = data.getColumnIndex(SettingsTable.COLUMN_SET_VALUE);
			while (!data.isAfterLast())
		    {
		    	int i = indexOf(viewMapKeys,data.getInt(setIdIdx));
		    	if (i != -1)
		    	{
		    		TextView v = (TextView) findViewById(viewMapVals[i]);
	    			v.setText(data.getString(valueIdx));
		    	}
		    	data.moveToNext();
		    }
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	}

}
