package com.android.geekyrss.tools;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;
import com.android.geekyrss.R;
import android.widget.TextView;

public class FeedSimpleCursorAdapter extends SimpleCursorAdapter implements
		Filterable {

	public FeedSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		int titleCol = c.getColumnIndex("title");
		int descCol = c.getColumnIndex("description");
		int countCol = c.getColumnIndex("count");
		int countColAll = c.getColumnIndex("countall");

		String name_value = c.getString(titleCol);
		TextView name_text = (TextView) v.findViewById(R.id.feedtitle);
		if (name_text != null) {
			name_text.setText(name_value);
		}
		String desc_value = c.getString(descCol);
		TextView desc_text = (TextView) v.findViewById(R.id.feeddesc);
		if (desc_text != null) {
			desc_text.setText(desc_value);
		}
		String count_value = c.getString(countCol);
		String count_all_value = c.getString(countColAll);
		TextView count_text = (TextView) v.findViewById(R.id.feedcount);
		if (count_text != null) {
			count_text.setText(count_value+"/"+count_all_value);
		}

	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

}