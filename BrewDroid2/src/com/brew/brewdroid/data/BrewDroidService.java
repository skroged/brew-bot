package com.brew.brewdroid.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.brew.brewdroid.HomeScreen;
import com.brew.brewdroid.R;
import com.brew.brewdroid.data.BrewDroidContentProvider.BulkInsertListener;
import com.brew.brewdroid.data.BrewDroidContentProvider.DeleteListener;
import com.brew.brewdroid.socket.SocketManager;
import com.brew.brewdroid.socket.SocketManager.SocketManagerListener;
import com.brew.brewdroid.util.BrewDroidUtil;
import com.brew.lib.model.ApkPacket;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.DeviceAddress;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SENSOR_TYPE;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SensorSettingsTransport;
import com.brew.lib.model.ServerInfo;
import com.brew.lib.model.SwitchTransport;
import com.brew.lib.model.User;

/**
 * This is an example of implementing an application service that can run in the
 * "foreground". It shows how to code this to work well by using the improved
 * Android 2.0 APIs when available and otherwise falling back to the original
 * APIs. Yes: you can take this exact code, compile it against the Android 2.0
 * SDK, and it will against everything down to Android 1.0.
 */
public class BrewDroidService extends Service {
	public static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
	// public static final String ACTION_BACKGROUND =
	// "com.example.android.apis.BACKGROUND";

