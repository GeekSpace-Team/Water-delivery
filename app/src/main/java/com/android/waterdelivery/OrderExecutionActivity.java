package com.android.waterdelivery;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NavUtils;

public class OrderExecutionActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private Uri mUri;
	private int mState;
	
	private static final int[] DELIVERED_EDITS = {R.id.editText1,R.id.editText2,R.id.editText3,R.id.editText4,R.id.editText5};
	private static final int[] ORDERED_VIEWS = {R.id.cntView1,R.id.cntView2,R.id.cntView3,R.id.cntView4,R.id.cntView5};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_execution);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent intent = getIntent();
		mState = intent.getIntExtra(OrderDetails.DELYVERY_STATE, -1);
		mUri = intent.getData();
		getLoaderManager().initLoader(1, null, this);

		Button ok = (Button) findViewById(R.id.buttonExecute);
		ok.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int[] ordCounts = getCounts(ORDERED_VIEWS);
				int[] delCounts = getCounts(DELIVERED_EDITS);
				boolean f = false;
				for (int i = 0; i < ordCounts.length; ++i) {
					if (delCounts[i]<0 || delCounts[i]>ordCounts[i]) {
						f = true;
						break;
					}
				} 
				if (f) {
					AlertDialog.Builder builder = new AlertDialog.Builder(OrderExecutionActivity.this);
					builder
						.setMessage("Неверно заполненые поля!")
						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
					builder.create().show();
					
				} else {
					Intent data = new Intent();
					data.putExtra(OrderDetails.ORDERED_COUNTS_RESULT, getCounts(DELIVERED_EDITS));
					data.putExtra(OrderDetails.DELYVERY_STATE, mState);
					setResult(RESULT_OK, data);
					finish();
				}
			}
		});
	}
	
	private void setCounts(int[] counts, int[] ids) {
		for (int i = 0; i < ids.length; ++i) {
			TextView et = (TextView) findViewById(ids[i]);
			et.setText(String.valueOf(counts[i]));
		}
	}

	private int[] getCounts(int[] ids) {
		int[] res = new int[ids.length];
		for (int i = 0; i < ids.length; ++i) {
			TextView tv = (TextView) findViewById(ids[i]);
			res[i] = Integer.parseInt(tv.getText().toString());
		}
		return res;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.activity_order_execution, menu);
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
		CursorLoader cursorLoader = new CursorLoader(this,
				mUri, null, null, null, null);
		return cursorLoader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.getCount() != 0) {
			data.moveToFirst();
			byte[] oCounts = data.getBlob(data.getColumnIndex(OrdersTable.COLUMN_ORDERED_COUNTS));
			setCounts(BlobUtils.blobToIntArr(oCounts),ORDERED_VIEWS);
			byte[] dCounts = data.getBlob(data.getColumnIndex(OrdersTable.COLUMN_DELIVERED_COUNTS));
			if (dCounts == null)
				dCounts = oCounts;
			setCounts(BlobUtils.blobToIntArr(dCounts),DELIVERED_EDITS);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		//NavUtils.navigateUpFromSameTask(this);
	}
}

