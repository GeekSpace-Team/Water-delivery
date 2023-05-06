package com.android.waterdelivery;

import android.app.Activity;
import android.view.Window;

public class SimpleIndeterminateProgressIndicator implements
		IndeterminateProgressIndicator {

	Activity mParent;
	
	public SimpleIndeterminateProgressIndicator(Activity parent) {
		mParent = parent;
	}

	public void setVisibility(boolean v) {
		mParent.setProgressBarIndeterminateVisibility(v);
	}

	public static IndeterminateProgressIndicator createIndicator(Activity parent) {
		if (parent.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS))
			return new SimpleIndeterminateProgressIndicator(parent);
		else
			return new EmptyIndeterminateProgressIndicator();
	}
}
