package com.brew.brewdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.brew.brewdroid.SensorCalibrationEditDialog.SensorCalibrationEditDialogListener;
import com.brew.brewdroid.SensorSettingsView.SensorSettingsViewListener;
import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.Sensor;

public class SensorSettingsFragment extends Fragment {

	private SensorSettingsCursorAdapter mSensorSettingsCursorAdapter;
	private Handler handler;
	private ListView mSensorLv;

	public static SensorSettingsFragment instantiate() {
		SensorSettingsFragment frag = new SensorSettingsFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		BrewDroidContentProvider.registerSensorsContentObserver(activity,
				mSensorsContentObserver);
		
		Intent intent = new Intent(BrewDroidService.ACTION_SUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.SENSOR_SETTINGS.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

		getCursor();

	}

	@Override
	public void onDetach() {
		super.onDetach();

		BrewDroidContentProvider.unregisterContentObserver(getActivity(),
				mSensorsContentObserver);
		
		Intent intent = new Intent(BrewDroidService.ACTION_UNSUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.SENSOR_SETTINGS.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_sensor_settings, null);

		mSensorLv = (ListView) v.findViewById(R.id.sensorList);

		handler = new Handler();

		return v;
	}

	private ContentObserver mSensorsContentObserver = new ContentObserver(
			handler) {

		@Override
		public void onChange(boolean selfChange) {
			getCursor();
			super.onChange(selfChange);
		}

	};

	private QueryListener sensorCursorListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (mSensorSettingsCursorAdapter == null) {

				mSensorSettingsCursorAdapter = new SensorSettingsCursorAdapter(
						getActivity(), cursor, mSensorSettingsViewListener);

				mSensorLv.setAdapter(mSensorSettingsCursorAdapter);
				
			}
			
			else{
				Cursor oldCursor = mSensorSettingsCursorAdapter.swapCursor(cursor);
				oldCursor.close();
			}

		}

	};

	private void getCursor() {

		BrewDroidContentProvider.querySensors(sensorCursorListener,
				getActivity());

	}

	private SensorSettingsViewListener mSensorSettingsViewListener = new SensorSettingsViewListener() {

		@Override
		public void onEditHighClicked(SensorSettingsView sender) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEditLowClicked(SensorSettingsView sender) {
			
			final Sensor sensor = sender.getSensor();
			
			SensorCalibrationEditDialog dialogLow = new SensorCalibrationEditDialog(
					getActivity(), sensor.getCalibration().getInputLow(),
					sensor.getCalibration().getOutputLow(),
					new SensorCalibrationEditDialogListener() {

						@Override
						public void onSaved(float input, float output) {
							sensor.getCalibration().setInputLow(input);
							sensor.getCalibration().setOutputLow(output);							
						}

						@Override
						public float requestCapture() {
							return sensor.getValue();
						}

					});

			dialogLow.show();
		}

		@Override
		public void onEditAddressClicked(SensorSettingsView sender) {
			// TODO Auto-generated method stub

		}

	};
}
