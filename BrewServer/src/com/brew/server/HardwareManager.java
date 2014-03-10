package com.brew.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SENSOR_NAME;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorSettingsTransport;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.Switch;
import com.brew.lib.model.SwitchTransport;
import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketChannel;
import com.brew.server.socket.SocketConnection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HardwareManager {

	public static final String ONE_WIRE_PATH_START = "/sys/bus/w1/devices/";
	public static final String ONE_WIRE_PATH_END = "/w1_slave";
	private static List<Sensor> sensors;
	private static Map<Integer, Switch> switches;

	public static void init() {

		registerSensorListener(sensorListener);
		registerSwitchListener(switchListener);
		initSwitches();
		initSensors();

		startUpdating();

	}

	private static void initSensors() {

		sensors = new ArrayList<Sensor>();

		for (SENSOR_NAME sn : SENSOR_NAME.values()) {
			sensors.add(MySqlManager.getSensor(sn));
		}

		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.HLT_TEMP));
		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.MLT_TEMP));
		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.BK_TEMP));
		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.FERM_TEMP));
		//
		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.BK_VOLUME));
		// sensors.add(MySqlManager.getSensor(SENSOR_NAME.HLT_VOLUME));
	}

	private static void initSwitches() {
		switches = new Hashtable<Integer, Switch>();

		for (SWITCH_NAME sn : SWITCH_NAME.values()) {
			Switch switchh = MySqlManager.getSwitch(sn);
			if (switchh != null) {
				switches.put(switchh.getId(), switchh);
			}
		}

		// Switch hltPump = new Switch();
		// hltPump.setName(SWITCH_NAME.HLT_PUMP);
		// switches.put(hltPump.getName(), hltPump);
		//
		// Switch hltBurner = new Switch();
		// hltBurner.setName(SWITCH_NAME.HLT_BURNER);
		// switches.put(hltBurner.getName(), hltBurner);
		//
		// Switch hltHlt = new Switch();
		// hltHlt.setName(SWITCH_NAME.HLT_HLT);
		// switches.put(hltHlt.getName(), hltHlt);
		//
		// Switch hltMlt = new Switch();
		// hltMlt.setName(SWITCH_NAME.HLT_MLT);
		// switches.put(hltMlt.getName(), hltMlt);
		//
		// Switch mltPump = new Switch();
		// mltPump.setName(SWITCH_NAME.MLT_PUMP);
		// switches.put(mltPump.getName(), mltPump);
		//
		// Switch mltBurner = new Switch();
		// mltBurner.setName(SWITCH_NAME.MLT_BURNER);
		// switches.put(mltBurner.getName(), mltBurner);
		//
		// Switch mltMlt = new Switch();
		// mltMlt.setName(SWITCH_NAME.MLT_MLT);
		// switches.put(mltMlt.getName(), mltMlt);
		//
		// Switch mltBk = new Switch();
		// mltBk.setName(SWITCH_NAME.MLT_BK);
		// switches.put(mltBk.getName(), mltBk);
		//
		// Switch bkPump = new Switch();
		// bkPump.setName(SWITCH_NAME.BK_PUMP);
		// switches.put(bkPump.getName(), bkPump);
		//
		// Switch bkBurner = new Switch();
		// bkBurner.setName(SWITCH_NAME.BK_BURNER);
		// switches.put(bkBurner.getName(), bkBurner);
		//
		// Switch bkBk = new Switch();
		// bkBk.setName(SWITCH_NAME.BK_BK);
		// switches.put(bkBk.getName(), bkBk);
		//
		// Switch bkFerm = new Switch();
		// bkFerm.setName(SWITCH_NAME.BK_FERM);
		// switches.put(bkFerm.getName(), bkFerm);
		//
		// Switch igniter = new Switch();
		// igniter.setName(SWITCH_NAME.IGNITER);
		// switches.put(igniter.getName(), igniter);
	}

	public static void receiveUpdate(BrewMessage message) {

		switch (message.getMethod()) {

		case SENSOR_SETTINGS_UPDATE:

			if (message.getData() != null) {

				if (message.getData().getSensorSettings() != null) {

					for (SensorSettingsTransport sst : message.getData()
							.getSensorSettings()) {

						for (Sensor sensor : sensors) {

							if (sensor.getSensorId() == sst.getSensorId()) {

								Logger.log("SETTINGS", "new settings for "
										+ sensor.getSensorName());

								sensor.populateFromSettingsTransport(sst, false);
								// invoke calibration
								sensor.setValue(sensor.getValue());
								MySqlManager.saveSensor(sensor);

								notifySensorChanged(sensor);

								break;
							}

						}

					}

				}
			}

			break;

		case SWITCH_UPDATE:

			if (message.getData() != null) {

				if (message.getData().getSwitchTransports() != null) {

					for (SwitchTransport sw : message.getData()
							.getSwitchTransports()) {

						Switch switchh = switches.get(sw.getSwitchId());
						if (switchh != null) {
							updateSwitchValue(switchh, sw.getSwitchValue());
						} else {
							// System.out.println("no value for "
							// + sw.getSwitchName());
						}

					}
				}
			}

			break;
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
			switchTransport.setSwitchId(switchh.getId());

			switches.add(switchTransport);

			data.setSwitchTransports(switches);

			message.setData(data);

			SocketChannel.get(SOCKET_CHANNEL.BREW_CONTROL).sendBroadcast(
					message);

		}

	};

	private static SensorListener sensorListener = new SensorListener() {

		@Override
		public void onValueChanged(Sensor sensor) {

			BrewMessage dataUpdateMessage = sensor.getDataUpdateMessage();

			SocketChannel.get(SOCKET_CHANNEL.BREW_CONTROL).sendBroadcast(
					dataUpdateMessage);

			BrewMessage settingsUpdateMessage = sensor
					.getSettingsUpdateMessage();

			SocketChannel.get(SOCKET_CHANNEL.SENSOR_SETTINGS).sendBroadcast(
					settingsUpdateMessage);
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

				// for (Sensor sensor : sensors) {
				//
				// SensorTransport sensorTransport = new SensorTransport();
				//
				// sensorTransport.setValue(sensor.getCalibratedValue());
				// sensorTransport.setSensorId(sensor.getSensorId());
				//
				// sensorTransports.add(sensorTransport);
				// }

				data.setSensors(sensors);

				// List<SwitchTransport> switchTransports = new
				// ArrayList<SwitchTransport>();
				List<Switch> switchList = new ArrayList<Switch>();
				Iterator<?> it = switches.entrySet().iterator();
				while (it.hasNext()) {

					// SwitchTransport switchTransport = new SwitchTransport();

					Map.Entry<?, ?> pairs = (Map.Entry<?, ?>) it.next();

					Switch switchh = (Switch) pairs.getValue();

					switchList.add(switchh);
					// switchTransport.setSwitchValue(switchh.getValue());
					// switchTransport.setSwitchName(switchh.getName());

					// switchTransports.add(switchTransport);

				}

				data.setSwitches(switchList);

				message.setData(data);

				socket.sendMessage(message);
				// SocketManager.sendMessage(message, socketId);

				super.run();
			}

		}.start();
	}

	public static void requestSettingsDump(final SocketConnection socket) {

		new Thread() {

			@Override
			public void run() {

				BrewMessage message = new BrewMessage();

				message.setMethod(SOCKET_METHOD.SENSOR_SETTINGS_UPDATE);

				BrewData data = new BrewData();

				List<SensorSettingsTransport> sensorTransports = new ArrayList<SensorSettingsTransport>();

				for (Sensor sensor : sensors) {

					SensorSettingsTransport sensorTransport = new SensorSettingsTransport(
							sensor);

					sensorTransports.add(sensorTransport);
				}

				data.setSensorSettings(sensorTransports);

				List<String> oneWireAddresses = getOneWireDevices();
				Logger.log("SETTINGS",
						"One-Wire devices (" + oneWireAddresses.size() + ")");
				for (String s : oneWireAddresses) {
					Logger.log("SETTINGS", s);
				}

				data.setOneWireAddresses(oneWireAddresses);

				message.setData(data);

				socket.sendMessage(message);

				super.run();
			}

		}.start();
	}

	public static List<Sensor> getSensors() {
		return sensors;
	}

	public static Map<Integer, Switch> getSwitches() {
		return switches;
	}

	public static class UpdateSPIThread extends Thread {

		private boolean kilt;

		@Override
		public synchronized void start() {
			super.start();
		}

		@Override
		public void run() {
			try {

				Logger.log("SYSTEM", "Starting SPI");

				kilt = false;

				Process proc = Runtime.getRuntime().exec("/home/pi/BrewServer/Spi_Pressure");

				BufferedReader stdInput = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));

				BufferedReader stdError = new BufferedReader(
						new InputStreamReader(proc.getErrorStream()));

				String s;
				while (!kilt && (s = stdInput.readLine()) != null) {
					updateSPISensorValues(s);
				}

				// read any errors from the attempted command
				// System.out
				// .println("Here is the standard error of the command (if any):\n");
				while (!kilt && (s = stdError.readLine()) != null) {
					System.out.println(s);
				}

				proc.destroy();

				stdInput.close();
				stdError.close();

				Logger.log("SYSTEM", "SPI ended");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void kill() {
			kilt = true;
		}
	}

	// public static class UpdateADCSensorsThread extends Thread {
	//
	// private boolean kilt;
	//
	// @Override
	// public synchronized void start() {
	// super.start();
	// }
	//
	// @Override
	// public void run() {
	// try {
	//
	// Logger.log("SYSTEM", "Starting ADC");
	//
	// kilt = false;
	//
	// Process proc = Runtime.getRuntime().exec(
	// "sudo python /home/pi/BrewServer/I2C/ADC.py");
	//
	// BufferedReader stdInput = new BufferedReader(
	// new InputStreamReader(proc.getInputStream()));
	//
	// BufferedReader stdError = new BufferedReader(
	// new InputStreamReader(proc.getErrorStream()));
	//
	//
	// String s;
	// while (!kilt && (s = stdInput.readLine()) != null) {
	// System.out.println("read: " + s);
	// // updateADCSensorValues(s);
	// }
	//
	// // read any errors from the attempted command
	// // System.out
	// // .println("Here is the standard error of the command (if any):\n");
	// while (!kilt && (s = stdError.readLine()) != null) {
	// System.out.println(s);
	// }
	//
	// proc.destroy();
	//
	// stdInput.close();
	// stdError.close();
	//
	// Logger.log("SYSTEM", "ADC ended");
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public void kill() {
	// kilt = true;
	// }
	// }

	public static class UpdateADCSensorsThread extends Thread {

		private boolean kilt;

		@Override
		public synchronized void start() {
			super.start();
		}

		@Override
		public void run() {
			try {

				Logger.log("SYSTEM", "Starting ADC");

				kilt = false;

				while (!kilt) {
					Process proc = Runtime.getRuntime().exec(
							"/usr/bin/python /home/pi/BrewServer/I2C/ADC.py");

					BufferedReader stdInput = new BufferedReader(
							new InputStreamReader(proc.getInputStream()));

					BufferedReader stdError = new BufferedReader(
							new InputStreamReader(proc.getErrorStream()));

					String s;
					while (!kilt && (s = stdInput.readLine()) != null) {
						// System.out.println("read: " + s);
						updateADCSensorValues(s);
					}

					// read any errors from the attempted command
					// System.out
					// .println("Here is the standard error of the command (if any):\n");
					while (!kilt && (s = stdError.readLine()) != null) {
						System.out.println(s);
					}

					proc.destroy();

					stdInput.close();
					stdError.close();
				}
				Logger.log("SYSTEM", "ADC ended");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void kill() {
			kilt = true;
		}
	}

	/*
	 * boolean changed = sensor.getValue() != degC;
	 * 
	 * if (changed) { Logger.log("DATA", sensor.getSensorName() + ": " + degC);
	 * }
	 * 
	 * if (changed) { sensor.setValue(degC); notifySensorChanged(sensor); }
	 */
	public static class UpdateOneWireThread extends Thread {
		private boolean stopped;

		@Override
		public synchronized void start() {
			super.start();
		}

		@Override
		public void run() {

			Logger.log("SYSTEM", "Starting ONE-WIRE");

			while (!stopped) {

				List<String> oneWireAddresses = getOneWireDevices();

				for (String address : oneWireAddresses) {

					for (Sensor sensor : sensors) {

						if (sensor.getAddress().equals(address)) {

							float value = readOneWireSensorValue(address);

							boolean changed = Math.abs(sensor.getValue()
									- value) > 0;

							if (changed) {
								Logger.log("DATA", sensor.getSensorName()
										+ ": " + value);
								notifySensorChanged(sensor);
								sensor.setValue(value);
							}

							break;
						}
					}

				}

				// for (Sensor sensor : sensors) {
				// if (sensor.getSensorName() == SENSOR_NAME.BK_TEMP
				// || sensor.getSensorName() == SENSOR_NAME.HLT_TEMP
				// || sensor.getSensorName() == SENSOR_NAME.MLT_TEMP
				// || sensor.getSensorName() == SENSOR_NAME.FERM_TEMP)
				//
				// }

			}
		}
	}

	private static UpdateOneWireThread updateOneWireThread;
	private static UpdateSPIThread updateSPIThread;
	private static UpdateADCSensorsThread updateADCSensorsThread;

	public static void startUpdating() {

		try {
			Process p1 = Runtime.getRuntime().exec("/sbin/modprobe w1-gpio");
			p1.waitFor();
			Process p2 = Runtime.getRuntime().exec("/sbin/modprobe w1-therm");
			p2.waitFor();
		} catch (IOException e) {
			Logger.log("SYSTEM", "failed to enable one-wire");
			Logger.log("ERROR", e.getMessage());
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updateOneWireThread = new UpdateOneWireThread();
		updateOneWireThread.start();

		updateSPIThread = new UpdateSPIThread();
		updateSPIThread.start();

		updateADCSensorsThread = new UpdateADCSensorsThread();
		updateADCSensorsThread.start();
	}

	public static void stopUddating() throws InterruptedException {
		updateOneWireThread.stopped = true;

		try {
			updateSPIThread.kill();

			updateOneWireThread.join(1000);
			updateSPIThread.join(1000);
			updateADCSensorsThread.join(1000);
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
			setSwitchHardware();
		}
	}

	private static void setSwitchHardware() {

		new Thread() {

			@Override
			public void run() {

				// BitSet bs = new BitSet(16);
				int bits = 0xffff;
				Iterator<?> it = switches.entrySet().iterator();
				while (it.hasNext()) {

					SwitchTransport switchTransport = new SwitchTransport();

					Map.Entry<?, ?> pairs = (Map.Entry<?, ?>) it.next();

					Switch switchh = (Switch) pairs.getValue();

					int address = switchh.getAddress();
					// int value = switchh.getValue() ? 0 : 1;

					if (switchh.getValue()) {
						bits &= ~(1 << address);
					} else {
						bits |= (1 << address);
					}

					// value <<= 8;
					// value &= 0xff;

					// bits &= (value << address);

					// bs.set(address, value);

				}

				// System.out.println("bits: " + bits);

				byte lowByte = (byte) (bits & 0xff);
				byte hiByte = (byte) ((bits & 0xff00) >> 8);

				String byte1Str = String.format("0x%x", lowByte);
				String byte2Str = String.format("0x%x", hiByte);

				try {
					Runtime.getRuntime().exec(
							"/usr/sbin/i2cset -y 1 0x25 " + byte1Str);

					Runtime.getRuntime().exec(
							"/usr/sbin/i2cset -y 1 0x26 " + byte2Str);

					// System.out.println("sudo i2cset -y 1 0x25 " + byte1Str);
					// System.out.println("sudo i2cset -y 1 0x26 " + byte2Str);

				} catch (IOException e) {
					e.printStackTrace();
				}

				super.run();
			}

		}.start();

	}

	private static List<String> getOneWireDevices() {

		List<String> returnList = new ArrayList<String>();

		String path = ONE_WIRE_PATH_START;

		File directory = new File(path);
		File[] devices = directory.listFiles();

		if (devices != null) {
			for (File f : devices) {
				returnList.add(f.getName());
			}
		}

		return returnList;

	}

	public static float readOneWireSensorValue(String address) {

		try {

			String path = ONE_WIRE_PATH_START + address + ONE_WIRE_PATH_END;

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

			br.close();

			// 2a 00 4b 46 ff ff 0d 10 d1 : crc=d1 YES
			// 2a 00 4b 46 ff ff 0d 10 d1 t=20937

			String[] lines = everything.split("\n");

			if (lines[0].contains("YES")) {

				int indexOfT = lines[1].indexOf("t=");

				String tempStr = lines[1].substring(indexOfT + 2);

				try {
					float value = Float.valueOf(tempStr);

					return value;

				} catch (NumberFormatException e) {

				}

			}

			br.close();
		} catch (IOException e) {
			Logger.log("ERROR", e.getMessage());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		}

		return 0;

	}

	public static void updateSPISensorValues(String valuesJson) {

		// float area = (float) (64f * Math.PI);

		JsonParser parser = new JsonParser();
		JsonObject jo = (JsonObject) parser.parse(valuesJson);

		Set<Entry<String, JsonElement>> set = jo.entrySet();
		Iterator<Entry<String, JsonElement>> iterator = set.iterator();
		while (iterator.hasNext()) {

			Entry<String, JsonElement> entry = iterator.next();
			String sensorAddressStr = entry.getKey();

			// float psi = o.getAsJsonPrimitive(sensorNameStr).getAsFloat();

			// float inchOfWater = 27.6704523f * psi;

			// loat volume =//inchOfWater;// area * inchOfWater * 0.004329f;

			// try{
			// SENSOR_NAME sensorName = SENSOR_NAME.valueOf(sensorNameStr);
			// }

			for (Sensor sensor : sensors) {

				if (sensor.getAddress().equals(sensorAddressStr)) {
					// if (sensor.getSensorName() == sensorName) {
					float rawValue = jo.getAsJsonPrimitive(sensorAddressStr)
							.getAsFloat();

					// round to nearest 10...
					int roundedValue = Math.round(rawValue / 10f) * 10;

					boolean changed = sensor.getValue() != roundedValue;
					if (changed) {
						Logger.log("DATA", sensor.getSensorName() + ": "
								+ roundedValue);
						notifySensorChanged(sensor);
						sensor.setValue(roundedValue);
					}

					break;
				}
			}
		}

	}

	private static void updateADCSensorValues(String valuesJson) {

		JsonParser parser = new JsonParser();
		JsonObject jo = (JsonObject) parser.parse(valuesJson);

		Set<Entry<String, JsonElement>> set = jo.entrySet();
		Iterator<Entry<String, JsonElement>> iterator = set.iterator();
		while (iterator.hasNext()) {

			Entry<String, JsonElement> entry = iterator.next();
			String sensorAddressStr = entry.getKey();

			for (Sensor sensor : sensors) {

				if (sensor.getAddress().equals(sensorAddressStr)) {
					// if (sensor.getSensorName() == sensorName) {
					float rawValue = jo.getAsJsonPrimitive(sensorAddressStr)
							.getAsFloat();

					// round to nearest 2...
					int roundedValue = Math.round(rawValue / 2f) * 2;

					boolean changed = sensor.getValue() != roundedValue;
					if (changed) {
						Logger.log("DATA", sensor.getSensorName() + ": "
								+ roundedValue);
						notifySensorChanged(sensor);
						sensor.setValue(roundedValue);
					}

					break;
				}
			}
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
