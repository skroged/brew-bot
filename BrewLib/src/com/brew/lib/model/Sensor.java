package com.brew.lib.model;

import java.util.ArrayList;
import java.util.List;

public class Sensor {

	private int sensorId;
	private String address;
	private float value;
	private float calibratedValue;
	private SENSOR_NAME sensorName;
	private SensorCalibration calibration;
	private SENSOR_TYPE sensorType;

	public void populateFromSettingsTransport(
			SensorSettingsTransport transport, boolean applyValue) {

		address = transport.getAddress();
		calibration = new SensorCalibration();
		calibration.setInputHigh(transport.getInputHigh());
		calibration.setInputLow(transport.getInputLow());
		calibration.setOutputHigh(transport.getOutputHigh());
		calibration.setOutputLow(transport.getOutputLow());

		calibration.resetEquation();

		if (applyValue) {
			value = transport.getValue();
			calibratedValue = transport.getCalibratedValue();
		}
	}

	public void populateFromSettingsTransport(SensorSettingsTransport transport) {

		populateFromSettingsTransport(transport, true);

	}

	public BrewMessage getDataUpdateMessage() {
		BrewMessage message = new BrewMessage();

		message.setMethod(SOCKET_METHOD.DATA_UPDATE);

		BrewData data = new BrewData();

		List<SensorTransport> sensors = new ArrayList<SensorTransport>();

		SensorTransport sensorTransport = new SensorTransport();

		sensorTransport.setValue(getCalibratedValue());
		sensorTransport.setSensorId(getSensorId());

		sensors.add(sensorTransport);

		data.setSensorTransports(sensors);

		message.setData(data);

		return message;
	}

	public BrewMessage getSettingsUpdateMessage() {

		BrewMessage message = new BrewMessage();

		message.setMethod(SOCKET_METHOD.SENSOR_SETTINGS_UPDATE);

		BrewData data = new BrewData();

		List<SensorSettingsTransport> sensorTransports = new ArrayList<SensorSettingsTransport>();

		SensorSettingsTransport sensorTransport = new SensorSettingsTransport(
				this);

		sensorTransports.add(sensorTransport);

		data.setSensorSettings(sensorTransports);

		message.setData(data);

		return message;
	}

	public void setCalibratedValue(float calibratedValue) {
		this.calibratedValue = calibratedValue;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {

		this.value = value;

		if (calibration != null) {
			calibratedValue = calibration.transpose(value);
		} else {
			calibratedValue = value;
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

	public SENSOR_TYPE getSensorType() {
		return sensorType;
	}

	public void setSensorType(SENSOR_TYPE sensorType) {
		this.sensorType = sensorType;
	}

	public int getSensorId() {
		return sensorId;
	}

	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

}
