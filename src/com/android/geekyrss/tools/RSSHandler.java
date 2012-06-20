package com.android.geekyrss.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.android.geekyrss.include.Article;
import com.android.geekyrss.include.Feed;

import android.content.Context;
import android.util.Log;

public class RSSHandler extends DefaultHandler {

	// Used to define what elements we are currently in
	private boolean inItem = false;
	private boolean inTitle = false;
	private boolean inDescription = false;
	private boolean inLink = false;

	private boolean inImage = false;
	private boolean inGuid = false;
	private boolean inPubDate = false;
	StringBuilder buf = null;

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private Feed currentFeed = new Feed();


	
	// The possible values for targetFlag
	private static final int TARGET_FEED = 0;
	private static final int TARGET_ARTICLES = 1;

	// A flag to know if looking for Articles or Feed name
	private int targetFlag;
	InputSource is = null;

	public RSSHandler() {
		
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (targetFlag == TARGET_FEED) {
			if (name.trim().equals("title")) {
				inTitle = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("link")) {
				inLink = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("description")) {
				inDescription = true;
				buf = new StringBuilder();
			}
		}

		if (targetFlag == TARGET_ARTICLES) {

			if (name.trim().equals("title")) {
				inTitle = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("description")) {
				inDescription = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("item")) {
				inItem = true;
			} else if (name.trim().equals("link")) {
				inLink = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("image")) {
				inImage = true;
				buf = new StringBuilder();
			}else if (name.trim().equals("enclosure")) {
				  String imgUrl = atts.getValue("url");
				  currentArticle.image = getBytesFromUrl(imgUrl);
	            
			} else if (name.trim().equals("guid")) {
				inGuid = true;
				buf = new StringBuilder();
			} else if (name.trim().equals("pubDate")) {
				inPubDate = true;
				buf = new StringBuilder();
			}
		}
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {
		if (targetFlag == TARGET_FEED) {			
			
			if (name.trim().equals("title")) {
				currentFeed.title = buf.toString().trim();
				inTitle = false;
			} else if (name.trim().equals("link")) {
				try {
					currentFeed.link = new URL(buf.toString().trim());
				} catch (MalformedURLException e) {
					Log.e("GeekyRSS", "RSSHandler+"+e.toString());
				}
				inLink = false;
			}
			else if (name.trim().equals("description")) {
				currentFeed.description = buf.toString().trim();
					inDescription = false;
			}
			
			
			// Check if looking for feed, and if feed is complete
			if (targetFlag == TARGET_FEED && currentFeed.link != null
					&& currentFeed.title != null && currentFeed.description != null) {
				// We know everything we need to know, so insert feed and exit
				currentFeed.feedId = Utils.droidDB.insertFeed(currentFeed.title,
						currentFeed.link,currentFeed.rsslink,currentFeed.description);				
				throw new SAXException();
			}			
		}
		if (targetFlag == TARGET_ARTICLES) {
			if (inItem) {
				if (name.trim().equals("title")) {
					currentArticle.title = buf.toString().trim();
					inTitle = false;
				} else if (name.trim().equals("description")) {
					currentArticle.description = buf.toString().trim();
					inDescription = false;
				} else if (name.trim().equals("item")) {
					inItem = false;
				} else if (name.trim().equals("link")) {
					try {
						currentArticle.link = new URL(buf.toString().trim());
					} catch (MalformedURLException e) {
						Log.e("GeekyRSS", "RSSHandler:"+e.toString());
					}
					inLink = false;
				} else if (name.trim().equals("image")) {
					currentArticle.image = buf.toString().trim().getBytes();
					inImage = false;
				} else if (name.trim().equals("guid")) {
					currentArticle.guid = buf.toString().trim();
					inGuid = false;
				} else if (name.trim().equals("pubDate")) {
					currentArticle.pubDate = buf.toString().trim();
					inPubDate = false;
				} 
				if (targetFlag == TARGET_ARTICLES && inItem != true) {
					Utils.droidDB.insertArticle(currentFeed.feedId, currentArticle.title,
							currentArticle.description, currentArticle.link,
							currentArticle.image, currentArticle.guid,
							currentArticle.pubDate);
					currentArticle.title = null;
					currentArticle.description = null;
					currentArticle.link = null;

					currentArticle.image = null;
					currentArticle.guid = null;
					currentArticle.pubDate = null;
				}
			}
		}
	}

	public void characters(char ch[], int start, int length) {

		if (targetFlag == TARGET_FEED) {
			if (!inItem) {
				if (inTitle || inDescription || inLink) {
					for (int i = start; i < start + length; i++) {
						buf.append(ch[i]);
					}
				}
			}
		}
		if (targetFlag == TARGET_ARTICLES) {
			if (inItem) {
				if (buf != null
						&& (inTitle ||inDescription || inLink || inImage || inGuid || inPubDate)) {
					for (int i = start; i < start + length; i++) {
						buf.append(ch[i]);
					}
				}
			}
		}

	}

	public long createFeed(Context ctx, URL url, URL rsslink) {
		try {
			targetFlag = TARGET_FEED;
			currentFeed.link = url;
			currentFeed.rsslink = rsslink;
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			is = new InputSource(url.openStream());
			xr.parse(is);
		} catch (IOException e) {
			Log.e("GeekyRSS ", "RSSHandler:"+e.toString());
		} catch (SAXException e) {
			Log.e("GeekyRSS", "RSSHandler:"+e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("GeekyRSS", "RSSHandler:"+e.toString());
		}
		return currentFeed.feedId;

	}

	

	public void updateArticles(Context ctx, Feed feed) {
		try {
			targetFlag = TARGET_ARTICLES;
			currentFeed = feed;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			
			xr.parse(new InputSource(currentFeed.rsslink.openStream()));

		} catch (IOException e) {
			Log.e("GeekyRSS", "RSSHandler:"+e.toString());
		} catch (SAXException e) {
			Log.e("GeekyRSS", "RSSHandler:"+e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("GeekyRSS","RSSHandler:"+e.toString());
		}
	}
	  public byte[] getBytesFromUrl(String imageURL) { 
          byte[] bytes = null;
          try {
                        URL url = new URL(imageURL); 
 
                        Log.d("ImageManager", "download begining");
                        Log.d("ImageManager", "download url:" + url);
//                        Log.d("ImageManager", "downloaded file name:" + fileName);
                        URLConnection ucon = url.openConnection();
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        ByteArrayBuffer baf = new ByteArrayBuffer(50);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                                baf.append((byte) current);
                        }
 
                        bytes =  baf.toByteArray();
//                        image = BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.length());

                } catch (IOException e) {
                        Log.d("ImageManager", "RSSHandler:"+e.toString());
                }
        return bytes;
 
        }

}
