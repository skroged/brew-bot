package com.brew.brewdroid;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.Sensor;

public class SensorSettingsView extends RelativeLayout {

	private TextView nameText;
	private TextView rawValueText;
	private TextView calibratedValueText;
	private TextView inLowText;
	private TextView outLowText;
	private TextView inHighText;
	private TextView outHighText;
	private TextView addressText;
	private Sensor sensor;
	private List<String> oneWireAddresses;
	private SensorSettingsViewListener sensorSettingsViewListener;

	public SensorSettingsView(Context context) {
		super(context);
		setStuff();
	}

	public SensorSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStuff();
	}

	public SensorSettingsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setStuff();
	}

	private void setStuff() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_sensor_settings, this);

		nameText = (TextView) findViewById(R.id.nameText);
		rawValueText = (TextView) findViewById(R.id.rawValueText);
		calibratedValueText = (TextView) findViewById(R.id.calibratedValueText);
		inLowText = (TextView) findViewById(R.id.inLowText);
		outLowText = (TextView) findViewById(R.id.outLowText);
		inHighText = (TextView) findViewById(R.id.inHighText);
		outHighText = (TextView) findViewById(R.id.outHighText);
		addressText = (TextView) findViewById(R.id.addressText);

		findViewById(R.id.editLowButton).setOnClickListener(clickListener);
		findViewById(R.id.editHighButton).setOnClickListener(clickListener);
		findViewById(R.id.editAddressButton).setOnClickListener(clickListener);

	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			switch (arg0.getId()) {

			case R.id.editLowButton:

				sensorSettingsViewListener
						.onEditLowClicked(SensorSettingsView.this);

				// SensorCalibrationEditDialog dialogLow = new
				// SensorCalibrationEditDialog(
				// getContext(), sensor.getCalibration().getInputLow(),
				// sensor.getCalibration().getOutputLow(),
				// new SensorCalibrationEditDialogListener() {
				//
				// @Override
				// public void onSaved(float input, float output) {
				// sensor.getCalibration().setInputLow(input);
				// sensor.getCalibration().setOutputLow(output);
				// sensorSettingsViewListener
				// .onChanged(SensorSettingsView.this);
				// }
				//
				// @Override
				// public float requestCapture() {
				// return sensor.getValue();
				// }
				//
				// });
				//
				// dialogLow.show();

				break;

			case R.id.editHighButton:

				sensorSettingsViewListener
						.onEditHighClicked(SensorSettingsView.this);

				// SensorCalibrationEditDialog dialogHigh = new
				// SensorCalibrationEditDialog(
				// getContext(), sensor.getCalibration().getInputHigh(),
				// sensor.getCalibration().getOutputHigh(),
				// new SensorCalibrationEditDialogListener() {
				//
				// @Override
				// public void onSaved(float input, float output) {
				// sensor.getCalibration().setInputHigh(input);
				// sensor.getCalibration().setOutputHigh(output);
				// sensorSettingsViewListener
				// .onChanged(SensorSettingsView.this);
				// }
				//
				// @Override
				// public float requestCapture() {
				// return sensor.getValue();
				// }
				//
				// });
				//
				// dialogHigh.show();

				break;

			case R.id.editAddressButton:

				sensorSettingsViewListener
						.onEditAddressClicked(SensorSettingsView.this);
				// String[] addresses = new String[oneWireAddresses.size()];
				//
				// for (int i = 0; i < addresses.length; i++) {
				// addresses[i] = oneWireAddresses.get(i);
				// }
				//
				// SensorAddressEditDialog dialogAddress = new
				// SensorAddressEditDialog(
				// getContext(), addresses, sensor.getAddress(),
				// new SensorAddressEditDialogListener() {
				//
				// @Override
				// public void onSaved(String address) {
				// sensor.setAddress(address);
				// sensorSettingsViewListener
				// .onChanged(SensorSettingsView.this);
				// }
				//
				// });
				//
				// dialogAddress.show();

				break;
			}

		}

	};

	public void setSensorSettingsViewListener(
			SensorSettingsViewListener sensorSettingsViewListener) {
		this.sensorSettingsViewListener = sensorSettingsViewListener;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;

		// if (!inLowText.isFocused()) {
		// inLowText.setText(sensor.getCalibration().getInputLow() + "");
		// }
		// if (!outLowText.isFocused()) {
		// outLowText.setText(sensor.getCalibration().getOutputLow() + "");
		// }
		// if (!inHighText.isFocused()) {
		// inHighText.setText(sensor.getCalibration().getInputHigh() + "");
		// }
		// if (!outHighText.isFocused()) {
		// outHighText.setText(sensor.getCalibration().getOutputHigh() + "");
		// }
		// if (!addressText.isFocused()) {
		// addressText.setText(sensor.getAddress());
		// }

		inLowText.setText(sensor.getCalibration().getInputLow() + "");
		outLowText.setText(sensor.getCalibration().getOutputLow() + "");
		inHighText.setText(sensor.getCalibration().getInputHigh() + "");
		outHighText.setText(sensor.getCalibration().getOutputHigh() + "");
		addressText.setText(sensor.getAddress());

		nameText.setText(sensor.getSensorName().toString());
		rawValueText.setText(sensor.getValue() + "");
		calibratedValueText.setText(sensor.getCalibratedValue() + "");

	}

	public void setOneWireAddresses(List<String> oneWireAddresses) {
		this.oneWireAddresses = oneWireAddresses;
	}

	public Sensor getSensor() {
		return sensor;
	}

	public static interface SensorSettingsViewListener {
		public void onEditHighClicked(SensorSettingsView sender);

		public void onEditLowClicked(SensorSettingsView sender);

		public void onEditAddressClicked(SensorSettingsView sender);
	}

}
