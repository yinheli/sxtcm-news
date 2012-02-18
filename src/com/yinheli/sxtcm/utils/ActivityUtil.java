package com.yinheli.sxtcm.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Activity utils
 * 
 * @author yinheli <yinheli@gmail.com>
 *
 */
public class ActivityUtil {
	
	public static boolean isNewworkAvailable(Activity context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		return info != null && info.isAvailable();
	}

}
