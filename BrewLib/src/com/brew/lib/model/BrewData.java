package com.brew.lib.model;

import java.util.List;

public class BrewData {

	private List<SensorSettingsTransport> sensorSettings;
	private List<SensorTransport> sensorTransports;
	private List<Sensor> sensors;
	private List<SwitchTransport> switchTransports;
	private List<Switch> switches;
	private List<User> users;
	private List<String> oneWireAddresses;

	public List<SensorTransport> getSensorTransports() {
		return sensorTransports;
	}

	public void setSensorTransports(List<SensorTransport> sensorTransports) {
		this.sensorTransports = sensorTransports;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<SensorSettingsTransport> getSensorSettings() {
		return sensorSettings;
	}

	public void setSensorSettings(List<SensorSettingsTransport> sensorSettings) {
		this.sensorSettings = sensorSettings;
	}

	public List<String> getOneWireAddresses() {
		return oneWireAddresses;
	}

	public void setOneWireAddresses(List<String> oneWireAddresses) {
		this.oneWireAddresses = oneWireAddresses;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	public List<SwitchTransport> getSwitchTransports() {
		return switchTransports;
	}

	public void setSwitchTransports(List<SwitchTransport> switchTransports) {
		this.switchTransports = switchTransports;
	}

	public List<Switch> getSwitches() {
		return switches;
	}

	public void setSwitches(List<Switch> switches) {
		this.switches = switches;
	}

}
