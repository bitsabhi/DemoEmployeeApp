package com.sample.employees;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.sample.employeeslib.EmployeeProviderMetaData;
import com.sample.employeeslib.EmployeeProviderMetaData.EmployeeTableMetaData;

public class EmployeesManagerActivity extends Activity{
	private static final String TAG = "EmployeeManager";
	private Cursor mCursor;
	private ContentResolver mContentResolver;
	private MyAdapter mAdapter;
	private ListView mEmpListView;
	private Context mContext;
	private String mQuery;
	private int mCounter;
	private Handler mHandler;
	private EmployeeContentObserver mObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		Log.i(TAG, "in onCreate ThreadID = " + android.os.Process.myTid());

		mHandler = new Handler();
		mObserver = new EmployeeContentObserver(mHandler);

		setContentView(R.layout.list);
		mContext = this;

		mEmpListView = (ListView)findViewById(android.R.id.list);

		mEmpListView.setTextFilterEnabled(true);

		new SetListViewTask().execute();

		// Detail and Edit onItemClick	
		mEmpListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//mCursor= mAdapter.getCursor();

				//long empId = mCursor.getLong(
				//mCursor.getColumnIndex(EmployeeProviderMetaData.EmployeeTableMetaData._ID));

				long empId = mAdapter.getItemId(position);
				Log.i(TAG, "ItemId = " + empId);