	private static final Class<?>[] mSetForegroundSignature = new Class[] { boolean.class };
	private static final Class<?>[] mStartForegroundSignature = new Class[] {
			int.class, Notification.class };
	private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };

	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	private Handler handler;

	public static String ACTION_CLOSE = "actionClose";

	public static String ACTION_UPDATE_SENSOR_SETTING = "actionUpdateSensorSetting";
	public static String ACTION_SUBSCRIBE = "actionSubscribe";
	public static String ACTION_UNSUBSCRIBE = "actionUnsubscribe";
	public static String ACTION_LOGIN = "actionLogin";
	public static String ACTION_LOGOUT = "actionLogout";
	public static String ACTION_AUTH_RESULT = "actionAuthResult";
	public static String ACTION_SWITCH_UPDATE = "actionSwitchUpdate";
	public static String ACTION_CONNECT_CHANGED = "actionConnectionChanged";
	public static String ACTION_PING_RESULT = "actionPingResult";

	public static String BUNDLE_CHANNEL = "bundleChannel";
	public static String BUNDLE_USERNAME = "bundleUsername";
	public static String BUNDLE_PASSWORD = "bundlePassword";
	public static String BUNDLE_AUTH_RESULT = "bundleAuthResult";
	public static String BUNDLE_SWITCH_ID = "bundleSwitchId";
	public static String BUNDLE_SWITCH_VALUE = "bundleSwitchValue";

	public static String BUNDLE_SENSOR_ID = "bundleSensorId";
	public static String BUNDLE_SENSOR_CALIBRATION_INPUT_LOW = "bundleSensorCalibrationInputLow";
	public static String BUNDLE_SENSOR_CALIBRATION_INPUT_HIGH = "bundleSensorCalibrationInputHigh";
	public static String BUNDLE_SENSOR_CALIBRATION_OUTPUT_LOW = "bundleSensorCalibrationOutputLow";
	public static String BUNDLE_SENSOR_CALIBRATION_OUTPUT_HIGH = "bundleSensorCalibrationOutputHigh";
	public static String BUNDLE_SENSOR_ADDRESS = "bundleSensorAddress";

	public static String BUNDLE_PING_TIME = "pingTime";

	private Timer mPingTimer;

	void invokeMethod(Method method, Object[] args) {
		try {
			mStartForeground.invoke(this, mStartForegroundArgs);
		} catch (InvocationTargetException e) {
			// Should not happen.
			Log.w("ApiDemos", "Unable to invoke method", e);
		} catch (IllegalAccessException e) {
			// Should not happen.
			Log.w("ApiDemos", "Unable to invoke method", e);
		}
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			invokeMethod(mStartForeground, mStartForegroundArgs);
			return;
		}

		// Fall back on the old API.
		mSetForegroundArgs[0] = Boolean.TRUE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
		mNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat(int id) {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("ApiDemos", "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("ApiDemos", "Unable to invoke stopForeground", e);
			}
			return;
		}

		// Fall back on the old API. Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNM.cancel(id);
		mSetForegroundArgs[0] = Boolean.FALSE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
	}

	@Override
	public void onCreate() {

		SharedPreferences sp = getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);
		String host = sp.getString("HOST", "");

		if (TextUtils.isEmpty(host)) {
			Toast.makeText(this, "Invalid host", Toast.LENGTH_SHORT).show();
			stopSelf();
		}

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
			return;
		}
		try {
			mSetForeground = getClass().getMethod("setForeground",
					mSetForegroundSignature);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
					"OS doesn't have Service.startForeground OR Service.setForeground!");
		}

		SocketManager.registerSocketManagerListener(socketManagerListener);

		handler = new Handler();
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		// Make sure our notification is gone.
		stopForegroundCompat(R.string.foreground_service_started);
		closeSocket();
		SocketManager.unregisterSocketManagerListener(socketManagerListener);
	}

	// This is the old onStart method that will be called on the pre-2.0
	// platform. On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void handleCommand(final Intent intent) {

		Runnable r = new Runnable() {

			@Override
			public void run() {

				if (intent == null || intent.getAction() == null) {
					return;
				}

				if (intent.getAction().equals(ACTION_FOREGROUND)) {
					openSocket();
				} else if (intent.getAction().equals(ACTION_CLOSE)) {
					stopSelf();
				}

				else if (intent.getAction().equals(ACTION_LOGIN)) {

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					Bundle bundle = intent.getExtras();

					String username = bundle.getString(BUNDLE_USERNAME);
					String password = bundle.getString(BUNDLE_PASSWORD);

					BrewMessage message = new BrewMessage();
					message.setMethod(SOCKET_METHOD.LOGIN_USER);
					BrewData data = new BrewData();
					message.setData(data);
					message.setGuaranteeId(UUID.randomUUID().toString());
					List<User> users = new ArrayList<User>();
					data.setUsers(users);
					User user = new User();
					users.add(user);
					user.setUsername(username);
					user.setPassword(password);

					SocketManager.sendMessage(message);

					BrewDroidUtil.saveUser(BrewDroidService.this, user);

				} else if (intent.getAction().equals(ACTION_LOGOUT)) {

					BrewDroidUtil.deleteUser(BrewDroidService.this);

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					BrewMessage message = new BrewMessage();
					message.setMethod(SOCKET_METHOD.LOGOUT_USER);

					SocketManager.sendMessage(message);

					Intent i = new Intent();
					i.setAction(ACTION_LOGOUT);
					sendBroadcast(i);

				}

				else if (intent.getAction().equals(ACTION_SUBSCRIBE)) {

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					Bundle bundle = intent.getExtras();

					String channelStr = bundle.getString(BUNDLE_CHANNEL);
					SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);

					BrewMessage message = new BrewMessage();
					message.setMethod(SOCKET_METHOD.SUBSCRIBE);
					message.setChannel(channel);
					message.setGuaranteeId(UUID.randomUUID().toString());

					SocketManager.sendMessage(message);

				} else if (intent.getAction().equals(ACTION_UNSUBSCRIBE)) {

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					Bundle bundle = intent.getExtras();

					String channelStr = bundle.getString(BUNDLE_CHANNEL);
					SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);

					BrewMessage message = new BrewMessage();
					message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
					message.setChannel(channel);
					message.setGuaranteeId(UUID.randomUUID().toString());

					SocketManager.sendMessage(message);

				}

				else if (intent.getAction().equals(ACTION_SWITCH_UPDATE)) {

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					Bundle bundle = intent.getExtras();

					int switchId = bundle.getInt(BUNDLE_SWITCH_ID);
					boolean isOn = bundle.getBoolean(BUNDLE_SWITCH_VALUE);

					SwitchTransport st = new SwitchTransport();
					st.setSwitchId(switchId);
					st.setSwitchValue(isOn);

					BrewMessage message = new BrewMessage();
					message.setGuaranteeId(UUID.randomUUID().toString());
					message.setMethod(SOCKET_METHOD.SWITCH_UPDATE);
					BrewData data = new BrewData();
					message.setData(data);
					List<SwitchTransport> switches = new ArrayList<SwitchTransport>();
					switches.add(st);
					data.setSwitchTransports(switches);
					SocketManager.sendMessage(message);

				}

				else if (intent.getAction()
						.equals(ACTION_UPDATE_SENSOR_SETTING)) {

					if (!SocketManager.isConnected()) {
						sendConnectionErrorBroadcast();
						return;
					}

					Bundle bundle = intent.getExtras();

					int id = bundle.getInt(BUNDLE_SENSOR_ID);
					float inputLow = bundle
							.getFloat(BUNDLE_SENSOR_CALIBRATION_INPUT_LOW);
					float inputHigh = bundle
							.getFloat(BUNDLE_SENSOR_CALIBRATION_INPUT_HIGH);
					float outputLow = bundle
							.getFloat(BUNDLE_SENSOR_CALIBRATION_OUTPUT_LOW);
					float outputHigh = bundle
							.getFloat(BUNDLE_SENSOR_CALIBRATION_OUTPUT_HIGH);
					String address = bundle.getString(BUNDLE_SENSOR_ADDRESS);

					BrewMessage message = new BrewMessage();
					message.setGuaranteeId(UUID.randomUUID().toString());
					message.setMethod(SOCKET_METHOD.SENSOR_SETTINGS_UPDATE);

					BrewData data = new BrewData();

					List<SensorSettingsTransport> sensorTransports = new ArrayList<SensorSettingsTransport>();

					SensorSettingsTransport sensorTransport = new SensorSettingsTransport();

					sensorTransport.setInputLow(inputLow);
					sensorTransport.setInputHigh(inputHigh);
					sensorTransport.setOutputLow(outputLow);
					sensorTransport.setOutputHigh(outputHigh);
					sensorTransport.setAddress(address);
					sensorTransport.setSensorId(id);

					sensorTransports.add(sensorTransport);

					data.setSensorSettings(sensorTransports);

					message.setData(data);

					SocketManager.sendMessage(message);

				}

			}

		};

		new Thread(r).start();

	}

	private void sendConnectionErrorBroadcast() {

	}

	private void openSocket() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				SocketManager.connect(BrewDroidService.this);
			}

		});

	}

	private void closeSocket() {
		if (SocketManager.isConnected()) {
			SocketManager.disconnect();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private SocketManagerListener socketManagerListener = new SocketManagerListener() {

		@Override
		public void onConfirmationAction(int pendingConfirmation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSubscribeResult(SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION permission) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect(boolean disconnectByClient) {

			Intent i = new Intent(ACTION_CONNECT_CHANGED);
			sendBroadcast(i);
			stopSelf();

			if (!disconnectByClient) {
				Intent intent = new Intent(BrewDroidService.ACTION_FOREGROUND);
				intent.setClass(BrewDroidService.this, BrewDroidService.class);
				startService(intent);
			}

			if (mPingTimer != null) {
				mPingTimer.cancel();
				mPingTimer.purge();
			}
		}

		@Override
		public void onConnect() {

			Intent i = new Intent(ACTION_CONNECT_CHANGED);
			sendBroadcast(i);

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					BrewDroidService.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Brew Service")
					.setContentText("Connected to Brew Bot");

			PendingIntent contentIntent = PendingIntent.getActivity(
					BrewDroidService.this, 0, new Intent(BrewDroidService.this,
							HomeScreen.class), 0);

			mBuilder.setContentIntent(contentIntent);

			Notification notification = mBuilder.build();

			startForeground(2, notification);

			loginSavedUser();

			if (mPingTimer != null) {
				mPingTimer.cancel();
				mPingTimer.purge();
			}

			mPingTimer = new Timer();
			mPingTimer.scheduleAtFixedRate(new PingTimerTask(), 0, 2000);
		}

		@Override
		public void onConnectFailed() {

			String msg = "There was an error trying to connect to the Brew Bot!";

			Intent closeIntent = new Intent(BrewDroidService.this,
					BrewDroidService.class);
			closeIntent.setAction(ACTION_CLOSE);
			PendingIntent piClose = PendingIntent.getService(
					BrewDroidService.this, 0, closeIntent, 0);

			Intent retryIntent = new Intent(BrewDroidService.this,
					BrewDroidService.class);
			retryIntent.setAction(ACTION_FOREGROUND);
			PendingIntent piRetry = PendingIntent.getService(
					BrewDroidService.this, 0, retryIntent, 0);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					BrewDroidService.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Brew Service")
					.setContentText("Failed to connect to Brew Bot");

			Intent resultIntent = new Intent(BrewDroidService.this,
					HomeScreen.class);

			PendingIntent contentIntent = PendingIntent.getActivity(
					BrewDroidService.this, 0, resultIntent, 0);

			builder.setContentIntent(contentIntent);

			builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
			builder.addAction(R.drawable.ic_launcher, "Close", piClose);
			builder.addAction(R.drawable.ic_launcher, "Retry", piRetry);

			Notification notification = builder.build();

			startForeground(2, notification);
		}

		@Override
		public void onData(BrewData brewData) {

			if (brewData.getSensors() != null) {

				BrewDroidContentProvider.insertSensors(BrewDroidService.this,
						brewData.getSensors(), null);

			}

			if (brewData.getSensorTransports() != null) {

				BrewDroidContentProvider.updateSensors(null,
						BrewDroidService.this, brewData.getSensorTransports());

			}

			if (brewData.getSwitches() != null) {

				BrewDroidContentProvider.insertSwitches(BrewDroidService.this,
						brewData.getSwitches(), null);

			}

			if (brewData.getSwitchTransports() != null) {

				BrewDroidContentProvider.updateSwitches(null,
						BrewDroidService.this, brewData.getSwitchTransports());

			}

		}

		@Override
		public void onConnecting(long timer, String port, String host) {

			String msg = "Connecting to Brew Bot ..." + timer + "\nHost: "
					+ host + "\nPort: " + port;

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					BrewDroidService.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Brew Service")
					.setContentText("Connecting to Brew Bot ..." + timer);

			builder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));

			PendingIntent contentIntent = PendingIntent.getActivity(
					BrewDroidService.this, 0, new Intent(BrewDroidService.this,
							HomeScreen.class), 0);

			builder.setContentIntent(contentIntent);

			Notification notification = builder.build();

			startForeground(2, notification);
		}

		@Override
		public void onPingReturned(long time) {
			// TODO Auto-generated method stub
			Intent i = new Intent(ACTION_PING_RESULT);
			i.putExtra(BUNDLE_PING_TIME, time);
			sendBroadcast(i);

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					BrewDroidService.this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Brew Service")
					.setContentText(
							"Connected to Brew Bot.\n Ping: " + time + " ms.");

			PendingIntent contentIntent = PendingIntent.getActivity(
					BrewDroidService.this, 0, new Intent(BrewDroidService.this,
							HomeScreen.class), 0);

			mBuilder.setContentIntent(contentIntent);

			Notification notification = mBuilder.build();

			startForeground(2, notification);

		}

		@Override
		public void onAuthResult(final boolean success, final User user) {

			final List<User> users = new ArrayList<User>();
			users.add(user);

			BulkInsertListener insertListener = new BulkInsertListener() {

				@Override
				public void onComplete(int count) {

					for (User user : users) {
						if (user.getPermissions() != null) {

							BrewDroidContentProvider.insertPermissions(
									BrewDroidService.this,
									user.getPermissions(), null);
						}
					}

				}

			};

			BrewDroidContentProvider.insertUsers(BrewDroidService.this, users,
					insertListener);

			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(BrewDroidService.this,
							"Login " + (success ? "success" : "fail"),
							Toast.LENGTH_SHORT).show();

					if (success) {
						BrewDroidUtil.setUserId(BrewDroidService.this,
								user.getId());
					} else {
						BrewDroidUtil.deleteUser(BrewDroidService.this);
					}

					Intent intent = new Intent(ACTION_AUTH_RESULT);
					intent.putExtra(BUNDLE_AUTH_RESULT, success);
					sendBroadcast(intent);

				}

			});

		}

		@Override
		public void onLogReceived(LogMessage logMessage) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorSettingsReceived(final BrewData brewData) {

			if (brewData.getOneWireAddresses() != null) {

				DeleteListener deleteListener = new DeleteListener() {

					@Override
					public void onComplete(int count) {

						List<DeviceAddress> deviceAddresses = new ArrayList<DeviceAddress>();

						for (String address : brewData.getOneWireAddresses()) {
							DeviceAddress deviceAddress = new DeviceAddress();
							deviceAddress.setAddress(address);
							deviceAddress.setType(SENSOR_TYPE.ONE_WIRE);
							deviceAddresses.add(deviceAddress);
						}

						BulkInsertListener insertListener = null;
						BrewDroidContentProvider.insertDeviceAddresses(
								BrewDroidService.this, deviceAddresses,
								insertListener);

					}

				};

				BrewDroidContentProvider.deleteAllDeviceAddresses(
						BrewDroidService.this, deleteListener);

			}

			List<SensorSettingsTransport> sensorSettings = brewData
					.getSensorSettings();

			if (sensorSettings != null) {
				BrewDroidContentProvider.updateSensorSettings(null,
						BrewDroidService.this, sensorSettings);
			}

		}

		@Override
		public void onUsersReceived(BrewData brewData) {
			if (brewData != null && brewData.getUsers() != null) {

				final List<User> users = brewData.getUsers();

				BulkInsertListener insertListener = new BulkInsertListener() {

					@Override
					public void onComplete(int count) {

						for (User user : users) {
							if (user.getPermissions() != null) {

								BrewDroidContentProvider.insertPermissions(
										BrewDroidService.this,
										user.getPermissions(), null);
							}
						}

					}

				};
				BrewDroidContentProvider.insertUsers(BrewDroidService.this,
						users, insertListener);

			}

		}

		@Override
		public void onServerInfoReceived(ServerInfo info) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onApkPacketReceived(ApkPacket packet) {
			// TODO Auto-generated method stub

		}

	};

	private void loginSavedUser() {
		User user = BrewDroidUtil.getSavedUser(BrewDroidService.this);
		if (user == null) {
			return;
		}
		Intent intent = new Intent(BrewDroidService.ACTION_LOGIN);
		intent.putExtra(BrewDroidService.BUNDLE_USERNAME, user.getUsername());
		intent.putExtra(BrewDroidService.BUNDLE_PASSWORD, user.getPassword());
		intent.setClass(BrewDroidService.this, BrewDroidService.class);
		BrewDroidService.this.startService(intent);
	}

	private static class PingTimerTask extends TimerTask {

		@Override
		public void run() {
			BrewMessage message = new BrewMessage();
			message.setMethod(SOCKET_METHOD.PING);
			message.setGuaranteeId(UUID.randomUUID().toString());
			SocketManager.sendMessage(message);
		}

	}

}