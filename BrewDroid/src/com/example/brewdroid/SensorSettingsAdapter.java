package com.example.brewdroid;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.brew.lib.model.Sensor;
import com.example.brewdroid.SensorSettingsView.SensorSettingsViewListener;

public class SensorSettingsAdapter extends ArrayAdapter<Sensor> {

	private SensorSettingsAdapterListener sensorSettingsAdapterListener;

	public SensorSettingsAdapter(Context context, List<Sensor> objects,
			SensorSettingsAdapterListener sensorSettingsAdapterListener) {
		super(context, 0, 0, objects);

		this.sensorSettingsAdapterListener = sensorSettingsAdapterListener;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SensorSettingsView ssv;
		Sensor sensor = getItem(position);

		if (convertView == null) {
			ssv = new SensorSettingsView(getContext());
			ssv.setSensorSettingsViewListener(sensorSettingsViewListener);
		} else {
			ssv = (SensorSettingsView) convertView;
		}

		ssv.setSensor(sensor);

		return ssv;
	}

	private SensorSettingsViewListener sensorSettingsViewListener = new SensorSettingsViewListener() {

		@Override
		public void onChanged(SensorSettingsView sender) {
			sensorSettingsAdapterListener.onItemChanged(sender);

		}

	};

	public interface SensorSettingsAdapterListener {
		public void onItemChanged(SensorSettingsView sender);
	}
}
