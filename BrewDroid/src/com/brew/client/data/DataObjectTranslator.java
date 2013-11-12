package com.brew.client.data;

import android.content.ContentValues;

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

	public static Sensor getSensorFromContentValues(ContentValues values) {

		Sensor sensor = new Sensor();

		sensor.setSensorId(values.getAsInteger(DbOpenHelper.SENSORS_ID));
		sensor.setSensorName(SENSOR_NAME.valueOf(values
				.getAsString(DbOpenHelper.SENSORS_NAME)));
		sensor.setAddress(values.getAsString(DbOpenHelper.SENSORS_ADDRESS));
		sensor.setValue(values.getAsFloat(DbOpenHelper.SENSORS_VALUE));

		return sensor;
	}
}
