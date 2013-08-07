package com.brew.lib.model;


public class Sensor {

	private String address;
	private float value;
	private SENSOR_NAME sensorName;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
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

}
