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

public class ArticlesList extends ListActivity {

	private List<Article> articles = new ArrayList<Article>();;
	private Feed feed;
	private ProgressDialog progressDialog;
	private View touchedView;

	@Override
	protected void onCreate(Bundle icicle) {
		try {

			super.onCreate(icicle);

			setContentView(R.layout.articles_list);

			feed = new Feed();

			if (icicle != null) {
				feed.feedId = icicle.getLong("feed_id");
				feed.title = icicle.getString("title");
				feed.link = new URL(icicle.getString("link"));
				feed.rsslink = new URL(icicle.getString("rsslink"));
			} else {
				Bundle extras = getIntent().getExtras();
				feed.feedId = extras.getLong("feed_id");
				feed.title = extras.getString("title");
				feed.link = new URL(extras.getString("link"));
				feed.rsslink = new URL(extras.getString("rsslink"));
			}

			fillData();

		} catch (MalformedURLException e) {
			Log.e("GeekyRSS", "ArticlesList:"+e.toString());
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("feed_id", feed.feedId);
		outState.putString("title", feed.title);
		outState.putString("link", feed.link.toString());
		outState.putString("rsslink", feed.rsslink.toString());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, R.string.menu_sync);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		switch (item.getItemId()) {
		case 1:
			if (Utils.CheckInternetConnection(this)) {
				//Utils.droidDB.deleteAricles(feed.feedId);
				setListAdapter(null);
				progressDialog = ProgressDialog.show(ArticlesList.this,
						"Sync In progress", "Syncing articles");

				new Thread() {
					public void run() {
						try {

							RSSHandler rh = new RSSHandler();
							rh.updateArticles(ArticlesList.this, feed);

						} catch (Exception e) {

						}
						progressDialog.dismiss();
						runOnUiThread(new Runnable() {
							public void run() {

								fillData();

							}
						});

					}

				}.start();
				break;
			} else {
				Toast.makeText(this, "No internet connection!",
						Toast.LENGTH_SHORT).show();
			}
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final int pos = position;

		Toast.makeText(ArticlesList.this, "Loading", Toast.LENGTH_SHORT).show();

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
		if (touchedView != null) {
			// change style of list item to viewed
			TextView tv = (TextView) touchedView.findViewById(R.id.text1);
			tv.setTextColor(this.getResources().getColor(
					R.color.text_color_grey));
			tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			tv.setTypeface(Typeface.DEFAULT);
			touchedView = null;
		}
	}

	private void fillData() {
		articles.clear();
		Cursor cursor_articles = Utils.droidDB.getArticlesC(feed.feedId);
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
