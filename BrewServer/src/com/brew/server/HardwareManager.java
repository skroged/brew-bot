package com.brew.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.Switch;
import com.brew.lib.model.SwitchTransport;
import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketChannel;
import com.brew.server.socket.SocketConnection;

public class HardwareManager {

	public static final String ONE_WIRE_PATH_START = "/sys/bus/w1/devices/";
	public static final String ONE_WIRE_PATH_END = "/w1_slave";
	private static List<Sensor> sensors;
	private static Map<SWITCH_NAME, Switch> switches;

	public static void init() {

		registerSensorListener(sensorListener);
		registerSwitchListener(switchListener);
		initSwitches();
		initSensors();

		startUpdating();

	}

	private static void initSensors() {

		Logger.log("SYSTEM", "Starting ONE-WIRE");

		sensors = new ArrayList<Sensor>();

		Sensor sensor = MySqlManager.getSensor(SENSOR_NAME.HLT_TEMP);

		sensors.add(sensor);
	}

	private static void initSwitches() {
		switches = new Hashtable<SWITCH_NAME, Switch>();

		Switch hltPump = new Switch();
		hltPump.setName(SWITCH_NAME.HLT_PUMP);
		switches.put(hltPump.getName(), hltPump);

		Switch hltBurner = new Switch();
		hltBurner.setName(SWITCH_NAME.HLT_BURNER);
		switches.put(hltBurner.getName(), hltBurner);

		Switch hltHlt = new Switch();
		hltHlt.setName(SWITCH_NAME.HLT_HLT);
		switches.put(hltHlt.getName(), hltHlt);

		Switch hltMlt = new Switch();
		hltMlt.setName(SWITCH_NAME.HLT_MLT);
		switches.put(hltMlt.getName(), hltMlt);

		Switch mltPump = new Switch();
		mltPump.setName(SWITCH_NAME.MLT_PUMP);
		switches.put(mltPump.getName(), mltPump);

		Switch mltBurner = new Switch();
		mltBurner.setName(SWITCH_NAME.MLT_BURNER);
		switches.put(mltBurner.getName(), mltBurner);

		Switch mltMlt = new Switch();
		mltMlt.setName(SWITCH_NAME.MLT_MLT);
		switches.put(mltMlt.getName(), mltMlt);

		Switch mltBk = new Switch();
		mltBk.setName(SWITCH_NAME.MLT_BK);
		switches.put(mltBk.getName(), mltBk);

		Switch bkPump = new Switch();
		bkPump.setName(SWITCH_NAME.BK_PUMP);
		switches.put(bkPump.getName(), bkPump);

		Switch bkBurner = new Switch();
		bkBurner.setName(SWITCH_NAME.BK_BURNER);
		switches.put(bkBurner.getName(), bkBurner);

		Switch bkBk = new Switch();
		bkBk.setName(SWITCH_NAME.BK_BK);
		switches.put(bkBk.getName(), bkBk);

		Switch bkFerm = new Switch();
		bkFerm.setName(SWITCH_NAME.BK_FERM);
		switches.put(bkFerm.getName(), bkFerm);

		Switch igniter = new Switch();
		igniter.setName(SWITCH_NAME.IGNITER);
		switches.put(igniter.getName(), igniter);
	}

	public static void receiveUpdate(BrewMessage message) {

		if (message.getData() != null) {

			if (message.getData().getSwitches() != null) {

				for (SwitchTransport sw : message.getData().getSwitches()) {

					Switch switchh = switches.get(sw.getSwitchName());
					if (switchh != null) {
						updateSwitchValue(switchh, sw.getSwitchValue());
					} else {
						System.out
								.println("no value for " + sw.getSwitchName());
					}

				}
			}
		}
	}

	private static SwitchListener switchListener = new SwitchListener() {

		@Override
		public void onValueChanged(Switch switchh) {

			BrewMessage message = new BrewMessage();

			message.setMethod(SOCKET_METHOD.DATA_UPDATE);

			BrewData data = new BrewData();

			List<SwitchTransport> switches = new ArrayList<SwitchTransport>();

			SwitchTransport switchTransport = new SwitchTransport();

			switchTransport.setSwitchValue(switchh.getValue());
			switchTransport.setSwitchName(switchh.getName());

			switches.add(switchTransport);

			data.setSwitches(switches);

			message.setData(data);

			SocketChannel.get(SOCKET_CHANNEL.BREW_CONTROL).sendBroadcast(
					message);

		}

	};

