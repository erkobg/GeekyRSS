package com.android.geekyrss.tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.android.geekyrss.include.Article;
import com.android.geekyrss.include.Feed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class GeekyRSSDB extends SQLiteOpenHelper {

	private static final String CREATE_TABLE_FEEDS = "create table  if not exists feeds (feed_id integer primary key autoincrement, "
			+ "title text not null, link text not null unique,rsslink text not null , description text );";

	private static final String CREATE_TABLE_ARTICLES = "create table  if not exists articles (article_id integer primary key autoincrement, "
			+ "feed_id int not null, title text not null, description text not null, link text not null unique, "
			+ " image BLOB ,guid text not null, pubDate text not null, viewed integer default 0);";
	
	private static final String CREATE_TABLE_FEATURE_WORDS = "create table  if not exists feature_words ( mstring text not null);";

	private static final String FEEDS_TABLE = "feeds";
	private static final String ARTICLES_TABLE = "articles";
	private static final String FEATURE_LIST_TABLE = "feature_words";
	// private static String DB_PATH = "/mnt/sdcard/database/";
	private static String DB_PATH = Environment.getExternalStorageDirectory()
			+ "/com.android.geekyrss/";
	private static final String DATABASE_NAME = "geekyrss.db";
	private static int DATABASE_VERSION = 25;

	private SQLiteDatabase db;

	// region CONSTRUCTOR & CREATE & UPGRADE
	public GeekyRSSDB(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		try {
			createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (dbExist) {
			// do nothing - database already exist
		} else {

			this.getReadableDatabase();
			File folder = new File(DB_PATH);
			if (!folder.exists()) {
				folder.mkdirs();
				Log.i("GeekyRSS", "GeekyRSSDB:Created database folder:"
						+ DB_PATH);
			}
			db = SQLiteDatabase.openDatabase(DB_PATH + DATABASE_NAME, null,
					SQLiteDatabase.CREATE_IF_NECESSARY);
			db.execSQL(CREATE_TABLE_FEEDS);
			db.execSQL(CREATE_TABLE_ARTICLES);
			db.execSQL(CREATE_TABLE_FEATURE_WORDS);
			db.close();
		}
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		String outFileName = DB_PATH + DATABASE_NAME;
		File file = new File(outFileName);
		file.delete();
		onCreate(database);
	}

	// endregion

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = DB_PATH + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	// region Opening database
	public void openDataBaseRead() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY
						| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DATABASE_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE
						| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	public boolean isOpen() {
		return db.isOpen();
	}

	// endregion Opening database

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();
		super.close();
	}

	// region FEEDS
	public Cursor getFeeds() {
		return db
				.rawQuery(
						"select a.feed_id as _id,a.feed_id,a.title as title,a.link as link,a.rsslink as rsslink,a.description, ( select count(*) from articles where feed_id=a.feed_id and viewed=0) as count,  ( select count(*) from articles where feed_id=a.feed_id ) as countall from Feeds a",
						null);

	}

	public Feed getFeed(long feedid) {
		Feed feed = null;
		try {
			Cursor c = db.query(FEEDS_TABLE, new String[] { "feed_id", "title",
					"link", "rsslink", "description" }, "feed_id = ?",
					new String[] { String.valueOf(feedid) }, null, null, null);

			if (c.moveToFirst()) {
				feed = getFeedbyCursor(c);

			}

		} catch (SQLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		}
		return feed;
	}

	public Feed getFeedbyCursor(Cursor c) {
		Feed feed = null;
		try {
			feed = new Feed();
			feed.feedId = c.getLong(c.getColumnIndexOrThrow("feed_id"));
			feed.title = c.getString(c.getColumnIndexOrThrow("title"));
			feed.link = new URL(c.getString(c.getColumnIndexOrThrow("link")));
			feed.rsslink = new URL(c.getString(c
					.getColumnIndexOrThrow("rsslink")));
			feed.description = c.getString(c
					.getColumnIndexOrThrow("description"));

		} catch (MalformedURLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		}
		return feed;
	}

	public long insertFeed(String title, URL url, URL rsslink,
			String description) {
		ContentValues values = new ContentValues();
		values.put("title", title);
		values.put("link", url.toString());
		values.put("rsslink", rsslink.toString());
		values.put("description", description);
		try {
			return db.insertOrThrow(FEEDS_TABLE, null, values);
		} catch (SQLiteConstraintException sqlEx) {
		}
		return -1;

	}

	public boolean deleteFeed(Long feedId) {
		// first delete articles if any
		deleteAricles(feedId);
		// then delete the feed
		return (db.delete(FEEDS_TABLE, "feed_id=" + feedId.toString(), null) > 0);
	}

	// endregion FEEDS

	// region ARTICLES
	public Article getArticleFromCursor(Cursor c) {
		Article article = null;
		try {
			article = new Article();
			article.articleId = c
					.getLong(c.getColumnIndexOrThrow("article_id"));
			article.feedId = c.getLong(c.getColumnIndexOrThrow("feed_id"));
			article.title = c.getString(c.getColumnIndexOrThrow("title"));
			article.description = c.getString(c
					.getColumnIndexOrThrow("description"));
			article.link = new URL(c.getString(c.getColumnIndexOrThrow("link")));
			article.image = c.getBlob(c.getColumnIndexOrThrow("image"));
			article.guid = c.getString(c.getColumnIndexOrThrow("guid"));
			article.pubDate = c.getString(c.getColumnIndexOrThrow("pubDate"));
			article.viewed = c.getInt(c.getColumnIndexOrThrow("viewed"));
		} catch (MalformedURLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		}
		return article;
	}

	public Cursor getArticlesC(Long feedId) {
		return db.query(ARTICLES_TABLE, new String[] { "article_id",
				"feed_id as _id", "feed_id as feed_id", "title", "description",
				"link", "image", "guid", "pubDate", "viewed" }, "feed_id="
				+ feedId.toString(), null, null, null,
				"viewed ASC,pubDate DESC");

	}
	public Cursor getArticlesFeatured(String str) {
		return db.query(ARTICLES_TABLE, new String[] { "article_id",
				"feed_id as _id", "feed_id as feed_id", "title", "description",
				"link", "image", "guid", "pubDate", "viewed" }, 
				str
				, null, null, null,
				"viewed ASC,pubDate DESC");

	}

	public List<Article> getArticles(Long feedId) {
		ArrayList<Article> articles = new ArrayList<Article>();
		try {
			Cursor c = db.query(ARTICLES_TABLE, new String[] { "article_id",
					"feed_id", "title", "description", "link", "image", "guid",
					"pubDate", "viewed" }, "feed_id=" + feedId.toString(),
					null, null, null, null);

			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Article article = new Article();
				article.articleId = c.getLong(c
						.getColumnIndexOrThrow("article_id"));
				article.feedId = c.getLong(c.getColumnIndexOrThrow("feed_id"));
				article.title = c.getString(c.getColumnIndexOrThrow("title"));
				article.description = c.getString(c
						.getColumnIndexOrThrow("description"));
				article.link = new URL(c.getString(c
						.getColumnIndexOrThrow("link")));

				article.image = c.getBlob(c.getColumnIndexOrThrow("image"));
				article.guid = c.getString(c.getColumnIndexOrThrow("guid"));
				article.pubDate = c.getString(c
						.getColumnIndexOrThrow("pubDate"));
				article.viewed = c.getInt(c.getColumnIndexOrThrow("viewed"));

				articles.add(article);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		} catch (MalformedURLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		}
		return articles;
	}

	public Article getArticle(Long articleId) {
		try {
			// Cursor c = db.query(ARTICLES_TABLE, new String[] { "article_id",
			// "feed_id", "title", "description", "link", "image", "guid",
			// "pubDate","viewed" }, "article_id=" + articleId.toString(), null,
			// null, null, null);

			Cursor c = db
					.rawQuery(
							"select a.article_id,a.feed_id, a.title, a.description, a.link, a.image, "
									+ "a.guid, a.pubDate,a.viewed, b.title as feed_title "
									+ "from articles a "
									+ "inner join feeds b "
									+ "on a.feed_id = b.feed_id "
									+ " where a.article_id="
									+ articleId.toString(), null);

			c.moveToFirst();
			Article article = new Article();
			article.articleId = c
					.getLong(c.getColumnIndexOrThrow("article_id"));
			article.feedId = c.getLong(c.getColumnIndexOrThrow("feed_id"));
			article.feedTitle = c.getString(c
					.getColumnIndexOrThrow("feed_title"));
			article.title = c.getString(c.getColumnIndexOrThrow("title"));
			article.description = c.getString(c
					.getColumnIndexOrThrow("description"));
			article.link = new URL(c.getString(c.getColumnIndexOrThrow("link")));

			article.image = c.getBlob(c.getColumnIndexOrThrow("image"));
			article.guid = c.getString(c.getColumnIndexOrThrow("guid"));
			article.pubDate = c.getString(c.getColumnIndexOrThrow("pubDate"));
			article.viewed = c.getInt(c.getColumnIndexOrThrow("viewed"));

			c.moveToNext();
			return article;
		} catch (SQLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		} catch (MalformedURLException e) {
			Log.e("GeekyRSS", "GeekyRSSDB:" + e.toString());
		}
		return null;
	}

	public long insertArticle(Long feedId, String title, String description,
			URL url, byte[] image, String guid, String pubDate) {
		ContentValues values = new ContentValues();
		values.put("feed_id", feedId);
		values.put("title", title);
		values.put("description", description);
		values.put("link", url.toString());
		values.put("image", image);
		values.put("guid", guid);
		values.put("pubDate", pubDate);
		try {
			return (db.insertOrThrow(ARTICLES_TABLE, null, values));
		} catch (SQLiteConstraintException sqlEx) {
		}
		return -1;

	}

	public boolean deleteAricles(Long feedId) {
		return (db.delete(ARTICLES_TABLE, "feed_id=" + feedId.toString(), null) > 0);
	}

	public boolean deleteOldArticles() {
		return (db.delete(ARTICLES_TABLE,
				"date('now') > date('date_added','+10 days')", null) > 0);
	}

	public void updateAricle(Long articleId) {
		ContentValues cv = new ContentValues();
		cv.put("viewed", "1");
		db.update(ARTICLES_TABLE, cv, "article_id=" + articleId.toString(),
				null);
	}

	// endregion ARTICLES

	//region feature word list
	public long insertFeatureList(String str) {
		ContentValues values = new ContentValues();
		values.put("mstring", str);
		try {
			return (db.insertOrThrow(FEATURE_LIST_TABLE, null, values));
		} catch (SQLiteConstraintException sqlEx) {
		}
		return -1;

	}
	public Cursor getFeatureList() {
		return db.query(FEATURE_LIST_TABLE, new String[] { "mstring" }, 
				null
				, null, null, null,
				null);

	}

	public boolean deleteFeatureList(String str) {
		return (db.delete(FEATURE_LIST_TABLE,
				"mstring = '"+ str+"'", null) > 0);
	}
	//endregion
	
	
}