				String actionName = EmployeesCommon.INTENT_ACTION_EMPLOYEE_DETAILS;
				Intent intent = new Intent(actionName);
				intent.putExtra(EmployeesCommon.INTENT_EXTRA_EMPLOYEEID, empId);
				startActivity(intent);
			}
		});

		registerForContextMenu(mEmpListView);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i(TAG, "in onNewIntent");
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);	      
		}

		else if (EmployeesCommon.INTENT_ACTION_SEACH_SUGGESTION.equals(intent.getAction())) {
			Log.i(TAG, "in handleIntent");
			String id = intent.getDataString();
			Long empId = new Long(id);
			Log.i(TAG, "Emp ID = " + empId);

			String actionName = EmployeesCommon.INTENT_ACTION_EMPLOYEE_DETAILS;
			Intent intentDetail = new Intent(actionName);
			intentDetail.putExtra(EmployeesCommon.INTENT_EXTRA_EMPLOYEEID, empId);
			startActivity(intentDetail);
		}
	}
	private void doMySearch(String query) {
		mCounter++;
		mQuery = query;
		new SetListViewTask().execute();	
	}

	// Using OptionMenu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "in onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		Log.i(TAG, "in onOptionsItemSelected");

		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				Log.i(TAG, "in onMenuItemClick");

				switch (item.getItemId()){

				case R.id.item_Add :
					String actionName = EmployeesCommon.INTENT_ACTION_EMPLOYEE_DETAILS;
					Intent intent = new Intent(actionName);
					intent.putExtra(EmployeesCommon.INTENT_EXTRA_EMPLOYEEID, -1l);
					startActivity(intent);
					break;
				case R.id.item_ShowAll:
					mCounter = 0;
					new SetListViewTask().execute();
					break;	
				}
				return true;
			}
		});
		return true;
	}

	// Using ContextMenu 
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.i(TAG, "in onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG, "in onContextItemSelected");
		super.onContextItemSelected(item);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		final long itemId = mAdapter.getItemId(info.position);

		Log.i(TAG, "itemId = " + itemId);

		switch (item.getItemId()) {

		// Delete
		case(R.id.delete):
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Delete ?");

		builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				new DeleteTask().execute(itemId); 			
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});

		AlertDialog ad = builder.create();
		ad.show();
		return true;

		default :
			return false;
		}		
	}

	// Extending ResourceCursorAdapter

	/*class MyAdapter extends ResourceCursorAdapter {

		public MyAdapter(Context context, int view, Cursor cursor) {				
			super(context, view, cursor);
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Log.i(TAG, "in bindView");

			TextView nameTextView = (TextView)view.findViewById(R.id.name_tv);
			TextView depTextView = (TextView)view.findViewById(R.id.department_tv);
			ImageView star = (ImageView)view.findViewById(R.id.star);

			int iName = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_NAME);
			int iDep = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT);
			int iAge = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_AGE);
			Long age = cursor.getLong(iAge);

			if (age > 24) {
				nameTextView.setText(cursor.getString(iName));
				depTextView.setText(cursor.getString(iDep));
				star.setImageResource(R.drawable.favorites);
			}
			else {
				nameTextView.setText(cursor.getString(iName));
				depTextView.setText(cursor.getString(iDep));	
				star.setImageResource(0);
			}
		}
		@Override
		protected void onContentChanged() {
			Log.i(TAG, " in onContentChanged ThreadID = " + android.os.Process.myTid());

			new SetListViewTask().execute();
		}
	}
	 */

	public class MyAdapter extends BaseAdapter{

		private LayoutInflater mInflator;
		List <Employee> mEmpList;

		public MyAdapter(Context context, List<Employee> empList){
			mContext = context;
			mEmpList = empList;
			mInflator = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mEmpList.size();
		}

		@Override
		public Object getItem(int position) {
			return mEmpList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mEmpList.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflator.inflate(R.layout.child, null);
			}
			TextView nameTextView = (TextView)convertView.findViewById(R.id.name_tv);
			TextView depTextView = (TextView)convertView.findViewById(R.id.department_tv);

			nameTextView.setText(mEmpList.get(position).getName());
			depTextView.setText(mEmpList.get(position).getDepartment());

			return convertView;
		}

		public void changeList(List<Employee> empList) {
			mEmpList = empList;
		}
	}
	/*public static class MyAdapter extends BaseAdapter {

		private Context mContext;
		private LayoutInflater mInflator;
		private Cursor mCursor;

		public MyAdapter(Context context, Cursor cursor){
			mContext = context;			
			mInflator = LayoutInflater.from(context);
			mCursor = cursor;
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			mCursor.moveToPosition(position);
			return mCursor;
		}

		@Override
		public long getItemId(int position) {
			mCursor.moveToPosition(position);
			Long empId = mCursor.getLong(mCursor.getColumnIndex(EmployeeTableMetaData._ID));
			return empId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflator.inflate(R.layout.child, null);
			}

			TextView nameTextView = (TextView)convertView.findViewById(R.id.name_tv);
			TextView depTextView = (TextView)convertView.findViewById(R.id.department_tv);

			mCursor.moveToPosition(position);

			nameTextView.setText(mCursor.getString(mCursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_NAME)));
			depTextView.setText(mCursor.getString(mCursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT)));

			return convertView;
		}
	}*/

	public class EmployeeContentObserver extends ContentObserver {

		public EmployeeContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.i("EmployeeContentObserver", "in onChange");
			Log.i("EmployeeContentObserver", ", ThreadId = " + android.os.Process.myTid());
			new SetListViewTask().execute();
		}
	}

	private Cursor getEmployees() {

		if (mCounter > 0) {

			Uri uri = EmployeeTableMetaData.SEARCH_URI;

			mContentResolver = mContext.getContentResolver();

			String[] selectionArgs = {mQuery};

			Cursor cursor = mContentResolver.query(uri, null, null, selectionArgs, null);

			return cursor;
		}
		else {
			Uri uri = EmployeeTableMetaData.SELECT_URI;

			mContentResolver = mContext.getContentResolver();

			Cursor cursor = mContentResolver.query(uri, null, null, null, null);

			return cursor;
		}		
	}

	private Cursor changeCursor(Cursor cursor){

		if (mCursor != null) {
			mCursor.unregisterContentObserver(mObserver);
			mCursor = null;
			mCursor = cursor;
			mCursor.registerContentObserver(mObserver);
		}
		else {
			mCursor = cursor;
			mCursor.registerContentObserver(mObserver);			
		}
		return mCursor;		
	}

	private void modifyAdapter(Cursor cursor) {

		List<Employee> empList =  new ArrayList<Employee>();

		cursor.moveToFirst();

		do {
			String name = cursor.getString(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_NAME));
			String department = cursor.getString(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT));
			Long age = cursor.getLong(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_AGE));
			String designation = cursor.getString(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT));
			Long empId = cursor.getLong(cursor.getColumnIndex(EmployeeTableMetaData._ID));

			Employee empObj = new Employee(name, age, department, designation, empId);

			empList.add(empObj);
		}
		while (cursor.moveToNext());

		if (mAdapter == null) {
			mAdapter = new MyAdapter(mContext, empList);
		}
		else {
			mAdapter.changeList(empList);
		}

		mAdapter.notifyDataSetChanged();

		mEmpListView.setAdapter(mAdapter);
	}

	// SetListViewTask

	private class SetListViewTask extends AsyncTask<Void, Integer, Cursor > {
		private static final String TAG = "SetListViewTask";

		@Override
		protected Cursor doInBackground(Void...v) {
			Log.i(TAG, "in doInBackground, ThreadID = " + android.os.Process.myTid());

			Cursor cursor = getEmployees();

			return cursor;
		}
		@Override
		protected void onPostExecute(Cursor cursor) {
			if (cursor != null) {
				Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
				Log.i(TAG, "in onPostExecute, Row(s) = " + cursor.getCount());
				changeCursor(cursor);
				modifyAdapter(cursor);
			}

		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			Log.i(TAG, "in onProgressUpdate " + progress[0]);
			setProgress(progress[0]);
		}
	}

	// DeleteTask

	private class DeleteTask extends AsyncTask<Long, Integer, Integer >{
		private static final String TAG = "DeleteAsyncTask";

		@Override
		protected Integer doInBackground(Long...itemId) {
			/*mCursor = mAdapter.getCursor();

			long empId = mCursor.getLong(
					mCursor.getColumnIndex(EmployeeProviderMetaData.EmployeeTableMetaData._ID));*/

			long empId = itemId[0];

			Log.i(TAG, "employeeId = " + empId);

			Uri uri = EmployeeTableMetaData.DELETE_URI;

			/*Uri deletedUri = Uri.withAppendedPath(
					uri, String.valueOf(empId));*/

			String selection = EmployeeProviderMetaData.EmployeeTableMetaData._ID + " = ?";

			String[] selectionArgs = {String.valueOf(empId)};

			int count = mContentResolver.delete(uri, selection, selectionArgs);

			return count;
		}
		@Override
		protected void onPostExecute(Integer result) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());

			int count = result;

			if(count >= 0){
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Deletion Successful!");

				builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.cancel();
					}
				});
				AlertDialog ad = builder.create();
				ad.show();
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Deletion Unsuccessful!");

				builder.setNeutralButton("Okay", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.cancel();
					}
				});
				AlertDialog ad = builder.create();
				ad.show();
			}
		}
	}
}
