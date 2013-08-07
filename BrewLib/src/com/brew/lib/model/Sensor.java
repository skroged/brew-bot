package com.brew.lib.model;

public class Sensor {

	private String address;
	private float value;
	private float calibratedValue;
	private SENSOR_NAME sensorName;
	private SensorCalibration calibration;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {

		this.value = value;

		if (calibration != null) {
			calibratedValue = calibration.transpose(value);
		}
	}

	public SENSOR_NAME getSensorName() {
		return sensorName;
	}

	public void setSensorName(SENSOR_NAME sensorName) {
		this.sensorName = sensorName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public SensorCalibration getCalibration() {
		return calibration;
	}

	public void setCalibration(SensorCalibration calibration) {
		this.calibration = calibration;
	}

	public float getCalibratedValue() {
		return calibratedValue;
	}

}
