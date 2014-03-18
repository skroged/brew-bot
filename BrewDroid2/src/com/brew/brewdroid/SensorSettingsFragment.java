package com.brew.brewdroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.brew.brewdroid.SensorAddressEditDialog.SensorAddressEditDialogListener;
import com.brew.brewdroid.SensorCalibrationEditDialog.SensorCalibrationEditDialogListener;
import com.brew.brewdroid.SensorSettingsView.SensorSettingsViewListener;
import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.DeviceAddress;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.Sensor;

public class SensorSettingsFragment extends Fragment {

	private SensorSettingsCursorAdapter mSensorSettingsCursorAdapter;
	private Handler handler;
	private ListView mSensorLv;
	private List<DeviceAddress> deviceAddresses;

	public static SensorSettingsFragment instantiate() {
		SensorSettingsFragment frag = new SensorSettingsFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		BrewDroidContentProvider.registerSensorsContentObserver(activity,
				mSensorsContentObserver);
		BrewDroidContentProvider.registerDeviceAddressesContentObserver(
				activity, mSensorsContentObserver);

		Intent intent = new Intent(BrewDroidService.ACTION_SUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.SENSOR_SETTINGS.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

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
		
		mSensorSettingsCursorAdapter.getCursor().close();
		mSensorSettingsCursorAdapter = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_sensor_settings, null);

		mSensorLv = (ListView) v.findViewById(R.id.sensorList);

		handler = new Handler();
		
		getCursor();

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
		public void onComplete(final Cursor cursor) {

			if (mSensorSettingsCursorAdapter == null) {

				mSensorSettingsCursorAdapter = new SensorSettingsCursorAdapter(
						getActivity(), cursor, mSensorSettingsViewListener);

				mSensorLv.setAdapter(mSensorSettingsCursorAdapter);

			}

			else {

				mSensorSettingsCursorAdapter.changeCursor(cursor);

			}

		}

	};
	private QueryListener deviceAddressesCursorListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			deviceAddresses = new ArrayList<DeviceAddress>();

			while (cursor.moveToNext()) {

				DeviceAddress deviceAddress = DataObjectTranslator
						.getDeviceAddressFromCursor(cursor);

				deviceAddresses.add(deviceAddress);
			}

			cursor.close();
		}

	};

	private void getCursor() {

		BrewDroidContentProvider.querySensors(sensorCursorListener,
				getActivity());

		BrewDroidContentProvider.queryDeviceAddresses(
				deviceAddressesCursorListener, getActivity());

	}

	private void sendSensorSettings(Sensor sensor) {

		Intent intent = new Intent(
				BrewDroidService.ACTION_UPDATE_SENSOR_SETTING);

		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_ID, sensor.getSensorId());
		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_CALIBRATION_INPUT_LOW,
				sensor.getCalibration().getInputLow());
		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_CALIBRATION_INPUT_HIGH,
				sensor.getCalibration().getInputHigh());
		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_CALIBRATION_OUTPUT_LOW,
				sensor.getCalibration().getOutputLow());
		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_CALIBRATION_OUTPUT_HIGH,
				sensor.getCalibration().getOutputHigh());
		intent.putExtra(BrewDroidService.BUNDLE_SENSOR_ADDRESS,
				sensor.getAddress());

		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

	}

	private SensorSettingsViewListener mSensorSettingsViewListener = new SensorSettingsViewListener() {

		@Override
		public void onEditHighClicked(SensorSettingsView sender) {

			final Sensor sensor = sender.getSensor();

			SensorCalibrationEditDialog dialogLow = new SensorCalibrationEditDialog(
					getActivity(), sensor.getCalibration().getInputHigh(),
					sensor.getCalibration().getOutputHigh(),
					new SensorCalibrationEditDialogListener() {

						@Override
						public void onSaved(float input, float output) {
							sensor.getCalibration().setInputHigh(input);
							sensor.getCalibration().setOutputHigh(output);

							sendSensorSettings(sensor);

						}

						@Override
						public float requestCapture() {
							return sensor.getValue();
						}

					});

			dialogLow.show();
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

							sendSensorSettings(sensor);

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

			final Sensor sensor = sender.getSensor();

			String[] addresses = new String[deviceAddresses.size()];

			for (int i = 0; i < addresses.length; i++) {
				addresses[i] = deviceAddresses.get(i).getAddress();
			}

			SensorAddressEditDialog dialogAddress = new SensorAddressEditDialog(
					getActivity(), addresses, sensor.getAddress(),
					new SensorAddressEditDialogListener() {

						@Override
						public void onSaved(String address) {
							sensor.setAddress(address);
							sendSensorSettings(sensor);
						}

					});

			dialogAddress.show();

		}

	};
}
