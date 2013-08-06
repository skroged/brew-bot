package com.brew.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.SwitchTransport;
import com.brew.server.Sensor.SensorListener;
import com.brew.server.Switch.SwitchListener;
import com.brew.server.socket.SocketChannel;
import com.brew.server.socket.SocketConnection;

public class SensorManager {

	public static void init() {

		Sensor.registerSensorListener(sensorListener);
		Switch.registerSwitchListener(switchListener);
		Switch.init();

		Sensor.startUpdating();

	}

	public static void receiveUpdate(BrewMessage message) {

		if (message.getData() != null) {

			if (message.getData().getSwitches() != null) {

				Map<SWITCH_NAME, Switch> switches = Switch.getSwitches();
				for (SwitchTransport sw : message.getData().getSwitches()) {

					Switch switchh = switches.get(sw.getSwitchName());
					if (switchh != null) {
						switchh.setValue(sw.getSwitchValue());
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

	public static void requestDump(final SocketConnection socket) {

		new Thread() {

			@Override
			public void run() {

				BrewMessage message = new BrewMessage();

				message.setMethod(SOCKET_METHOD.DATA_UPDATE);

				BrewData data = new BrewData();

				List<SensorTransport> sensorTransports = new ArrayList<SensorTransport>();

				List<Sensor> sensors = Sensor.getSensors();
				for (Sensor sensor : sensors) {

					SensorTransport sensorTransport = new SensorTransport();

					sensorTransport.setValue(sensor.getValue());
					sensorTransport.setSensorName(sensor.getSensorName());

					sensorTransports.add(sensorTransport);
				}

				data.setSensors(sensorTransports);

				List<SwitchTransport> switchTransports = new ArrayList<SwitchTransport>();
				Map<SWITCH_NAME, Switch> switches = Switch.getSwitches();
				Iterator<?> it = switches.entrySet().iterator();
				while (it.hasNext()) {

					SwitchTransport switchTransport = new SwitchTransport();

					Map.Entry<?,?> pairs = (Map.Entry<?,?>) it.next();

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

}
