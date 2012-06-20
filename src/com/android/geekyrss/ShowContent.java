package com.android.geekyrss;

import com.android.geekyrss.include.Article;
import com.android.geekyrss.tools.Utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ShowContent extends Activity {

	private WebView webView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.webView = new WebView(this);

		WebSettings settings = this.webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");

		String summary = "";

		Intent startingIntent = getIntent();

		if (startingIntent != null) {
			try {
				
				//because of the String buffer limits we have to use different 
				//approach to concatenate content 
				Bundle b = startingIntent
						.getBundleExtra("android.intent.extra.INTENT");
				Article article = Utils.droidDB.getArticle(b.getLong("articleId"));
				StringBuffer sb = new StringBuffer();
				sb.append(getString(R.string.ArticleContent1));
				sb.append(article.link.toString());
				sb.append(getString(R.string.ArticleContent2));
				sb.append(article.title);
				sb.append(getString(R.string.ArticleContent3));
				sb.append(article.feedTitle);
				sb.append(getString(R.string.ArticleContent4));
				sb.append(article.pubDate);
				sb.append(getString(R.string.ArticleContent5));
				if (article.image != null) {
					String image64 = Base64.encodeToString(
							article.image, Base64.DEFAULT);					
					sb.append("<center><img src=\"data:image/jpeg;base64,"
							+ image64 + "\" /></center><br/>");
				} 
				sb.append(getString(R.string.ArticleContent6));
				sb.append(article.description);
				sb.append(getString(R.string.ArticleContent7));summary= sb.toString();

			} catch (Exception e) {
				// TODO Auto-generated catch block

				Log.e("GeekyRSS", e.toString());
			}

		} else {
			summary = this.getString(R.string.ArticleEmpty);
		}

		this.webView.loadData(summary, "text/html; charset=UTF-8", null);
		this.webView.getSettings().setJavaScriptEnabled(false);

		setContentView(this.webView);
		
	}

}