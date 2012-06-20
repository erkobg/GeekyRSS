package com.android.geekyrss;

import java.util.Timer;
import java.util.TimerTask;

import com.android.geekyrss.tools.GeekyRSSDB;
import com.android.geekyrss.tools.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class BackgroundService extends Service {
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();
	private static Timer timer = new Timer();
	private Context ctx;
	private boolean flag_opened_by_service = false;

	long FIRST_EXECUTION_AFTER = 0; // 5 min
	long SCHEDULE_PERIOD = 3600000; // 60min

	// long SCHEDULE_PERIOD = 5000;

	public class LocalBinder extends Binder {
		BackgroundService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return BackgroundService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onCreate() {
		super.onCreate();
		ctx = this;
		startService();
	}

	private void startService() {
		try{
		timer.scheduleAtFixedRate(new mainTask(), FIRST_EXECUTION_AFTER,
				SCHEDULE_PERIOD);
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	private class mainTask extends TimerTask {
		public void run() {
			toastHandler.sendEmptyMessage(0);
		}
	}

	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
	}

	private final Handler toastHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Toast.makeText(getApplicationContext(), "service task",
			// Toast.LENGTH_SHORT).show();
			if (Utils.droidDB == null) {
				Utils.droidDB = new GeekyRSSDB(ctx);
				Utils.droidDB.openDataBase();
			}
			if (!Utils.droidDB.isOpen()) {
				flag_opened_by_service = true;
				Utils.droidDB.openDataBase();
			}

			Utils.droidDB.deleteOldArticles();

			if (flag_opened_by_service) {
				flag_opened_by_service = false;
				Utils.droidDB.close();
			}
		}
	};
}