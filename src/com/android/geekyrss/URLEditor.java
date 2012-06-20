
package com.android.geekyrss;


import java.net.MalformedURLException;
import java.net.URL;

import com.android.geekyrss.R;
import com.android.geekyrss.include.Feed;
import com.android.geekyrss.tools.RSSHandler;
import com.android.geekyrss.tools.Utils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class URLEditor extends Activity  {

	EditText mText;
	TextView example1;
	TextView example2;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        setContentView(R.layout.url_editor);

        // Set up click handlers for the text field and button
        mText = (EditText) this.findViewById(R.id.url);
        
        
        if (icicle != null)
        	mText.setText(icicle.getString("link"));
        else
        {
        	//mText.setText("http://www.engadget.com/rss.xml");
        	//mText.setText("http://www.kaldata.com/rosebud/rss.php");
        }
        
        Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	okClicked();
            }          
        });
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	finish();
            }          
        });

        
        
        example1 = (TextView) findViewById(R.id.textView3);
        example1.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
            	examplesClicked(example1.getText().toString());
            }

			       
        });
        example2 = (TextView) findViewById(R.id.textView4);
        example2.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
            	examplesClicked(example2.getText().toString());
            }

			       
        });
        
        
    }

    protected void examplesClicked(String str) {
    	mText.setText(str);
    }
    protected void okClicked() {
    	try {
    		
    		Toast.makeText(this, "Adding...",Toast.LENGTH_SHORT).show();
    		RSSHandler rh = new RSSHandler();
    		long new_feeed_id =  rh.createFeed(this, new URL(mText.getText().toString()), new URL(mText.getText().toString()));
    		if(new_feeed_id!=-1)
    		{   
    			TextView errmsg= (TextView) this.findViewById(R.id.errorview);
    			errmsg.setVisibility(View.GONE);
				Feed feed = Utils.droidDB.getFeed(new_feeed_id);
				rh.updateArticles(this, feed);
				finish();
    		}
    		else
    		{
    			TextView errmsg= (TextView) this.findViewById(R.id.errorview);
    			errmsg.setText("Feed already exist!");
    			errmsg.setVisibility(View.VISIBLE);
    			
    		}
    	} catch (MalformedURLException e) {
    		Toast.makeText(this, "The URL you have entered is invalid.", 
    				Toast.LENGTH_SHORT).show();
    	}
    }

    

}
