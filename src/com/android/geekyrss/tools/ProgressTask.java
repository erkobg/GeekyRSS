package com.android.geekyrss.tools;

import java.util.List;

import com.android.geekyrss.include.Feed;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ProgressTask extends AsyncTask<Integer, Integer, Void> {
	ProgressBar progress;
	Context context;
	private List<Feed> feeds;

	public ProgressTask(ProgressBar progress, Context context, List<Feed> feeds) {
		this.progress = progress;
		this.context = context;
		this.feeds = feeds;

	}

	@Override
	protected void onPreExecute() {
		progress.setMax(100);

	}

	@Override
	protected Void doInBackground(Integer... params) {
		int i = 1;
		try {
			for (Feed f : feeds) {
				try {
					int n = i * 100 / feeds.size();
					
					//Utils.droidDB.deleteAricles(f.feedId);
					publishProgress(n/2);
					RSSHandler rh = new RSSHandler();
					
					rh.updateArticles(context, f);
					publishProgress(n);
					// SystemClock.sleep(100);
					i++;
				} catch (Exception e) {
					Log.e("GeekyRSS", e.toString());
				}
			}
		} catch (Exception e) {
			Log.e("GeekyRSS", e.toString());
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// increment progress bar by progress value
		progress.setProgress(values[0]);

	}

	@Override
	protected void onPostExecute(Void result) {
		Toast.makeText(context, "Syncing All Finished", Toast.LENGTH_SHORT)
				.show();
	}

}