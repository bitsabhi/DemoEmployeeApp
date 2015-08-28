package com.sample.employeeslib;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

public class EmployeeProviderMetaData {
	public static final String AUTHORITY = "com.sample.provider.EmployeeProvider";

	public static final String DATABASE_NAME = "emp.db";
	public static final int DATABASE_VERSION = 1;
	public static final String EMPLOYEES_TABLE_NAME = "employees";

	private EmployeeProviderMetaData() {};

	// Employee Details

	public static final class EmployeeTableMetaData implements BaseColumns {
		public static final String TABLE_NAME = "employees";

		public static final String EMPLOYEES = "emp";

		public static final String INSERT = "insert";

		public static final String UPDATE = "update";

		public static final String DELETE = "delete";

		public static final String SELECT = "select";
		
		public static final String SEARCH = "search";
		
		public static final String SUGGEST = SearchManager.SUGGEST_URI_PATH_QUERY;

		// Uri definition:

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

		public static final Uri INSERT_URI = Uri.parse(CONTENT_URI + "/" + EMPLOYEES + "/" + INSERT);

		public static final Uri UPDATE_URI = Uri.parse(CONTENT_URI + "/" + EMPLOYEES + "/" + UPDATE);

		public static final Uri DELETE_URI = Uri.parse(CONTENT_URI + "/" + EMPLOYEES + "/" + DELETE);

		public static final Uri SELECT_URI = Uri.parse(CONTENT_URI + "/" + EMPLOYEES + "/" + SELECT);
		
		public static final Uri SEARCH_URI = Uri.parse(CONTENT_URI + "/" + EMPLOYEES + "/" + SEARCH);
		
		public static final Uri SUGGEST_URI = Uri.parse(CONTENT_URI + "/" + SUGGEST);
		
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		// Table Columns

		public static final String EMPLOYEE_NAME = "name";
		public static final String EMPLOYEE_AGE = "age";
		public static final String EMPLOYEE_DESIGNATION = "designation";
		public static final String EMPLOYEE_DEPARTMENT = "department";
		public static final String CREATED_DATE = "created";
		public static final String MODIFIED_DATE = "modified";
	}
}
