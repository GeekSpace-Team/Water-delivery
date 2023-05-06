package com.android.waterdelivery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BlobUtils {
	public static final int[] blobToIntArr(byte[] bytes) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		int[] vals = new int[bytes.length/4];
		try {
			for (int i = 0; i < vals.length; ++i)
			{
				vals[i] = dis.readInt();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vals;
	}
	
	public static byte[] intArrToBlob(int[] arr) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
	    DataOutputStream dout = new DataOutputStream(bout);
	    for (int i = 0; i < arr.length; ++i) {
	    	try {
		    	try {
					dout.writeInt(arr[i]);
				} catch (NumberFormatException e) {
					dout.writeInt(0);
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return bout.toByteArray();
	}
	
	public static final int[] sum(int[] a, int[] b) {
		int[] res = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			res[i] = a[i] + b[i];
		}
		return res;
	}

	public static final int sum(int[] a) {
		int res = 0;
		for (int i = 0; i < a.length; ++i) {
			res += a[i];
		}
		return res;
	}
	public static final int[] sub(int[] a, int[] b) {
		int[] res = new int[a.length];
		for (int i = 0; i < a.length; ++i) {
			res[i] = a[i] - b[i];
		}
		return res;
	}
}
