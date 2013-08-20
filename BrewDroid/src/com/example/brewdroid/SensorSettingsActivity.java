package com.example.brewdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.SensorSettingsTransport;
import com.example.brewdroid.SensorSettingsAdapter.SensorSettingsAdapterListener;

public class SensorSettingsActivity extends Activity {

	private Handler handler;
	private ListView sensorList;
	private List<Sensor> sensors;
	private SensorSettingsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor_settings);

		sensors = Collections.synchronizedList(new ArrayList<Sensor>());
		handler = new Handler();

		sensorList = (ListView) findViewById(R.id.sensorList);

		adapter = new SensorSettingsAdapter(this, sensors,
				sensorSettingsAdapterListener);
		sensorList.setAdapter(adapter);
	}

	private SensorSettingsAdapterListener sensorSettingsAdapterListener = new SensorSettingsAdapterListener() {

		@Override
		public void onItemChanged(final SensorSettingsView sender) {

			new Thread() {

				@Override
				public void run() {

					Sensor sensor = sender.getSensor();

					BrewMessage message = new BrewMessage();
					message.setGuaranteeId(UUID.randomUUID().toString());
					message.setMethod(SOCKET_METHOD.SENSOR_SETTINGS_UPDATE);

					BrewData data = new BrewData();

					List<SensorSettingsTransport> sensorTransports = new ArrayList<SensorSettingsTransport>();

					SensorSettingsTransport sensorTransport = new SensorSettingsTransport(
							sensor);

					sensorTransports.add(sensorTransport);

					data.setSensorSettings(sensorTransports);

					message.setData(data);

					SocketManager.sendMessage(message);

					super.run();
				}

			}.start();

		}

	};

	private SocketManagerListener socketListener = new SocketManagerListener() {

		@Override
		public void onConfirmationAction(int pendingConfirmation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect() {
			sensors.clear();
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onConnect() {
			subscribe();
		}

		@Override
		public void onConnectFailed() {

		}

		@Override
		public void onData(BrewData brewData) {

		}

		@Override
		public void onPingReturned(long time) {

		}

		@Override
		public void onAuthResult(boolean success) {

		}

		@Override
		public void onSubscribeResult(SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION permission) {

		}

		@Override
		public void onLogReceived(final LogMessage logMessage) {

		}

		@Override
		public void onSensorSettingsReceived(BrewData brewData) {

			synchronized (sensors) {
				for (SensorSettingsTransport sst : brewData.getSensorSettings()) {

					Sensor sensor = null;
					for (Sensor s : sensors) {

						if (s.getSensorName() == sst.getSensorName()) {
							sensor = s;
							break;
						}
					}

					if (sensor == null) {
						sensor = new Sensor();
						sensors.add(sensor);
					}

					sensor.populateFromSettingsTransport(sst);

				}
			}

			handler.post(new Runnable() {

				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}

			});
		}

	};

	private void subscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.SUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.SENSOR_SETTINGS);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	private void unsubscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.SENSOR_SETTINGS);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	@Override
	protected void onPause() {
		super.onPause();

		SocketManager.unregisterSocketManagerListener(socketListener);

		unsubscribe();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SocketManager.registerSocketManagerListener(socketListener);

		if (SocketManager.isConnected()) {

			subscribe();

		}
	}
}
