package com.brew.lib.model;

import java.util.List;

public class BrewData {

	private List<SensorTransport> sensors;
	private List<SwitchTransport> switches;

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

}
