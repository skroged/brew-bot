package com.brew.lib.model;

public class SensorSettingsTransport {

	private Float value;
	private Float calibratedValue;
	private SENSOR_NAME sensorName;
	private float inputLow;
	private float outputLow;
	private float inputHigh;
	private float outputHigh;
	private String address;

	public SensorSettingsTransport(Sensor sensor) {
		value = sensor.getValue();
		calibratedValue = sensor.getCalibratedValue();
		sensorName = sensor.getSensorName();
		inputLow = sensor.getCalibration().getInputLow();
		outputLow = sensor.getCalibration().getOutputLow();
		inputHigh = sensor.getCalibration().getInputHigh();
		outputHigh = sensor.getCalibration().getOutputHigh();
		address = sensor.getAddress();
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public SENSOR_NAME getSensorName() {
		return sensorName;
	}

	public void setSensorName(SENSOR_NAME sensorName) {
		this.sensorName = sensorName;
	}

	public Float getCalibratedValue() {
		return calibratedValue;
	}

	public void setCalibratedValue(Float calibratedValue) {
		this.calibratedValue = calibratedValue;
	}

	public float getInputLow() {
		return inputLow;
	}

	public void setInputLow(float inputLow) {
		this.inputLow = inputLow;
	}

	public float getOutputLow() {
		return outputLow;
	}

	public void setOutputLow(float outputLow) {
		this.outputLow = outputLow;
	}

	public float getInputHigh() {
		return inputHigh;
	}

	public void setInputHigh(float inputHigh) {
		this.inputHigh = inputHigh;
	}

	public float getOutputHigh() {
		return outputHigh;
	}

	public void setOutputHigh(float outputHigh) {
		this.outputHigh = outputHigh;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
