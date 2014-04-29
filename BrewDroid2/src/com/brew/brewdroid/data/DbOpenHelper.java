package com.brew.brewdroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 10;
	private static final String DATABASE_NAME = "BrewBotDatabase";

	public static final String SENSORS_TABLE_NAME = "sensors";
	public static final String SENSORS_ID = "idsensors";
	public static final String SENSORS_NAME = "sensorName";
	public static final String SENSORS_ADDRESS = "sensorAddress";
	public static final String SENSORS_VALUE = "sensorValue";
	public static final String SENSORS_VALUE_CALIBRATED = "sensorValueCalibrated";
	public static final String SENSORS_CALIBRATION_INPUT_LOW = "sensorCalibrationInputLow";
	public static final String SENSORS_CALIBRATION_INPUT_HIGH = "sensorCalibrationInputHigh";
	public static final String SENSORS_CALIBRATION_OUTPUT_LOW = "sensorCalibrationOutputLow";
	public static final String SENSORS_CALIBRATION_OUTPUT_HIGH = "sensorCalibrationOutputHigh";

	public static final String SWITCHES_TABLE_NAME = "switches";
	public static final String SWITCHES_ID = "idswitches";
	public static final String SWITCHES_NAME = "switchName";
	public static final String SWITCHES_ADDRESS = "switchAddress";
	public static final String SWITCHES_VALUE = "switchValue";

	public static final String DEVICE_ADDRESSES_TABLE_NAME = "deviceAddresses";
	public static final String DEVICE_ADDRESSES_ID = "idDeviceAddresses";
	public static final String DEVICE_ADDRESSES_ADDRESS = "deviceAddress";
	public static final String DEVICE_ADDRESSES_TYPE = "deviceType";

	public static final String USERS_TABLE_NAME = "users";
	public static final String USERS_ID = "userId";
	public static final String USERS_USERNAME = "userUsername";
	public static final String USERS_NAME = "userName";

	public static final String PERMISSIONS_TABLE_NAME = "permissions";
	public static final String PERMISSIONS_ID = "permissionId";
	public static final String PERMISSIONS_USER_ID = "permissionUserId";
	public static final String PERMISSIONS_CHANNEL = "permissionChannel";
	public static final String PERMISSSION_PERMISSION = "permissionPermission";

	public static final String DEVICE_ADDRESSES_CREATE_TABLE = "CREATE TABLE "
			+ DEVICE_ADDRESSES_TABLE_NAME + " (" + DEVICE_ADDRESSES_ID
			+ " INTEGER PRIMARY KEY, " + DEVICE_ADDRESSES_ADDRESS + " TEXT, "
			+ DEVICE_ADDRESSES_TYPE + " TEXT);";

	public static final String SENSORS_CREATE_TABLE = "CREATE TABLE "
			+ SENSORS_TABLE_NAME + " (" + SENSORS_ID + " INTEGER PRIMARY KEY, "
			+ SENSORS_NAME + " TEXT, " + SENSORS_ADDRESS + " TEXT, "
			+ SENSORS_VALUE + " REAL," + SENSORS_VALUE_CALIBRATED + " REAL, "
			+ SENSORS_CALIBRATION_INPUT_LOW + " REAL,"
			+ SENSORS_CALIBRATION_INPUT_HIGH + " REAL,"
			+ SENSORS_CALIBRATION_OUTPUT_LOW + " REAL,"
			+ SENSORS_CALIBRATION_OUTPUT_HIGH + " REAL);";

	public static final String SWITCHES_CREATE_TABLE = "CREATE TABLE "
			+ SWITCHES_TABLE_NAME + " (" + SWITCHES_ID
			+ " INTEGER PRIMARY KEY, " + SWITCHES_NAME + " TEXT, "
			+ SWITCHES_ADDRESS + " TEXT, " + SWITCHES_VALUE + " REAL);";

	public static final String USERS_CREATE_TABLE = "CREATE TABLE "
			+ USERS_TABLE_NAME + " (" + USERS_ID + " INTEGER PRIMARY KEY, "
			+ USERS_USERNAME + " TEXT, " + USERS_NAME + " TEXT);";

	public static final String PERMISSIONS_CREATE_TABLE = "CREATE TABLE "
			+ PERMISSIONS_TABLE_NAME + "(" + PERMISSIONS_ID
			+ " INTEGER, " + PERMISSIONS_USER_ID + " INTEGER, "
			+ PERMISSIONS_CHANNEL + " TEXT, " + PERMISSSION_PERMISSION
			+ " TEXT, PRIMARY KEY (" + PERMISSIONS_USER_ID + ", " + PERMISSIONS_CHANNEL + "))";

	// public static final String SENSOR_CALIBRATIONS_CREATE_TABLE =
	// "CREATE TABLE "
	// + SENSOR_CALIBRATIONS_TABLE_NAME
	// + " ("
	// + SENSOR_CALIBRATIONS_ID
	// + " INTEGER PRIMARY KEY, "
	// + SENSOR_CALIBRATIONS_SENSOR_ID
	// + " INTEGER, "
	// + SENSOR_CALIBRATIONS_INPUT_LOW
	// + " REAL, "
	// + SENSOR_CALIBRATIONS_INPUT_HIGH
	// + " REAL, "
	// + SENSOR_CALIBRATIONS_OUTPUT_LOW
	// + " REAL, "
	// + SENSOR_CALIBRATIONS_OUTPUT_HIGH + " REAL);";

	public DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(SENSORS_CREATE_TABLE);
		db.execSQL(SWITCHES_CREATE_TABLE);
		db.execSQL(DEVICE_ADDRESSES_CREATE_TABLE);
		db.execSQL(USERS_CREATE_TABLE);
		db.execSQL(PERMISSIONS_CREATE_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + SENSORS_TABLE_NAME + ";");
		db.execSQL(SENSORS_CREATE_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + SWITCHES_TABLE_NAME + ";");
		db.execSQL(SWITCHES_CREATE_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + DEVICE_ADDRESSES_TABLE_NAME + ";");
		db.execSQL(DEVICE_ADDRESSES_CREATE_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME + ";");
		db.execSQL(USERS_CREATE_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + PERMISSIONS_TABLE_NAME + ";");
		db.execSQL(PERMISSIONS_CREATE_TABLE);

	}

	public int insertPermissions(ContentValues[] values) {
		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {

			for (ContentValues cv : values) {
				db.insertWithOnConflict(PERMISSIONS_TABLE_NAME, null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();

			return values.length;

		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public Cursor queryPermissions(String selection, String[] selectionArgs) {

		SQLiteDatabase db = getWritableDatabase();

		String[] cols = null;

		return db.query(PERMISSIONS_TABLE_NAME, cols, selection, selectionArgs,
				null, null, null);
	}

	public int insertUsers(ContentValues[] values) {

		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {

			for (ContentValues cv : values) {
				db.insertWithOnConflict(USERS_TABLE_NAME, null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();

			return values.length;

		} finally {
			db.endTransaction();
			db.close();
		}

	}

	public Cursor queryUsers(String selection, String[] selectionArgs) {

		SQLiteDatabase db = getWritableDatabase();

		String[] cols = { USERS_ID + " AS " + BaseColumns._ID, "*" };

		return db.query(USERS_TABLE_NAME, cols, selection, selectionArgs, null,
				null, null);
	}

	public int updateSensor(ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase db = getWritableDatabase();

		int count = db.update(SENSORS_TABLE_NAME, values, where, whereArgs);

		db.close();

		return count;

	}

	public long insertSensor(ContentValues values) {

		SQLiteDatabase db = getWritableDatabase();

		long id = db.insertWithOnConflict(SENSORS_TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

		db.close();

		return id;

	}

	public int insertSensors(ContentValues[] values) {

		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {

			for (ContentValues cv : values) {
				db.insertWithOnConflict(SENSORS_TABLE_NAME, null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();

			return values.length;

		} finally {
			db.endTransaction();
			db.close();
		}

	}

	public Cursor querySensors(String selection, String[] selectionArgs) {

		SQLiteDatabase db = getWritableDatabase();

		String[] cols = { SENSORS_ID + " AS " + BaseColumns._ID, "*" };

		return db.query(SENSORS_TABLE_NAME, cols, selection, selectionArgs,
				null, null, null);
	}

	// ////////////////

	public int updateSwitch(ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase db = getWritableDatabase();

		int count = db.update(SWITCHES_TABLE_NAME, values, where, whereArgs);

		db.close();

		return count;

	}

	public long insertSwitch(ContentValues values) {

		SQLiteDatabase db = getWritableDatabase();

		long id = db.insertWithOnConflict(SWITCHES_TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

		db.close();

		return id;

	}

	public int insertSwitches(ContentValues[] values) {

		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {

			for (ContentValues cv : values) {
				db.insertWithOnConflict(SWITCHES_TABLE_NAME, null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();

			return values.length;

		} finally {
			db.endTransaction();
			db.close();
		}

	}

	public Cursor querySwitches(String selection, String[] selectionArgs) {

		SQLiteDatabase db = getWritableDatabase();

		return db.query(SWITCHES_TABLE_NAME, null, selection, selectionArgs,
				null, null, null);
	}

	public int deleteAllDeviceAddresses() {
		SQLiteDatabase db = getWritableDatabase();
		int deleteCount = db.delete(DEVICE_ADDRESSES_TABLE_NAME, null, null);
		db.close();
		return deleteCount;
	}

	public long insertDeviceAddress(ContentValues values) {

		SQLiteDatabase db = getWritableDatabase();

		long id = db.insertWithOnConflict(DEVICE_ADDRESSES_TABLE_NAME, null,
				values, SQLiteDatabase.CONFLICT_REPLACE);

		db.close();

		return id;

	}

	public int insertDeviceAddresses(ContentValues[] values) {

		SQLiteDatabase db = getWritableDatabase();

		db.beginTransaction();

		try {

			for (ContentValues cv : values) {
				db.insertWithOnConflict(DEVICE_ADDRESSES_TABLE_NAME, null, cv,
						SQLiteDatabase.CONFLICT_REPLACE);
			}

			db.setTransactionSuccessful();

			return values.length;

		} finally {
			db.endTransaction();
			db.close();
		}

	}

	public Cursor queryDeviceAddresses(String selection, String[] selectionArgs) {

		SQLiteDatabase db = getWritableDatabase();

		return db.query(DEVICE_ADDRESSES_TABLE_NAME, null, selection,
				selectionArgs, null, null, null);
	}

}
