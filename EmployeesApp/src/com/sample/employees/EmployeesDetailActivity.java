package com.sample.employees;

import com.sample.employeeslib.EmployeeProviderMetaData;
import com.sample.employeeslib.EmployeeProviderMetaData.EmployeeTableMetaData;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class EmployeesDetailActivity extends Activity{
	private static final String TAG = "EmployeeDetail";
	private ContentResolver mContentResolver = null;
	private Cursor mCursor;
	private Context mContext;
	private String mName;
	private String mAge;
	private String mDesignation;
	private String mDepartment;
	private long mEmpID;

	private EditText mNameEditText ;
	private EditText mAgeEditText;	
	private ArrayAdapter<CharSequence> mAdapterDep;
	private ArrayAdapter<CharSequence> mAdapterDes;
	private Spinner mSpinnerDep;
	private Spinner mSpinnerDes;

	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.i(TAG, "in onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.detail);

		mContext = this;

		mContentResolver = getContentResolver();

		Bundle bundle = getIntent().getExtras();

		mEmpID = bundle.getLong(EmployeesCommon.INTENT_EXTRA_EMPLOYEEID);

		Log.i(TAG, "EmployeeId = " + mEmpID);

		mNameEditText = (EditText) findViewById(R.id.et_name);
		mAgeEditText = (EditText) findViewById(R.id.et_age);

		mSpinnerDep = (Spinner) (findViewById(R.id.spinner_department));
		mAdapterDep = 
			ArrayAdapter.createFromResource(mContext, R.array.department, android.R.layout.simple_spinner_item);
		mAdapterDep.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		mSpinnerDep.setAdapter(mAdapterDep);

		mSpinnerDes = (Spinner) (findViewById(R.id.spinner_designation));
		mAdapterDes = 
			ArrayAdapter.createFromResource(mContext, R.array.designation, android.R.layout.simple_spinner_item);					
		mAdapterDes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		mSpinnerDes.setAdapter(mAdapterDes);

		if(mEmpID != -1) {
			populateEmployeeDetails();
		}
		Button udtBtn = (Button)findViewById(R.id.update);
		udtBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mEmpID != -1) {
					updateEmployee();
				}
				else {
					addEmployee();
				}
				finish();
			}
		});

		Button canBtn = (Button)findViewById(R.id.cancel);
		canBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0){
				finish();
			}
		});
	}

	private void populateEmployeeDetails() {

		new PopulateDetailTask(mEmpID).execute();
	}

	private void addEmployee() {
		Log.i(TAG, "in addEmployee");

		new AddTask().execute();
	}

	private void updateEmployee() {
		Log.i(TAG, "in updateEmployee ");		
		new UpdateTask().execute(mEmpID);
	}

	// PopulateDetailTask
	private class PopulateDetailTask extends AsyncTask<Void, Integer, Cursor >{
		Long mEmployeeId;
		private PopulateDetailTask(Long empId) {
			mEmployeeId = empId;
		}
		private static final String TAG = "PopulateAsyncTask";

		@Override
		protected Cursor doInBackground(Void...v) {
			Log.i(TAG, "in doInBackground, ThreadID = " + android.os.Process.myTid());

			Uri uri = EmployeeTableMetaData.SELECT_URI;

			mContentResolver = mContext.getContentResolver();

			String selection = EmployeeProviderMetaData.EmployeeTableMetaData._ID + " = ?";

			String[] selectionArgs = {String.valueOf(mEmployeeId)};

			mCursor = mContentResolver.query(uri, null, selection, selectionArgs, null);

			return mCursor;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			Log.i(TAG, "in onPostExecute, Row(s) = " + cursor.getCount());

			cursor = mCursor;

			Log.i(TAG, "Query complete, Row(s) = " + cursor.getCount());

			int iName = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_NAME);
			int iAge = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_AGE);
			int iDesignation = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DESIGNATION);
			int iDepartment = cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT);

			if(cursor.moveToFirst()){
				mName = cursor.getString(iName);
				mAge = cursor.getString(iAge);
				mDesignation = cursor.getString(iDesignation);
				mDepartment = cursor.getString(iDepartment);
			}

			Log.i(TAG, "Employee's name, age, designation, department = "
					+ mName + ", " + mAge + ", " + mDesignation + ", " + mDepartment);

			mNameEditText.setText(mName);

			mAgeEditText.setText(mAge);

		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			Log.i(TAG, "in onProgressUpdate " + progress[0]);

			setProgress(progress[0]);
		}
	}

	// UpdateTask
	private class UpdateTask extends AsyncTask<Long, Integer, Integer >{
		private static final String TAG = "UpdateAsyncTask";
		private int mCount;
		@Override
		protected Integer doInBackground(Long...empId) {

			Log.i(TAG, "in doInBackground, ThreadID = " + android.os.Process.myTid());

			ContentValues values = new ContentValues();
			ContentResolver cr = mContext.getContentResolver();

			values.put(EmployeeTableMetaData.EMPLOYEE_NAME, mNameEditText.getText().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_AGE, mAgeEditText.getText().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_DESIGNATION, mSpinnerDes.getSelectedItem().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT, mSpinnerDep.getSelectedItem().toString());

			String selection = EmployeeProviderMetaData.EmployeeTableMetaData._ID + " = ?";

			Uri uri = EmployeeTableMetaData.UPDATE_URI;
			String[] selectionArgs = {String.valueOf(empId[0])};

			//Uri updatedUri = Uri.withAppendedPath(uri, String.valueOf(mEmpID));

			mCount = cr.update(uri, values, selection, selectionArgs);

			return mCount;
		}
		@Override
		protected void onPostExecute(Integer result) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			result = mCount;
			if(result < 0){
				Log.e(TAG, "Failed to update, EmployeeId = " + mEmpID);
			}	
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			Log.i(TAG, "in onProgressUpdate " + progress[0]);

			setProgress(progress[0]);
		}
	}

	// AddTask

	private class AddTask extends AsyncTask<Void, Integer, Uri >{
		private static final String TAG = "AddAsyncTask";

		@Override
		protected Uri doInBackground(Void...v) {
			Log.i(TAG, "in doInBackground, ThreadID = " + android.os.Process.myTid());

			ContentValues values = new ContentValues();
			ContentResolver cr = mContext.getContentResolver();

			values.put(EmployeeTableMetaData.EMPLOYEE_NAME, mNameEditText.getText().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_AGE, mAgeEditText.getText().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_DESIGNATION, mSpinnerDes.getSelectedItem().toString());
			values.put(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT, mSpinnerDep.getSelectedItem().toString());

			Uri uri = EmployeeTableMetaData.INSERT_URI;

			Uri insertedUri = cr.insert(uri, values);

			Log.i(TAG, "inserted Uri = " + insertedUri);

			return insertedUri;
		}

		@Override
		protected void onPostExecute(Uri result) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			mContext.getContentResolver().notifyChange(result, null);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.i(TAG, "ThreadID = " + android.os.Process.myTid());
			Log.i(TAG, "in onProgressUpdate " + progress[0]);

			setProgress(progress[0]);
		}
	}
}

