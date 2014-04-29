package com.brew.brewdroid;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.brew.brewdroid.SensorSettingsView.SensorSettingsViewListener;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.Sensor;

public class SensorSettingsCursorAdapter extends CursorAdapter {

	private SensorSettingsViewListener mSensorSettingsViewListener;

	public SensorSettingsCursorAdapter(Context context, Cursor c,
			SensorSettingsViewListener sensorSettingsViewListener) {
		super(context, c, 0);
		mSensorSettingsViewListener = sensorSettingsViewListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		SensorSettingsView ssv = (SensorSettingsView) view;

		Sensor sensor = DataObjectTranslator.getSensorFromCursor(cursor);

		ssv.setSensor(sensor);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		SensorSettingsView ssv = new SensorSettingsView(context);

		ssv.setSensorSettingsViewListener(mSensorSettingsViewListener);

		return ssv;
	}

}
