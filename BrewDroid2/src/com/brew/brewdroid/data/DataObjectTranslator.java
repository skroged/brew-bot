package com.brew.brewdroid.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorTransport;

public class DataObjectTranslator {

	public static ContentValues getContentValuesFromSensor(Sensor sensor) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SENSORS_ID, sensor.getSensorId());
		values.put(DbOpenHelper.SENSORS_NAME, sensor.getSensorName().toString());
		values.put(DbOpenHelper.SENSORS_ADDRESS, sensor.getAddress());
		values.put(DbOpenHelper.SENSORS_VALUE, sensor.getValue());

		return values;
	}

	public static ContentValues getContentValuesFromSensorTransport(
			SensorTransport sensor) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SENSORS_ID, sensor.getSensorId());
		values.put(DbOpenHelper.SENSORS_VALUE, sensor.getValue());

		return values;
	}

	public static Sensor getSensorFromCursor(Cursor cursor)
			throws CursorIndexOutOfBoundsException {

		Sensor sensor = new Sensor();

		sensor.setSensorId(cursor.getInt(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_ID)));
		sensor.setSensorName(SENSOR_NAME.valueOf(cursor.getString(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_NAME))));
		sensor.setAddress(cursor.getString(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_ADDRESS)));
		sensor.setValue(cursor.getFloat(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_VALUE)));

		return sensor;
	}
}
