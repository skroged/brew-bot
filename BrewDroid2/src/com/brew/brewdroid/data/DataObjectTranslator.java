package com.brew.brewdroid.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;

import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorCalibration;
import com.brew.lib.model.SensorSettingsTransport;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.Switch;
import com.brew.lib.model.SwitchTransport;

public class DataObjectTranslator {

	public static ContentValues getContentValuesFromSensor(Sensor sensor) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SENSORS_ID, sensor.getSensorId());
		values.put(DbOpenHelper.SENSORS_NAME, sensor.getSensorName().toString());
		values.put(DbOpenHelper.SENSORS_ADDRESS, sensor.getAddress());
		values.put(DbOpenHelper.SENSORS_VALUE, sensor.getCalibratedValue());

		if (sensor.getCalibration() != null) {
			values.put(DbOpenHelper.SENSORS_CALIBRATION_INPUT_LOW, sensor
					.getCalibration().getInputLow());
			values.put(DbOpenHelper.SENSORS_CALIBRATION_INPUT_HIGH, sensor
					.getCalibration().getInputHigh());
			values.put(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_LOW, sensor
					.getCalibration().getOutputLow());
			values.put(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_HIGH, sensor
					.getCalibration().getOutputHigh());
		}

		return values;
	}

	public static ContentValues getContentValuesFromSensorTransport(
			SensorTransport sensor) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SENSORS_ID, sensor.getSensorId());
		values.put(DbOpenHelper.SENSORS_VALUE, sensor.getValue());

		return values;
	}

	public static ContentValues getContentVluesFromSensorSettinsTransport(
			SensorSettingsTransport sensor) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SENSORS_ID, sensor.getSensorId());
		values.put(DbOpenHelper.SENSORS_VALUE, sensor.getCalibratedValue());
		values.put(DbOpenHelper.SENSORS_CALIBRATION_INPUT_LOW,
				sensor.getInputLow());
		values.put(DbOpenHelper.SENSORS_CALIBRATION_INPUT_HIGH,
				sensor.getInputHigh());
		values.put(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_LOW,
				sensor.getOutputLow());
		values.put(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_HIGH,
				sensor.getOutputHigh());

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

		SensorCalibration calibration = new SensorCalibration();
		
		calibration.setInputLow(cursor.getFloat(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_CALIBRATION_INPUT_LOW)));
		calibration.setInputHigh(cursor.getFloat(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_CALIBRATION_INPUT_HIGH)));
		calibration.setOutputLow(cursor.getFloat(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_LOW)));
		calibration.setOutputHigh(cursor.getFloat(cursor
				.getColumnIndex(DbOpenHelper.SENSORS_CALIBRATION_OUTPUT_HIGH)));

		sensor.setCalibration(calibration);

		return sensor;
	}

	// //////

	public static ContentValues getContentValuesFromSwitch(Switch switchh) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SWITCHES_ID, switchh.getId());
		values.put(DbOpenHelper.SWITCHES_NAME, switchh.getName().toString());
		values.put(DbOpenHelper.SWITCHES_ADDRESS, switchh.getAddress());
		values.put(DbOpenHelper.SWITCHES_VALUE, switchh.getValue() ? 1 : 0);

		return values;
	}

	public static ContentValues getContentValuesFromSwitchTransport(
			SwitchTransport switchh) {

		ContentValues values = new ContentValues();

		values.put(DbOpenHelper.SWITCHES_ID, switchh.getSwitchId());
		values.put(DbOpenHelper.SWITCHES_VALUE, (switchh.getSwitchValue() ? 1
				: 0));

		return values;
	}

	public static Switch getSwitchFromCursor(Cursor cursor)
			throws CursorIndexOutOfBoundsException {

		Switch switchh = new Switch();

		switchh.setId(cursor.getInt(cursor
				.getColumnIndex(DbOpenHelper.SWITCHES_ID)));
		switchh.setName(SWITCH_NAME.valueOf(cursor.getString(cursor
				.getColumnIndex(DbOpenHelper.SWITCHES_NAME))));
		switchh.setAddress(cursor.getInt(cursor
				.getColumnIndex(DbOpenHelper.SWITCHES_ADDRESS)));
		switchh.setValue(cursor.getInt(cursor
				.getColumnIndex(DbOpenHelper.SWITCHES_VALUE)) == 1);

		return switchh;
	}
}
