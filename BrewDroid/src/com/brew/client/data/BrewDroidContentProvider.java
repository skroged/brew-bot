package com.brew.client.data;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorTransport;

public class BrewDroidContentProvider extends ContentProvider {

	private static DbOpenHelper mDbOpenHelper;

	public static final String AUTHORITY = "com.brew.client.data.BrewDroidContentProvider";

	public static final Uri SENSORS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DbOpenHelper.SENSORS_TABLE_NAME);

	public static final int SENSORS = 1;
	public static final int SENSOR = 2;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {

		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(AUTHORITY, DbOpenHelper.SENSORS_TABLE_NAME, SENSORS);

		matcher.addURI(AUTHORITY, DbOpenHelper.SENSORS_TABLE_NAME + "/#",
				SENSOR);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {

		int count = 0;

		switch (sUriMatcher.match(uri)) {

		case SENSORS:
			count = mDbOpenHelper.insertSensors(values);
			break;
		}

		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		switch (sUriMatcher.match(uri)) {

		case SENSORS:
			mDbOpenHelper.insertSensor(values);
			break;
		}

		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (sUriMatcher.match(uri)) {

		case SENSORS:
			return mDbOpenHelper.querySensors(null, null);

		case SENSOR:
			selection = "WHERE " + DbOpenHelper.SENSORS_ID + " = ?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			return mDbOpenHelper.querySensors(selection, selectionArgs);
		}

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int count = 0;

		switch (sUriMatcher.match(uri)) {
		case SENSORS:
			count = mDbOpenHelper
					.updateSensor(values, selection, selectionArgs);
			break;

		case SENSOR:
			count = mDbOpenHelper
					.updateSensor(values, selection, selectionArgs);

			break;
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;

	}

	public static void insertSensors(Context context, List<Sensor> sensors,
			BulkInsertListener insertListener) {

		ContentValues[] values = new ContentValues[sensors.size()];

		for (int i = 0; i < sensors.size(); i++) {
			Sensor sensor = sensors.get(i);
			ContentValues cv = DataObjectTranslator
					.getContentValuesFromSensor(sensor);
			values[i] = cv;
		}

		new BulkInsertTask(insertListener, context, SENSORS_URI, values)
				.execute();
	}

	public static void updateSensor(UpdateListener updateListener,
			Context context, SensorTransport sensorTransport) {

		ContentValues values = DataObjectTranslator
				.getContentValuesFromSensorTransport(sensorTransport);

		String where = "WHERE " + DbOpenHelper.SENSORS_ID + " = ?";

		String[] whereClause = { sensorTransport.getSensorId() + "" };

		Uri uri = SENSORS_URI.buildUpon()
				.appendEncodedPath(sensorTransport.getSensorId() + "").build();

		new UpdateTask(updateListener, context, uri, values, whereClause, where);

	}

	public static void querySensors(QueryListener queryListener, Context context) {

		new QueryTask(queryListener, context, SENSORS_URI, null, null, null)
				.execute();
	}
	
	public static void querySensor(QueryListener queryListener, Context context, int sensorId) {

		Uri uri = SENSORS_URI.buildUpon().appendEncodedPath(sensorId + "").build();
		
		new QueryTask(queryListener, context, uri, null, null, null)
				.execute();
	}

	public static void registerSensorContentObserver(Context context,
			ContentObserver observer, int sensorId) {

		Uri uri = SENSORS_URI.buildUpon().appendEncodedPath(sensorId + "")
				.build();

		context.getContentResolver().registerContentObserver(uri, false,
				observer);

	}

	public static void unregisterContentObserver(Context context,
			ContentObserver observer) {
		context.getContentResolver().unregisterContentObserver(observer);
	}

	private static class InsertTask extends AsyncTask<Void, Void, Uri> {

		private InsertListener mListener;
		private Uri mUri;
		private ContentValues mValues;
		private Context mContext;

		public InsertTask(InsertListener listener, Context context, Uri uri,
				ContentValues values) {
			mListener = listener;
			mContext = context;
			mValues = values;
			mUri = uri;
		}

		@Override
		protected Uri doInBackground(Void... params) {

			return mContext.getContentResolver().insert(mUri, mValues);
		}

		@Override
		protected void onPostExecute(Uri uri) {
			mListener.onComplete(uri);
			super.onPostExecute(uri);
		}

	};

	private static class BulkInsertTask extends AsyncTask<Void, Void, Integer> {

		private BulkInsertListener mListener;
		private Uri mUri;
		private ContentValues[] mValues;
		private Context mContext;

		public BulkInsertTask(BulkInsertListener listener, Context context,
				Uri uri, ContentValues[] values) {
			mListener = listener;
			mContext = context;
			mValues = values;
			mUri = uri;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			return mContext.getContentResolver().bulkInsert(mUri, mValues);
		}

		@Override
		protected void onPostExecute(Integer count) {
			mListener.onComplete(count);
			super.onPostExecute(count);
		}

	};

	private static class UpdateTask extends AsyncTask<Void, Void, Integer> {

		private UpdateListener mListener;
		private Uri mUri;
		private ContentValues mValues;
		private String[] mSelectionArgs;
		private String mWhere;
		private Context mContext;

		public UpdateTask(UpdateListener updateListener, Context context,
				Uri uri, ContentValues values, String[] selectionArgs,
				String where) {
			mListener = updateListener;
			mContext = context;
			mValues = values;
			mSelectionArgs = selectionArgs;
			mWhere = where;
			mUri = uri;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			return mContext.getContentResolver().update(mUri, mValues, mWhere,
					mSelectionArgs);
		}

		@Override
		protected void onPostExecute(Integer count) {
			if (mListener != null) {
				mListener.onComplete(count);
			}
			super.onPostExecute(count);
		}

	};

	private static class QueryTask extends AsyncTask<Void, Void, Cursor> {

		private QueryListener mListener;
		private Uri mUri;
		private String[] mSelectionArgs;
		private String mSelection;
		private String mSortOrder;
		private Context mContext;

		public QueryTask(QueryListener cursorListener, Context context,
				Uri uri, String[] selectionArgs, String selection,
				String sortOrder) {
			mListener = cursorListener;
			mContext = context;
			mSelectionArgs = selectionArgs;
			mSelection = selection;
			mSortOrder = sortOrder;
			mUri = uri;
		}

		@Override
		protected Cursor doInBackground(Void... params) {

			return mContext.getContentResolver().query(mUri, null, mSelection,
					mSelectionArgs, mSortOrder);
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			mListener.onComplete(cursor);
			super.onPostExecute(cursor);
		}

	};

	public static interface UpdateListener {
		public void onComplete(int count);
	}

	public static interface QueryListener {
		public void onComplete(Cursor cursor);
	}

	public static interface InsertListener {
		public void onComplete(Uri uri);
	}

	public static interface BulkInsertListener {
		public void onComplete(int count);
	}

}
