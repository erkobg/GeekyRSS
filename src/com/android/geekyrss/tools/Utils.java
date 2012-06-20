package com.android.geekyrss.tools;

import java.util.ArrayList;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final  class Utils {
	public static GeekyRSSDB droidDB;
	public static ArrayList<String> featured_word_list;
	static public boolean CheckInternetConnection(Context ctx)
	{
		ConnectivityManager conMgr =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo i = conMgr.getActiveNetworkInfo();
		  if (i == null)
		    return false;
		  if (!i.isConnected())
		    return false;
		  if (!i.isAvailable())
		    return false;
		  return true;
	}
}
