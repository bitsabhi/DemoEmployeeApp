package com.sample.provider;

import java.util.HashMap;

import com.sample.employeeslib.EmployeeProviderMetaData;
import com.sample.employeeslib.EmployeeProviderMetaData.EmployeeTableMetaData;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class EmployeeProvider extends ContentProvider{
	private static final String TAG = "EmployeeProvider";

	private static HashMap<String, String> msEmplolyeesProjectionMap;
	static {
		msEmplolyeesProjectionMap = new HashMap<String, String>();

		msEmplolyeesProjectionMap.put(EmployeeProviderMetaData.EmployeeTableMetaData._ID, 
				EmployeeProviderMetaData.EmployeeTableMetaData._ID);

		msEmplolyeesProjectionMap.put(EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_NAME
				,EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_NAME);

		msEmplolyeesProjectionMap.put(EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_AGE, 
				EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_AGE);

		msEmplolyeesProjectionMap.put(EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_DESIGNATION, 
				EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_DESIGNATION);

		msEmplolyeesProjectionMap.put(EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_DEPARTMENT
				, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEE_DEPARTMENT);
	}

	// Setup Uri
	private static final UriMatcher msUriMatcher;

	private static final int MULTIPLE_EMPLOYEES_SELECT_URI_INDICATOR = 1;

	private static final int SINGLE_EMPLOYEE_SELECT_URI_INDICATOR = 2;

	private static final int MULTIPLE_EMPLOYEES_DELETE_URI_INDICATOR = 3;

	private static final int SINGLE_EMPLOYEE_DELETE_URI_INDICATOR = 4;

	private static final int MULTIPLE_EMPLOYEES_UPDATE_URI_INDICATOR = 5;

	private static final int SINGLE_EMPLOYEE_UPDATE_URI_INDICATOR = 6;

	private static final int MULTIPLE_EMPLOYEES_INSERT_URI_INDICATOR = 0;

	private static final int MULTIPLE_EMPLOYEES_SEARCH_URI_INDICATOR = 7;

	private static final int MULTIPLE_EMPLOYEES_SUGGEST_URI_INDICATOR = 8;

	static {
		msUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.INSERT, 
				MULTIPLE_EMPLOYEES_INSERT_URI_INDICATOR);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.UPDATE, 
				MULTIPLE_EMPLOYEES_UPDATE_URI_INDICATOR);
		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.UPDATE + "/#", 
				SINGLE_EMPLOYEE_UPDATE_URI_INDICATOR);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.DELETE, 
				MULTIPLE_EMPLOYEES_DELETE_URI_INDICATOR);
		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.DELETE + "/#", 
				SINGLE_EMPLOYEE_DELETE_URI_INDICATOR);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.SELECT, 
				MULTIPLE_EMPLOYEES_SELECT_URI_INDICATOR);
		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.SELECT + "/#", 
				SINGLE_EMPLOYEE_SELECT_URI_INDICATOR);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeProviderMetaData.EmployeeTableMetaData.EMPLOYEES
				+ "/" + EmployeeProviderMetaData.EmployeeTableMetaData.SEARCH, 
				MULTIPLE_EMPLOYEES_SEARCH_URI_INDICATOR);

		msUriMatcher.addURI(
				EmployeeProviderMetaData.AUTHORITY, EmployeeTableMetaData.SUGGEST
				, MULTIPLE_EMPLOYEES_SUGGEST_URI_INDICATOR);

	}

	// Database Setup

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, EmployeeProviderMetaData.DATABASE_NAME, null, EmployeeProviderMetaData.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "DatabaseHelper method onCreate");

			db.execSQL("CREATE TABLE " + EmployeeTableMetaData.TABLE_NAME + " ("
					+ EmployeeTableMetaData._ID + " INTEGER PRIMARY KEY,"
					+ EmployeeTableMetaData.EMPLOYEE_NAME + " TEXT,"
					+ EmployeeTableMetaData.EMPLOYEE_AGE + " TEXT,"
					+ EmployeeTableMetaData.EMPLOYEE_DESIGNATION + " TEXT,"
					+ EmployeeTableMetaData.EMPLOYEE_DEPARTMENT + " TEXT,"
					+ EmployeeTableMetaData.CREATED_DATE + " INTEGER,"
					+ EmployeeTableMetaData.MODIFIED_DATE + " INTEGER"
					+ ");"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "DatabaseHelper method onUpgrade");

			Log.i(TAG, "Upgrading Database from version " + oldVersion + "to " + newVersion
					+ "which will destroy all old data");

			db.execSQL("DROP TABLE IF EXISTS " + EmployeeTableMetaData.TABLE_NAME);

			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	// Component creation callback

	@Override
	public boolean onCreate() {
		Log.i(TAG, "in onCreate");

		mOpenHelper = new DatabaseHelper(getContext());

		return true;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		Log.i(TAG, "delete method, Uri = " + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;

		switch(msUriMatcher.match(uri)) {

		case MULTIPLE_EMPLOYEES_DELETE_URI_INDICATOR :

			Log.i(TAG, "MULTIPLE_EMPLOYEES_DELETE_URI_INDICATOR");

			count = db.delete(EmployeeTableMetaData.TABLE_NAME, where, whereArgs);

			break;

		case SINGLE_EMPLOYEE_DELETE_URI_INDICATOR :

			Log.i(TAG, "SINGLE_EMPLOYEE_DELETE_URI_INDICATOR");

			String rowId = uri.getPathSegments().get(2);

			String finalWhere = EmployeeTableMetaData._ID + "=" + rowId; 

			//String whereClause = (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");

			Log.i(TAG, "finalWhere = " + finalWhere);
			//Log.i(TAG, "where = " + whereClause);

			count = db.delete(EmployeeTableMetaData.TABLE_NAME, finalWhere, null);

			// count = db.delete(EmployeeTableMetaData.TABLE_NAME, whereClause, whereArgs);

			break;

		default:
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(EmployeeTableMetaData.SELECT_URI, null);
		return count;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Log.i(TAG, "method insert, Uri = " + uri);

		if (msUriMatcher.match(uri)
				!= MULTIPLE_EMPLOYEES_INSERT_URI_INDICATOR ){
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}

		Log.i(TAG, "MULTIPLE_EMPLOYEES_INSERT_URI_INDICATOR");

		ContentValues values ;

		if(initialValues != null){
			values = new ContentValues(initialValues);
		}
		else {
			values = new ContentValues();
		}

		Long now = Long.valueOf(System.currentTimeMillis());

		if (values.containsKey(EmployeeTableMetaData.CREATED_DATE) == false){
			values.put(EmployeeTableMetaData.CREATED_DATE, now);
		}

		if (values.containsKey(EmployeeTableMetaData.MODIFIED_DATE) == false){
			values.put(EmployeeTableMetaData.MODIFIED_DATE, now);
		}

		if (values.containsKey(EmployeeTableMetaData.EMPLOYEE_NAME) == false){
			throw new SQLException("Failed to insert row because employee name is needed" + uri);
		}

		if(values.containsKey(EmployeeTableMetaData.EMPLOYEE_AGE )== false){
			values.put(EmployeeTableMetaData.EMPLOYEE_AGE, "Unknown age");
		}

		if(values.containsKey(EmployeeTableMetaData.EMPLOYEE_DESIGNATION )== false){
			values.put(EmployeeTableMetaData.EMPLOYEE_DESIGNATION, "Unknown designation");
		}

		if(values.containsKey(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT )== false){
			values.put(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT, "Unknown department");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(EmployeeTableMetaData.TABLE_NAME, EmployeeTableMetaData.EMPLOYEE_NAME, values);

		if(rowId > 0) {
			Uri insertedUri = ContentUris.withAppendedId(EmployeeTableMetaData.CONTENT_URI, rowId);

			Long t1 = System.currentTimeMillis();
			Log.i(TAG, "t1 = " + t1);
			
			getContext().getContentResolver().notifyChange(uri, null);
			getContext().getContentResolver().notifyChange(EmployeeTableMetaData.SELECT_URI, null);

			Long t2 = System.currentTimeMillis();
			Long total = t2 - t1;
			Log.i(TAG, "Total time = " + total);
			
			return insertedUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Log.i(TAG, "method query, Uri = " + uri);

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch(msUriMatcher.match(uri)){

		case MULTIPLE_EMPLOYEES_SELECT_URI_INDICATOR :
			qb.setTables(EmployeeTableMetaData.TABLE_NAME);
			qb.setProjectionMap(msEmplolyeesProjectionMap);
			Log.i(TAG, "MULTIPLE_EMPLOYEES_SELECT_URI_INDICATOR");
			break;

		case SINGLE_EMPLOYEE_SELECT_URI_INDICATOR :
			qb.setTables(EmployeeTableMetaData.TABLE_NAME);
			qb.setProjectionMap(msEmplolyeesProjectionMap);
			qb.appendWhere(EmployeeTableMetaData._ID + "=" + uri.getPathSegments().get(2));
			Log.i(TAG, "SINGLE_EMPLOYEE_SELECT_URI_INDICATOR");
			break;

		case MULTIPLE_EMPLOYEES_SEARCH_URI_INDICATOR :
			qb.setTables(EmployeeTableMetaData.TABLE_NAME);
			qb.setProjectionMap(msEmplolyeesProjectionMap);
			Log.i(TAG, "MULTIPLE_EMPLOYEES_SEARCH_URI_INDICATOR");
			break;

		case MULTIPLE_EMPLOYEES_SUGGEST_URI_INDICATOR :
			qb.setTables(EmployeeTableMetaData.TABLE_NAME);
			qb.setProjectionMap(msEmplolyeesProjectionMap);
			Log.i(TAG, "MULTIPLE_EMPLOYEES_SUGGEST_URI_INDICATOR");
			break;

		default :
			throw new IllegalArgumentException("Unknown Uri " + uri);
		}

		String orderBy;
		if (TextUtils.isEmpty(sortOrder)){
			orderBy = EmployeeTableMetaData.DEFAULT_SORT_ORDER;
		}
		else {
			orderBy = sortOrder;
		}

		// Run the query
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Cursor c = null;

		switch(msUriMatcher.match(uri)){
		case MULTIPLE_EMPLOYEES_SEARCH_URI_INDICATOR :

			String where = EmployeeTableMetaData.EMPLOYEE_NAME +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_DEPARTMENT +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_DESIGNATION +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_AGE +  " LIKE ? ";

			String[] whereArgs = {"%" + selectionArgs[0] + "%"};

			Log.i(TAG, where);

			c = db.query(
					EmployeeTableMetaData.TABLE_NAME, projection, where, whereArgs, null, null, orderBy);

			c.setNotificationUri(getContext().getContentResolver(), uri);
			c.setNotificationUri(getContext().getContentResolver(), EmployeeTableMetaData.SELECT_URI);

			break;

		case MULTIPLE_EMPLOYEES_SELECT_URI_INDICATOR :

			c = db.query(
					EmployeeTableMetaData.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			c.setNotificationUri(getContext().getContentResolver(), EmployeeTableMetaData.SELECT_URI);
			break;

		case MULTIPLE_EMPLOYEES_SUGGEST_URI_INDICATOR :			

			String query = selectionArgs[0];

			String[] columnNames = {
					EmployeeTableMetaData._ID,
					SearchManager.SUGGEST_COLUMN_TEXT_1, 
					SearchManager.SUGGEST_COLUMN_TEXT_2,
					SearchManager.SUGGEST_COLUMN_ICON_2,
					SearchManager.SUGGEST_COLUMN_INTENT_DATA
			};

			Log.i(TAG, query);

			MatrixCursor matrixCursor = new MatrixCursor(columnNames);	

			String sel = EmployeeTableMetaData.EMPLOYEE_NAME +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_DEPARTMENT +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_DESIGNATION +  " LIKE ? " + " OR " + 
			EmployeeTableMetaData.EMPLOYEE_AGE +  " LIKE ? ";

			String[] selArgs = {"%" + selectionArgs[0] + "%"};

			Cursor cursor = db.query(
					EmployeeTableMetaData.TABLE_NAME, projection, sel, selArgs, null, null, null);

			cursor.moveToFirst(); 

			do {

				Long id = cursor.getLong(cursor.getColumnIndex(EmployeeTableMetaData._ID));
				String name = cursor.getString(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_NAME));
				String department = 
					"Dep : " + cursor.getString(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_DEPARTMENT));
				Long age = cursor.getLong(cursor.getColumnIndex(EmployeeTableMetaData.EMPLOYEE_AGE));

				Object[] columnValues = new Object[columnNames.length];

				if (age < 25) {
					columnValues[0] = id;
					columnValues[1] = name;
					columnValues[2] = department;
					columnValues[3] = null;
					columnValues[4] = id;

				}
				else {
					columnValues[0] = id;
					columnValues[1] = name;
					columnValues[2] = department;
					columnValues[3] = R.drawable.favorites;
					columnValues[4] = id;
				}
				matrixCursor.addRow(columnValues);
			}

			while (cursor.moveToNext());

			cursor.close();

			return matrixCursor;			
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		Log.i(TAG, "method update, Uri = " + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count;

		switch(msUriMatcher.match(uri)){

		case MULTIPLE_EMPLOYEES_UPDATE_URI_INDICATOR :
			Log.i(TAG, "MULTIPLE_EMPLOYEES_UPDATE_URI_INDICATOR");
			count = db.update(EmployeeTableMetaData.TABLE_NAME, values, where, whereArgs);
			break;

		case SINGLE_EMPLOYEE_UPDATE_URI_INDICATOR :
			Log.i(TAG, "SINGLE_EMPLOYEE_UPDATE_URI_INDICATOR");

			String rowId = uri.getPathSegments().get(2);

			String finalWhere = EmployeeTableMetaData._ID + "=" + rowId;		

			count = db.update(EmployeeTableMetaData.TABLE_NAME, values, finalWhere, null);

			break;
		default :
			throw new IllegalArgumentException("Unknown Uri" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		getContext().getContentResolver().notifyChange(EmployeeTableMetaData.SELECT_URI, null);

		return count;
	}
}

