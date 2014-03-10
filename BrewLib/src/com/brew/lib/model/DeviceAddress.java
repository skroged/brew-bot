package com.brew.lib.model;

public class DeviceAddress {

	private String address;
	private SENSOR_TYPE type;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public SENSOR_TYPE getType() {
		return type;
	}

	public void setType(SENSOR_TYPE type) {
		this.type = type;
	}
}
