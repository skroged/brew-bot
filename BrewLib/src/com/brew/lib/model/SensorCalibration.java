package com.brew.lib.model;

public class SensorCalibration {

	private float inputLow;
	private float inputHigh;
	private float outputLow;
	private float outputHigh;
	private float slope;
	private float intercept;
	private boolean calculatedEqution;

	public float getInputLow() {
		return inputLow;
	}

	public void setInputLow(float inputLow) {
		this.inputLow = inputLow;
	}

	public float getInputHigh() {
		return inputHigh;
	}

	public void setInputHigh(float inputHigh) {
		this.inputHigh = inputHigh;
	}

	public float getOutputLow() {
		return outputLow;
	}

	public void setOutputLow(float outputLow) {
		this.outputLow = outputLow;
	}

	public float getOutputHigh() {
		return outputHigh;
	}

	public void setOutputHigh(float outputHigh) {
		this.outputHigh = outputHigh;
	}

	public float transpose(float input) {

		if (!calculatedEqution) {

			float rise = outputHigh - outputLow;
			float run = inputHigh - outputLow;

			slope = rise / run;

			intercept = outputLow - inputLow * slope;

		}

		return input * slope + intercept;
	}

}
