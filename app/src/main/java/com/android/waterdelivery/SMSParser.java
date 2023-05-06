package com.android.waterdelivery;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.content.ContentValues;

public class SMSParser {
	
	public static final ContentValues parse(String body, String delimiter)
	{
		String[] tokens = body.split(delimiter);
		ContentValues res = new ContentValues();
		res.put(OrdersTable.COLUMN_ORDER_STATE, tokens[0]);
		res.put(OrdersTable.COLUMN_CLIENT_CODE, tokens[1]);
		if (tokens.length < 3)
			return res;
		res.put(OrdersTable.COLUMN_PHONE, tokens[2]);
		
		if (tokens.length < 8)
			return res;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    DataOutputStream dout = new DataOutputStream(bout);
	    for (int i = 3; i < 8; ++i) {
	    	try {
		    	try {
					dout.writeInt(Integer.parseInt(tokens[i]));
				} catch (NumberFormatException e) {
					dout.writeInt(0);
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    try {
			dout.close();
		    res.put(OrdersTable.COLUMN_ORDERED_COUNTS, bout.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		    res.put(OrdersTable.COLUMN_ORDERED_COUNTS, new byte[0]);
		}
	    
		if (tokens.length < 9)
			return res;
		res.put(OrdersTable.COLUMN_ADDR, tokens[8]);
		//res.put(OrdersTable.COLUMN_ORDER_TIME, tokens[9]);
		if (tokens.length < 10)
			return res;
		res.put(OrdersTable.COLUMN_DELIVER_TIME, tokens[9]);

		if (tokens.length < 11)
			return res;
		res.put(OrdersTable.COLUMN_LATITUDE, tokens[10]);

		if (tokens.length < 12)
			return res;
		res.put(OrdersTable.COLUMN_LONGITUDE, tokens[11]);

		return res;
	}
}
