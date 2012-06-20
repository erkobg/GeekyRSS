package com.android.geekyrss;

import java.util.ArrayList;
import java.util.List;

import com.android.geekyrss.BackgroundService.LocalBinder;
import com.android.geekyrss.include.Feed;

import com.android.geekyrss.tools.FeedSimpleCursorAdapter;
import com.android.geekyrss.tools.GeekyRSSDB;
import com.android.geekyrss.tools.RSSHandler;
import com.android.geekyrss.tools.Utils;
import com.android.geekyrss.tools.ProgressTask;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class FeedsList extends ListActivity {

	private static final int ACTIVITY_DELETE = 1;
	private static final int ACTIVITY_INSERT = 2;
	private static final int ACTIVITY_VIEW = 3;
	private static final int ACTIVITY_SYNC_ALL = 4;
	private static final int ACTIVITY_SYNC = 5;
	private static final int ACTIVITY_STOP_SERVICE = 6;
	private static final int ACTIVITY_ABOUT = 7;
	private static final int FEATURE_LIST = 8;

	private List<Feed> feeds = new ArrayList<Feed>();
	private ProgressDialog progressDialog;
	BackgroundService mService;
	boolean mBound = false;

	@Override
	protected void onCreate(Bundle icicle) {
		try {
			super.onCreate(icicle);
			
			setContentView(R.layout.feeds_list);
			if(Utils.droidDB==null)
			{
				Utils.droidDB = new GeekyRSSDB(this);
				Utils.droidDB.openDataBase();
			}
			if(!Utils.droidDB.isOpen())
				Utils.droidDB.openDataBase();
			fillData();
			if (Utils.CheckInternetConnection(this)) {
				Toast.makeText(this, "Syncing All", Toast.LENGTH_SHORT).show();
				ProgressBar loading = (ProgressBar) findViewById(R.id.progressBar1);
				ProgressTask task = new ProgressTask(loading, this, feeds);
				task.execute(0);
			} else {
				Toast.makeText(this, "No internet connection: offline reading",
						Toast.LENGTH_SHORT).show();
			}

		} catch (Throwable e) {
			Log.e("GeekyRSS","FeedsList:"+ e.toString());
		}
		 Intent intent = new Intent(this, BackgroundService.class);
	      bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ACTIVITY_INSERT, 1, R.string.menu_insert);
		menu.add(0, FEATURE_LIST, 2, R.string.menu_feature);
		menu.add(0, ACTIVITY_SYNC_ALL, 3, R.string.menu_sync_all);
		menu.add(0, ACTIVITY_STOP_SERVICE, 4, R.string.menu_stop_service);
		menu.add(0, ACTIVITY_ABOUT, 5, R.string.menu_about);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
		case ACTIVITY_INSERT:
			createFeed();
			break;
		case FEATURE_LIST:
			Intent i = new Intent(this, FeatureList.class);
			startActivity(i);
			break;
		case ACTIVITY_DELETE:
			setListAdapter(null);

			Feed f = feeds.get(info.position);
			Utils.droidDB.deleteFeed(f.feedId);
			fillData();
			Toast.makeText(FeedsList.this, "Feed : " + f.title + "  deleted!",
					Toast.LENGTH_LONG).show();
			break;
		case ACTIVITY_SYNC_ALL:
			if (Utils.CheckInternetConnection(this)) {
				progressDialog = ProgressDialog.show(FeedsList.this,
						"Sync In progress", "Syncing all!");
				new Thread() {
					public void run() {
						try {
							for (Feed f : feeds) {
								//Utils.droidDB.deleteAricles(f.feedId);
								RSSHandler rh = new RSSHandler();
								rh.updateArticles(FeedsList.this, f);
							}

						} catch (Exception e) {

						}
						progressDialog.dismiss();

					}
				}.start();
			} else {
				Toast.makeText(this, "No internet connection!",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case ACTIVITY_SYNC:
			if (Utils.CheckInternetConnection(this)) {
				info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
				final Feed fs = feeds.get(info.position);
				progressDialog = ProgressDialog.show(FeedsList.this,
						"Sync In progress", "Syncing : " + fs.title);
				new Thread() {
					public void run() {
						try {

							//Utils.droidDB.deleteAricles(fs.feedId);
							RSSHandler rh = new RSSHandler();
							rh.updateArticles(FeedsList.this, fs);
							Toast.makeText(FeedsList.this,
									"Feed : " + fs.title + "  deleted!",
									Toast.LENGTH_LONG).show();

						} catch (Exception e) {

						}
						progressDialog.dismiss();

					}
				}.start();
			} else {
				Toast.makeText(this, "No internet connection!",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case ACTIVITY_STOP_SERVICE:
			if (mBound) {
	            unbindService(mConnection);
	            mBound = false;
	        }

			break;
		case ACTIVITY_ABOUT:
			Toast.makeText(this, this.getString(R.string.about_text),
					Toast.LENGTH_LONG).show();

			break;
		}
		return true;
	}

	private void createFeed() {
		Intent i = new Intent(this, URLEditor.class);
		startActivityForResult(i, ACTIVITY_INSERT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		fillData();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent i = new Intent(this, ArticlesList.class);
		i.putExtra("feed_id", feeds.get(position).feedId);
		i.putExtra("title", feeds.get(position).title);
		i.putExtra("link", feeds.get(position).link.toString());
		i.putExtra("rsslink", feeds.get(position).rsslink.toString());
		startActivityForResult(i, ACTIVITY_VIEW);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			if (info.position == 0)
				return;
			menu.setHeaderTitle(feeds.get(info.position).title);
			menu.add(Menu.NONE, ACTIVITY_DELETE, 1, "Delete");
			menu.add(Menu.NONE, ACTIVITY_SYNC, 2, "Sync");
		}
	}

	private void fillData() {

		feeds.clear();
		Cursor cursor_feeds = Utils.droidDB.getFeeds();
		while (cursor_feeds.moveToNext()) {
			Feed f = Utils.droidDB.getFeedbyCursor(cursor_feeds);

			feeds.add(f);
		}
		String[] from = new String[] { "title", "count" };
		int[] to = new int[] { R.id.feedtitle, R.id.feeddesc };

		FeedSimpleCursorAdapter feedsAdp = new FeedSimpleCursorAdapter(this,
				R.layout.feeds_row, cursor_feeds, from, to);
		setListAdapter(feedsAdp);
		ListView list = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(list);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
		Utils.droidDB.close();
	}
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
   

}
