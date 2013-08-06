package com.brew.lib.model;

public class SensorTransport {

	private Float value;
	private SENSOR_NAME sensorName;

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

}
