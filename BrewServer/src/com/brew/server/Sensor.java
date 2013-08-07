package com.brew.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.brew.lib.model.SENSOR_NAME;

public class Sensor {

	public static final String ONE_WIRE_PATH_START = "/sys/bus/w1/devices/";
	public static final String ONE_WIRE_PATH_END = "/w1_slave";

	public static final String TEMP_1_ID = "28-00000474d694";
	// private static final long UPDATE_SLEEP = 1000;

	private String sensorId;
	private float value;
	private SENSOR_NAME sensorName;

	private static List<Sensor> sensors;

	public static List<Sensor> getSensors() {
		return sensors;
	}

	private void readSensor() {

		try {

			String path = ONE_WIRE_PATH_START + sensorId + ONE_WIRE_PATH_END;

			FileReader fileReader = new FileReader(path);

			BufferedReader br = new BufferedReader(fileReader);

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}

			String everything = sb.toString();

			// 2a 00 4b 46 ff ff 0d 10 d1 : crc=d1 YES
			// 2a 00 4b 46 ff ff 0d 10 d1 t=20937

			String[] lines = everything.split("\n");

			if (lines[0].contains("YES")) {

				int indexOfT = lines[1].indexOf("t=");

				String tempStr = lines[1].substring(indexOfT + 2);

				try {
					float degC = Float.valueOf(tempStr);

					degC /= 1000;

					float degF = (9f / 5f) * degC + 32f;

					setValue(degF);
					// Tf = (9/5)*Tc+32

				} catch (NumberFormatException e) {

				}

			}

			br.close();
		} catch (IOException e) {
			Logger.log("ERROR", e.getMessage());
		}

	}

	private static UpdateThread updateThread;

	public static void startUpdating() {

		try {
			Runtime.getRuntime().exec("sudo modprobe w1-gpio");
			Runtime.getRuntime().exec("sudo modprobe w1-therm");
		} catch (IOException e) {
			Logger.log("SYSTEM", "failed to enable one-wire");
			Logger.log("ERROR", e.getMessage());
			return;
		}

		Logger.log("SYSTEM", "Starting ONE-WIRE");

		sensors = new ArrayList<Sensor>();

		Sensor sensor = new Sensor();
		sensor.sensorId = TEMP_1_ID;
		sensor.sensorName = SENSOR_NAME.HLT_TEMP;

		sensors.add(sensor);

		updateThread = new UpdateThread();

		updateThread.start();
	}

	public static void stopUddating() throws InterruptedException {
		updateThread.stopped = true;

		try {
			updateThread.join(1000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {

		boolean changed = this.value != value;

		if (changed) {
			Logger.log("DATA", sensorName + ": " + value);
		}

		this.value = value;

		if (changed) {
			notifyChanged();
		}
	}

	public SENSOR_NAME getSensorName() {
		return sensorName;
	}

	public void setSensorName(SENSOR_NAME sensorName) {
		this.sensorName = sensorName;
	}

	private void notifyChanged() {

		synchronized (sensorListeners) {

			for (SensorListener sl : sensorListeners) {

				sl.onValueChanged(this);
			}

		}
	}

	public static void registerSensorListener(SensorListener sensorListener) {

		synchronized (sensorListeners) {

			sensorListeners.add(sensorListener);

		}
	}

	private static List<SensorListener> sensorListeners = Collections
			.synchronizedList(new ArrayList<SensorListener>());

	public static interface SensorListener {

		void onValueChanged(Sensor sensor);

	}

	public static class UpdateThread extends Thread {
		private boolean stopped;

		@Override
		public synchronized void start() {
			super.start();
		}

		@Override
		public void run() {
			while (!stopped) {

				for (Sensor sensor : sensors) {
					sensor.readSensor();
				}

			}
		}
	}

}
