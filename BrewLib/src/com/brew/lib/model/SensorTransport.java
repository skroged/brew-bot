package com.brew.lib.model;

public class SensorTransport {

	private Float value;
	// private SENSOR_NAME sensorName;
	private int sensorId;

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	// public SENSOR_NAME getSensorName() {
	// return sensorName;
	// }
	//
	// public void setSensorName(SENSOR_NAME sensorName) {
	// this.sensorName = sensorName;
	// }

	public int getSensorId() {
		return sensorId;
	}

	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

}
