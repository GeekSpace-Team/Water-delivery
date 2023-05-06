package com.android.waterdelivery;

import java.text.SimpleDateFormat;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;

class DateTimeViewBinder implements SimpleCursorAdapter.ViewBinder{

	public final static SimpleDateFormat longDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
	public final static SimpleDateFormat shortDateFormat = new SimpleDateFormat("HH:mm");

	/*//@Override
	public boolean setViewValue(View view, Object data,
			String textRepresentation) {
		if (view.getId() == R.id.time)
		{
			Calendar orderDate = Calendar.getInstance();
			orderDate.setTime((Date)data);
			SimpleDateFormat dstFormat = (DateUtils.isToday(orderDate.getTimeInMillis())) ? shortDateFormat : longDateFormat;
			int color = Color.parseColor("#FF4444");
			Calendar ref = Calendar.getInstance();
			ref.setTime(new Date());
			do{
				if (ref.before(orderDate)){
					color = Color.parseColor("#33B5E5");
					break;
				}
				ref.add(Calendar.HOUR, -1);
				if (ref.before(orderDate)){
					color = Color.WHITE;
					break;
				}
				ref.add(Calendar.HOUR, -1);
				if (ref.before(orderDate))
				{
					color = Color.parseColor("#99CC00");
					break;
				}
				ref.add(Calendar.HOUR, -1);
				if (ref.before(orderDate))
				{
					color = Color.parseColor("#FFBB33");
					break;
				}
				/*ref.add(Calendar.HOUR, -1);
				if (ref.before(orderDate))
				{
					color = Color.parseColor("#FF4444");
					break;
				}*/
/*			}while(false);
				
			((TextView)view).setText(dstFormat.format(orderDate.getTime()));
			//((TextView)view).setTextColor(color);
			ViewParent p = view.getParent();
			((View)p).setBackgroundColor(color);
			
			return true;
		}
		return false;
	}*/

	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}
	
}