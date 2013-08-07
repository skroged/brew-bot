package com.brew.lib.model;


public class Switch {

	private boolean value;
	private SWITCH_NAME name;

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

}
