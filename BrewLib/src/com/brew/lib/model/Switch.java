package com.brew.lib.model;

public class Switch {

	private int id;
	private boolean value;
	private SWITCH_NAME name;
	private int address;

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public SWITCH_NAME getName() {
		return name;
	}

	public void setName(SWITCH_NAME name) {
		this.name = name;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getAddress() {
		return address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
