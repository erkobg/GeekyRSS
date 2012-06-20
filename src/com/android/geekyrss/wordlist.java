package com.android.geekyrss;

import java.util.ArrayList;

import com.android.geekyrss.tools.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class wordlist extends Activity {
	
	ListView lv;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.words);
		Button bt = (Button) findViewById(R.id.Add);
		lv = (ListView) findViewById(R.id.MyListView);
		final EditText myEditText = (EditText) findViewById(R.id.myEditText);
		Utils.featured_word_list = new ArrayList<String>();
		
		Cursor mCursor = Utils.droidDB.getFeatureList();
		for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
		    // The Cursor is now set to the right position
			Utils.featured_word_list.add(mCursor.getString(mCursor.getColumnIndex("mstring")));
		}
		
		
		final ArrayAdapter<String> aa;
		
		aa = new ArrayAdapter<String>(this, R.layout.words_row, R.id.textadd,
				Utils.featured_word_list);
		lv.setAdapter(aa);
		bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (myEditText.getText().length() != 0) {
					 String keyword = myEditText.getText().toString().trim();
					Utils.featured_word_list.add(0, keyword);
					Utils.droidDB.insertFeatureList(keyword);
					aa.notifyDataSetChanged();
					myEditText.setText("");
				}
			}
		});
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				AlertDialog.Builder ad = new AlertDialog.Builder(wordlist.this);
				ad.setTitle("Delete?");
				ad.setMessage("Are you sure you want to delete ?");
				final int positionToRemove = arg2;
				final View view = arg1;
				ad.setNegativeButton("Cancel", null);
				ad.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
						TextView txt =(TextView)view.findViewById(R.id.textadd);
		                 String keyword = txt.getText().toString();
		        		Utils.featured_word_list.remove(positionToRemove);
						Utils.droidDB.deleteFeatureList(keyword);
						aa.notifyDataSetChanged();
					}
				});
				ad.show();
			}
		});
	}
}