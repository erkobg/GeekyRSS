package com.android.geekyrss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.android.geekyrss.include.Article;
import com.android.geekyrss.include.Feed;
import com.android.geekyrss.tools.ArticlesSimpleCursorAdapter;
import com.android.geekyrss.tools.RSSHandler;
import com.android.geekyrss.tools.Utils;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FeatureList extends ListActivity {

	private List<Article> articles = new ArrayList<Article>();;
	private ProgressDialog progressDialog;
	private View touchedView;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.feature_list);
		fillData();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 2, 1, R.string.menu_feature_list);
		menu.add(0, 1, 1, R.string.menu_sync);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
		case 1:
			// if (Utils.CheckInternetConnection(this)) {
			// //Utils.droidDB.deleteAricles(feed.feedId);
			// setListAdapter(null);
			// progressDialog = ProgressDialog.show(FeatureList.this,
			// "Sync In progress", "Syncing articles");
			//
			// new Thread() {
			// public void run() {2
			// try {
			//
			// RSSHandler rh = new RSSHandler();
			// rh.updateArticles(FeatureList.this, feed);
			//
			// } catch (Exception e) {
			//
			// }
			// progressDialog.dismiss();
			// runOnUiThread(new Runnable() {
			// public void run() {
			//
			// fillData();
			//
			// }
			// });
			//
			// }
			//
			// }.start();
			//
			// } else {
			// Toast.makeText(this, "No internet connection!",
			// Toast.LENGTH_SHORT).show();
			// }
			break;
		case 2:
			Intent i = new Intent(this, wordlist.class);
			startActivityForResult(i, 1);
			break;
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final int pos = position;

		Toast.makeText(FeatureList.this, "Loading", Toast.LENGTH_SHORT).show();

		new Thread() {
			public void run() {
				Utils.droidDB.updateAricle(articles.get(pos).articleId);
			}
		}.start();

		// show data
		Intent itemintent = new Intent(this, ShowContent.class);
		Bundle b = new Bundle();

		b.putLong("articleId", articles.get(pos).articleId);
		itemintent.putExtra("android.intent.extra.INTENT", b);

		this.startActivityForResult(itemintent, 0);

		touchedView = v;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			if (touchedView != null) {
				// change style of list item to viewed
				TextView tv = (TextView) touchedView.findViewById(R.id.text1);
				tv.setTextColor(this.getResources().getColor(
						R.color.text_color_grey));
				tv.setPaintFlags(tv.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
				tv.setTypeface(Typeface.DEFAULT);
				touchedView = null;
			}
			break;
		case 1:
			fillData();
			break;
		}

	}

	private void fillData() {
		articles.clear();
		Cursor cursor_articles = null;
		Cursor mCursor = Utils.droidDB.getFeatureList();
		String str = null;
		for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
				.moveToNext()) {
			String keyword = mCursor.getString(mCursor
					.getColumnIndex("mstring"));
			if (str != null) {
				str = str + " or title like '%" + keyword
						+ "%' or description like '%" + keyword + "%'";
			} else {
				str = "title like '%" + keyword + "%' or description like '%"
						+ keyword + "%'";
			}
		}
		cursor_articles = Utils.droidDB.getArticlesFeatured(str);
		while (cursor_articles.moveToNext()) {
			Article f = Utils.droidDB.getArticleFromCursor(cursor_articles);

			articles.add(f);
		}

		String[] from = new String[] { "title" };
		int[] to = new int[] { R.id.text1 };

		ArticlesSimpleCursorAdapter feedsAdp = new ArticlesSimpleCursorAdapter(
				this, R.layout.article_row, cursor_articles, from, to);
		setListAdapter(feedsAdp);
	}

}
