package com.brew.brewdroid.data;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.brew.lib.model.DeviceAddress;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorSettingsTransport;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.Switch;
import com.brew.lib.model.SwitchTransport;
import com.brew.lib.model.User;
import com.brew.lib.model.UserChannelPermission;

public class BrewDroidContentProvider extends ContentProvider {

	private static DbOpenHelper mDbOpenHelper;

	public static final String AUTHORITY = "com.brew.brewdroid.data.BrewDroidContentProvider";

	public static final Uri SENSORS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DbOpenHelper.SENSORS_TABLE_NAME);

	public static final Uri SWITCHES_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DbOpenHelper.SWITCHES_TABLE_NAME);

	public static final Uri DEVICE_ADDRESSES_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + DbOpenHelper.DEVICE_ADDRESSES_TABLE_NAME);

	public static final Uri USERS_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + DbOpenHelper.USERS_TABLE_NAME);

	public static final Uri PERMISSIONS_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + DbOpenHelper.PERMISSIONS_TABLE_NAME);

	public static final int SENSORS = 1;
	public static final int SENSOR = 2;

	public static final int SWITCHES = 3;
	public static final int SWITCH = 4;

	public static final int DEVICE_ADDRESSES = 5;

	public static final int USERS = 6;

	public static final int PERMISSIONS = 7;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {

		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(AUTHORITY, DbOpenHelper.SENSORS_TABLE_NAME, SENSORS);

		matcher.addURI(AUTHORITY, DbOpenHelper.SENSORS_TABLE_NAME + "/#",
				SENSOR);

		matcher.addURI(AUTHORITY, DbOpenHelper.SWITCHES_TABLE_NAME, SWITCHES);

		matcher.addURI(AUTHORITY, DbOpenHelper.SWITCHES_TABLE_NAME + "/#",
				SWITCH);

		matcher.addURI(AUTHORITY, DbOpenHelper.DEVICE_ADDRESSES_TABLE_NAME,
				DEVICE_ADDRESSES);

		matcher.addURI(AUTHORITY, DbOpenHelper.USERS_TABLE_NAME, USERS);

		matcher.addURI(AUTHORITY, DbOpenHelper.PERMISSIONS_TABLE_NAME,
				PERMISSIONS);

		return matcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int count = 0;

		switch (sUriMatcher.match(uri)) {

		case DEVICE_ADDRESSES:
			count = mDbOpenHelper.deleteAllDeviceAddresses();
			break;
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;

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
		case SWITCHES:
			count = mDbOpenHelper.insertSwitches(values);
			break;
		case DEVICE_ADDRESSES:
			count = mDbOpenHelper.insertDeviceAddresses(values);
			break;
		case USERS:
			count = mDbOpenHelper.insertUsers(values);
			break;
		case PERMISSIONS:
			count = mDbOpenHelper.insertPermissions(values);
			break;
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		switch (sUriMatcher.match(uri)) {

		case SENSORS:
			mDbOpenHelper.insertSensor(values);
			break;
		case SWITCHES:
			mDbOpenHelper.insertSwitch(values);
			break;
		case DEVICE_ADDRESSES:
			mDbOpenHelper.insertDeviceAddress(values);
			break;
		}

		return null;
	}

	@Override
	public boolean onCreate() {

		mDbOpenHelper = new DbOpenHelper(getContext());

		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (sUriMatcher.match(uri)) {

		case SENSORS:
			return mDbOpenHelper.querySensors(null, null);

		case SENSOR:
			selection = DbOpenHelper.SENSORS_ID + " = ?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			return mDbOpenHelper.querySensors(selection, selectionArgs);

		case SWITCHES:
			return mDbOpenHelper.querySwitches(null, null);

		case SWITCH:
			selection = DbOpenHelper.SWITCHES_ID + " = ?";
			selectionArgs = new String[] { uri.getLastPathSegment() };
			return mDbOpenHelper.querySwitches(selection, selectionArgs);

		case DEVICE_ADDRESSES:
			return mDbOpenHelper.queryDeviceAddresses(null, null);

		case USERS:
			return mDbOpenHelper.queryUsers(null, null);

		case PERMISSIONS:
			return mDbOpenHelper.queryPermissions(selection, selectionArgs);

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

			getContext().getContentResolver().notifyChange(SENSORS_URI, null);

			break;
		case SWITCHES:

			count = mDbOpenHelper
					.updateSwitch(values, selection, selectionArgs);
			break;

		case SWITCH:

			count = mDbOpenHelper
					.updateSwitch(values, selection, selectionArgs);

			getContext().getContentResolver().notifyChange(SWITCHES_URI, null);

			break;
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;

	}

	public static void insertPermissions(Context context,
			List<UserChannelPermission> permissions,
			BulkInsertListener insertListener) {

		ContentValues[] values = new ContentValues[permissions.size()];

		for (int i = 0; i < permissions.size(); i++) {
			UserChannelPermission permission = permissions.get(i);
			ContentValues cv = DataObjectTranslator
					.getContentValuesFromPermission(permission);
			values[i] = cv;
		}

		new BulkInsertTask(insertListener, context, PERMISSIONS_URI, values)
				.execute();
	}

	public static void queryPermissions(QueryListener queryListener,
			Context context) {

		new QueryTask(queryListener, context, PERMISSIONS_URI, null, null, null)
				.execute();
	}

	public static void queryPermissionsForUserId(QueryListener queryListener,
			Context context, int userId) {

		String[] selectionArgs = { userId + "" };
		String selection = DbOpenHelper.PERMISSIONS_USER_ID + " = ?";
		new QueryTask(queryListener, context, PERMISSIONS_URI, selectionArgs,
				selection, null).execute();
	}

	public static void registerPermissionsContentObserver(Context context,
			ContentObserver observer) {

		context.getContentResolver().registerContentObserver(USERS_URI, false,
				observer);

	}

	public static void insertUsers(Context context, List<User> users,
			BulkInsertListener insertListener) {

		ContentValues[] values = new ContentValues[users.size()];

		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			ContentValues cv = DataObjectTranslator
					.getContentValuesFromUser(user);
			values[i] = cv;
		}

		new BulkInsertTask(insertListener, context, USERS_URI, values)
				.execute();
	}

	public static void queryUsers(QueryListener queryListener, Context context) {

		new QueryTask(queryListener, context, USERS_URI, null, null, null)
				.execute();
	}

	public static void registerUsersContentObserver(Context context,
			ContentObserver observer) {

		context.getContentResolver().registerContentObserver(USERS_URI, false,
				observer);

	}

	public static void deleteAllDeviceAddresses(Context context,
			DeleteListener deleteListener) {

		new DeleteTask(deleteListener, context, DEVICE_ADDRESSES_URI, null,
				null).execute();

	}

	public static void insertDeviceAddresses(Context context,
			List<DeviceAddress> deviceAddresses,
			BulkInsertListener insertListener) {

		ContentValues[] values = new ContentValues[deviceAddresses.size()];

		for (int i = 0; i < deviceAddresses.size(); i++) {
			DeviceAddress deviceAddress = deviceAddresses.get(i);
			ContentValues cv = DataObjectTranslator
					.getContentValuesFromDeviceAddress(deviceAddress);
			values[i] = cv;
		}

		new BulkInsertTask(insertListener, context, DEVICE_ADDRESSES_URI,
				values).execute();
	}

	public static void queryDeviceAddresses(QueryListener queryListener,
			Context context) {

		new QueryTask(queryListener, context, DEVICE_ADDRESSES_URI, null, null,
				null).execute();
	}

	public static void registerDeviceAddressesContentObserver(Context context,
			ContentObserver observer) {

		context.getContentResolver().registerContentObserver(
				DEVICE_ADDRESSES_URI, false, observer);

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

		String where = DbOpenHelper.SENSORS_ID + " = ?";

		String[] whereClause = { sensorTransport.getSensorId() + "" };

		Uri uri = SENSORS_URI.buildUpon()
				.appendEncodedPath(sensorTransport.getSensorId() + "").build();

		new UpdateTask(updateListener, context, uri, values, whereClause, where)
				.execute();

	}

	public static void updateSensorSetting(UpdateListener updateListener,
			Context context, SensorSettingsTransport sensorSettingsTransport) {

		ContentValues values = DataObjectTranslator
				.getContentVluesFromSensorSettinsTransport(sensorSettingsTransport);

		String where = DbOpenHelper.SENSORS_ID + " = ?";

		String[] whereClause = { sensorSettingsTransport.getSensorId() + "" };

		Uri uri = SENSORS_URI.buildUpon()
				.appendEncodedPath(sensorSettingsTransport.getSensorId() + "")
				.build();

		new UpdateTask(updateListener, context, uri, values, whereClause, where)
				.execute();

	}

	public static void updateSensors(final UpdateListener updateListener,
			Context context, final List<SensorTransport> sensorTransports) {

		UpdateListener listener = new UpdateListener() {

			int listenerCount = 0;
			int updateCount = 0;

			@Override
			public void onComplete(int count) {
				updateCount += count;
				listenerCount++;

				if (updateListener != null
						&& listenerCount == sensorTransports.size()) {
					updateListener.onComplete(updateCount);
				}
			}

		};

		for (SensorTransport st : sensorTransports) {
			updateSensor(listener, context, st);
		}

	}

	public static void updateSensorSettings(
			final UpdateListener updateListener, Context context,
			final List<SensorSettingsTransport> sensorSettingsTransports) {

		UpdateListener listener = new UpdateListener() {

			int listenerCount = 0;
			int updateCount = 0;

			@Override
			public void onComplete(int count) {
				updateCount += count;
				listenerCount++;

				if (updateListener != null
						&& listenerCount == sensorSettingsTransports.size()) {
					updateListener.onComplete(updateCount);
				}
			}

		};

		for (SensorSettingsTransport st : sensorSettingsTransports) {
			updateSensorSetting(listener, context, st);
		}

	}

	public static void querySensors(QueryListener queryListener, Context context) {

		new QueryTask(queryListener, context, SENSORS_URI, null, null, null)
				.execute();
	}

	public static void querySensor(QueryListener queryListener,
			Context context, String sensorId) {

		Uri uri = SENSORS_URI.buildUpon().appendEncodedPath(sensorId).build();

		new QueryTask(queryListener, context, uri, null, null, null).execute();
	}

	public static void querySensor(QueryListener queryListener,
			Context context, Uri uri) {

		new QueryTask(queryListener, context, uri, null, null, null).execute();
	}

	public static void registerSensorContentObserver(Context context,
			ContentObserver observer, int sensorId) {

		Uri uri = SENSORS_URI.buildUpon().appendEncodedPath(sensorId + "")
				.build();

		context.getContentResolver().registerContentObserver(uri, false,
				observer);

	}

	public static void registerSensorsContentObserver(Context context,
			ContentObserver observer) {

		context.getContentResolver().registerContentObserver(SENSORS_URI,
				false, observer);

	}

	// ////////////////

	public static void insertSwitches(Context context, List<Switch> switches,
			BulkInsertListener insertListener) {

		ContentValues[] values = new ContentValues[switches.size()];

		for (int i = 0; i < switches.size(); i++) {
			Switch switchh = switches.get(i);
			ContentValues cv = DataObjectTranslator
					.getContentValuesFromSwitch(switchh);
			values[i] = cv;
		}

		new BulkInsertTask(insertListener, context, SWITCHES_URI, values)
				.execute();
	}

	public static void updateSwitch(UpdateListener updateListener,
			Context context, SwitchTransport switchTransport) {

		ContentValues values = DataObjectTranslator
				.getContentValuesFromSwitchTransport(switchTransport);

		String where = DbOpenHelper.SWITCHES_ID + " = ?";

		String[] whereClause = { switchTransport.getSwitchId() + "" };

		Uri uri = SWITCHES_URI.buildUpon()
				.appendEncodedPath(switchTransport.getSwitchId() + "").build();

		new UpdateTask(updateListener, context, uri, values, whereClause, where)
				.execute();

	}

	public static void updateSwitches(final UpdateListener updateListener,
			Context context, final List<SwitchTransport> switchhTransports) {

		UpdateListener listener = new UpdateListener() {

			int listenerCount = 0;
			int updateCount = 0;

			@Override
			public void onComplete(int count) {
				updateCount += count;
				listenerCount++;

				if (updateListener != null
						&& listenerCount == switchhTransports.size()) {
					updateListener.onComplete(updateCount);
				}
			}

		};

		for (SwitchTransport st : switchhTransports) {
			updateSwitch(listener, context, st);
		}

	}

	public static void querySwitches(QueryListener queryListener,
			Context context) {

		new QueryTask(queryListener, context, SWITCHES_URI, null, null, null)
				.execute();
	}

	public static void querySwitch(QueryListener queryListener,
			Context context, String switchhId) {

		Uri uri = SWITCHES_URI.buildUpon().appendEncodedPath(switchhId).build();

		new QueryTask(queryListener, context, uri, null, null, null).execute();
	}

	public static void querySwitch(QueryListener queryListener,
			Context context, Uri uri) {

		new QueryTask(queryListener, context, uri, null, null, null).execute();
	}

	public static void registerSwitchContentObserver(Context context,
			ContentObserver observer, int switchhId) {

		Uri uri = SWITCHES_URI.buildUpon().appendEncodedPath(switchhId + "")
				.build();

		context.getContentResolver().registerContentObserver(uri, false,
				observer);

	}

	public static void registerSwitchesContentObserver(Context context,
			ContentObserver observer) {

		context.getContentResolver().registerContentObserver(SWITCHES_URI,
				false, observer);

	}

	// ////////////////////

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
			if (mListener != null) {
				mListener.onComplete(uri);
			}
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
			if (mListener != null) {
				mListener.onComplete(count);
			}
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

	private static class DeleteTask extends AsyncTask<Void, Void, Integer> {

		private DeleteListener mListener;
		private Uri mUri;
		private String[] mSelectionArgs;
		private String mWhere;
		private Context mContext;

		public DeleteTask(DeleteListener deleteListener, Context context,
				Uri uri, String[] selectionArgs, String where) {
			mListener = deleteListener;
			mContext = context;
			mSelectionArgs = selectionArgs;
			mWhere = where;
			mUri = uri;
		}

		@Override
		protected Integer doInBackground(Void... params) {

			return mContext.getContentResolver().delete(mUri, mWhere,
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

			if (mContext == null) {
				return null;
			}
			return mContext.getContentResolver().query(mUri, null, mSelection,
					mSelectionArgs, mSortOrder);
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			if (mListener != null && cursor != null) {
				mListener.onComplete(cursor);
			}
			super.onPostExecute(cursor);
		}

	};

	public static interface DeleteListener {
		public void onComplete(int count);
	}

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
