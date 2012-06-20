package com.android.geekyrss.tools;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;
import com.android.geekyrss.R;
import android.widget.TextView;

public class ArticlesSimpleCursorAdapter extends SimpleCursorAdapter implements
		Filterable {

	public ArticlesSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {

		int titleCol = c.getColumnIndex("title");
		int viewedCol = c.getColumnIndex("viewed");

		String title_value = c.getString(titleCol);
		int viewed_val = c.getInt(viewedCol);

		TextView name_text = (TextView) v.findViewById(R.id.text1);
		name_text.setText(title_value);
		if (viewed_val == 1) {
			name_text.setTextColor(context.getResources().getColor(
					R.color.text_color_grey));
			name_text.setPaintFlags(name_text.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
			name_text.setTypeface(Typeface.DEFAULT);

		} else {
			name_text.setTextColor(context.getResources().getColor(
					R.color.text_color_def_article));
			name_text.setPaintFlags(name_text.getPaintFlags()
					& (~Paint.STRIKE_THRU_TEXT_FLAG));
			name_text.setTypeface(Typeface.DEFAULT_BOLD);
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