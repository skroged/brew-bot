package com.brew.lib.model;

import java.util.List;

public class BrewData {

	private List<SensorSettingsTransport> sensorSettings;
	private List<SensorTransport> sensors;
	private List<SwitchTransport> switches;
	private List<User> users;

	public List<SensorTransport> getSensors() {
		return sensors;
	}

	public void setSensors(List<SensorTransport> sensors) {
		this.sensors = sensors;
	}

	public List<SwitchTransport> getSwitches() {
		return switches;
	}

	public void setSwitches(List<SwitchTransport> switches) {
		this.switches = switches;
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

}
