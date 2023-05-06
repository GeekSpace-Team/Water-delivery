package com.android.waterdelivery;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {
	private ProgressDialog mDialog;
	private int showCnt;
	public MyProgressDialog(Context context){
		mDialog = new ProgressDialog(context);
		mDialog.setCancelable(false);
		showCnt = 0;
	}
	
	public void setMessage(String msg){
		mDialog.setMessage(msg);
	}
	
	public void show(){
		++showCnt;
		mDialog.show();
	}
	
	public void dismiss(){
		--showCnt;
		if (showCnt <= 0){
			mDialog.dismiss();
			showCnt = 0;
		}
	}

	public boolean isShowing() {
		return showCnt != 0;
	}
}