	private static SensorListener sensorListener = new SensorListener() {

		@Override
		public void onValueChanged(Sensor sensor) {

			BrewMessage message = new BrewMessage();

			message.setMethod(SOCKET_METHOD.DATA_UPDATE);

			BrewData data = new BrewData();

			List<SensorTransport> sensors = new ArrayList<SensorTransport>();

			SensorTransport sensorTransport = new SensorTransport();

			sensorTransport.setValue(sensor.getValue());
			sensorTransport.setSensorName(sensor.getSensorName());

			sensors.add(sensorTransport);

			data.setSensors(sensors);

			message.setData(data);

			SocketChannel.get(SOCKET_CHANNEL.BREW_CONTROL).sendBroadcast(
					message);

		}

	};

	public static void requestDataDump(final SocketConnection socket) {

		new Thread() {

			@Override
			public void run() {

				BrewMessage message = new BrewMessage();

				message.setMethod(SOCKET_METHOD.DATA_UPDATE);

				BrewData data = new BrewData();

				List<SensorTransport> sensorTransports = new ArrayList<SensorTransport>();

				for (Sensor sensor : sensors) {

					SensorTransport sensorTransport = new SensorTransport();

					sensorTransport.setValue(sensor.getValue());
					sensorTransport.setSensorName(sensor.getSensorName());

					sensorTransports.add(sensorTransport);
				}

				data.setSensors(sensorTransports);

				List<SwitchTransport> switchTransports = new ArrayList<SwitchTransport>();
				Iterator<?> it = switches.entrySet().iterator();
				while (it.hasNext()) {

					SwitchTransport switchTransport = new SwitchTransport();

					Map.Entry<?, ?> pairs = (Map.Entry<?, ?>) it.next();

					Switch switchh = (Switch) pairs.getValue();

					switchTransport.setSwitchValue(switchh.getValue());
					switchTransport.setSwitchName(switchh.getName());

					switchTransports.add(switchTransport);

				}

				data.setSwitches(switchTransports);

				message.setData(data);

				socket.sendMessage(message);
				// SocketManager.sendMessage(message, socketId);

				super.run();
			}

		}.start();
	}

	public static List<Sensor> getSensors() {
		return sensors;
	}

	public static Map<SWITCH_NAME, Switch> getSwitches() {
		return switches;
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
					updateSensorValue(sensor);
				}

			}
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

	public static void updateSwitchValue(Switch switchh, boolean newValue) {

		boolean changed = newValue ^ switchh.getValue();

		if (changed) {
			Logger.log("DATA", switchh.getName() + ": " + newValue);
		}

		switchh.setValue(newValue);

		if (changed) {
			notifySwitchChanged(switchh);
		}
	}

	public static void updateSensorValue(Sensor sensor) {

		try {

			String path = ONE_WIRE_PATH_START + sensor.getAddress()
					+ ONE_WIRE_PATH_END;

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

					boolean changed = sensor.getValue() != degF;

					if (changed) {
						Logger.log("DATA", sensor.getSensorName() + ": " + degF);
					}

					if (changed) {
						notifySensorChanged(sensor);
					}

					sensor.setValue(degF);
					// Tf = (9/5)*Tc+32

				} catch (NumberFormatException e) {

				}

			}

			br.close();
		} catch (IOException e) {
			Logger.log("ERROR", e.getMessage());
		}

	}

	private static void notifySensorChanged(Sensor sensor) {

		synchronized (sensorListeners) {

			for (SensorListener sl : sensorListeners) {

				sl.onValueChanged(sensor);
			}

		}
	}

	private static void notifySwitchChanged(Switch switchh) {

		synchronized (switchListeners) {

			for (SwitchListener sl : switchListeners) {

				sl.onValueChanged(switchh);
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

	public static void registerSwitchListener(SwitchListener switchListener) {

		synchronized (switchListeners) {

			switchListeners.add(switchListener);

		}
	}

	private static List<SwitchListener> switchListeners = Collections
			.synchronizedList(new ArrayList<SwitchListener>());

	public static interface SwitchListener {

		void onValueChanged(Switch sensor);

	}

}
