package org.shujito.simplenotesconsumer;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.CheckedTextView;

class CustomCursorAdapter extends SimpleCursorAdapter
{
	public static final String TAG = CustomCursorAdapter.class.getSimpleName();
	static final int[] ids = { android.R.id.text1 };
	static final String[] cols = { "description" };
	
	public CustomCursorAdapter(Context context)
	{
		super(context, android.R.layout.simple_list_item_checked, null, cols, ids, 0);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		super.bindView(view, context, cursor);
		boolean done = cursor.getInt(cursor.getColumnIndex("done")) != 0;
		((CheckedTextView) view).setChecked(done);
	}
}