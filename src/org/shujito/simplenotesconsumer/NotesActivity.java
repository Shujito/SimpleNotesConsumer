package org.shujito.simplenotesconsumer;

import java.util.List;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class NotesActivity extends ActionBarActivity
	implements
	OnItemClickListener,
	OnItemLongClickListener,
	LoaderManager.LoaderCallbacks<Cursor>
{
	public static final String TAG = NotesActivity.class.getSimpleName();
	private Uri CONTENT_URI = Uri.parse("content://org.shujito.simplenotes/notes");
	private ListView mListView = null;
	private CustomCursorAdapter mAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.e(TAG, "?");
		super.onCreate(savedInstanceState);
		if (this.isProviderAvailable())
		{
			Log.i(TAG, "provider available");
			TextView tv = new TextView(this);
			tv.setText("No notes!");
			this.mAdapter = new CustomCursorAdapter(this);
			this.mListView = new ListView(this);
			this.mListView.setAdapter(this.mAdapter);
			this.mListView.setOnItemClickListener(this);
			this.mListView.setOnItemLongClickListener(this);
			this.mListView.setEmptyView(tv);
			this.setContentView(this.mListView);
			this.getSupportLoaderManager().initLoader(0, null, this);
		}
		else
		{
			Log.w(TAG, "provider not available");
			TextView tv = new TextView(this);
			tv.setText("No provider available");
			this.setContentView(tv);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (this.isProviderAvailable())
		{
			this.getMenuInflater().inflate(R.menu.notes, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_add:
				final EditText editText = new EditText(this);
				editText.setSingleLine(true);
				new AlertDialog.Builder(this)
					.setTitle("New note")
					.setView(editText)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
						new OnClickListener()
						{
							NotesActivity $this = NotesActivity.this;
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								ContentValues cvs = new ContentValues();
								String uuid = UUID.randomUUID().toString();
								uuid = uuid.replaceAll("-", "");
								cvs.put("_id", uuid);
								cvs.put("description", editText.getText().toString());
								cvs.put("done", false);
								$this.getContentResolver().insert(CONTENT_URI, cvs);
							}
						}).show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> ada, View v, int pos, long id)
	{
		Cursor cursor = this.mAdapter.getCursor();
		cursor.moveToPosition(pos);
		String _id = cursor.getString(cursor.getColumnIndex("_id"));
		boolean done = cursor.getInt(cursor.getColumnIndex("done")) != 0;
		ContentValues cvs = new ContentValues();
		cvs.put("done", !done);
		String[] args = { _id };
		this.getContentResolver().update(CONTENT_URI, cvs, "_id = ?", args);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> ada, View v, int pos, long id)
	{
		Cursor cursor = this.mAdapter.getCursor();
		cursor.moveToPosition(pos);
		String _id = cursor.getString(cursor.getColumnIndex("_id"));
		String[] args = { _id };
		this.getContentResolver().delete(CONTENT_URI, "_id = ?", args);
		return true;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args)
	{
		return new CursorLoader(this, CONTENT_URI, null, null, null, "done ASC");
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		this.mAdapter.swapCursor(cursor);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		this.mAdapter.swapCursor(null);
	}
	
	private boolean isProviderAvailable()
	{
		PackageManager packageMan = this.getPackageManager();
		List<PackageInfo> packageInfoList = packageMan
			.getInstalledPackages(PackageManager.GET_PROVIDERS);
		for (PackageInfo packageInfo : packageInfoList)
		{
			if (packageInfo.providers == null)
				continue;
			for (ProviderInfo providerInfo : packageInfo.providers)
			{
				if ("org.shujito.simplenotes.provider"
					.equals(providerInfo.readPermission))
				{
					return true;
				}
			}
		}
		return false;
	}
}
