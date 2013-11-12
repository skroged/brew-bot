package com.brew.brewdroid.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.brew.brewdroid.HomeScreen;
import com.brew.brewdroid.R;
import com.brew.brewdroid.socket.SocketManager;
import com.brew.brewdroid.socket.SocketManager.SocketManagerListener;
import com.brew.brewdroid.util.BrewDroidUtil;
import com.brew.lib.model.ApkPacket;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.ServerInfo;
import com.brew.lib.model.User;
import com.brew.lib.util.BrewHelper;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.

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

	public static String ACTION_SUBSCRIBE = "actionSubscribe";
	public static String ACTION_UNSUBSCRIBE = "actionUnsubscribe";
	public static String ACTION_LOGIN = "actionLogin";
	public static String ACTION_AUTH_RESULT = "actionAuthResult";

	public static String BUNDLE_CHANNEL = "bundleChannel";
	public static String BUNDLE_USERNAME = "bundleUsername";
	public static String BUNDLE_PASSWORD = "bundlePassword";
	public static String BUNDLE_AUTH_RESULT = "bundleAuthResult";

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
	}

	@Override
	public void onDestroy() {
		// Make sure our notification is gone.
		stopForegroundCompat(R.string.foreground_service_started);
		SocketManager.disconnect();
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

	void handleCommand(Intent intent) {
		if (ACTION_FOREGROUND.equals(intent.getAction())) {
			// In this sample, we'll use the same text for the ticker and the
			// expanded notification
			CharSequence text = getText(R.string.foreground_service_started);

			// Set the icon, scrolling text and timestamp
			Notification notification = new Notification(
					R.drawable.ic_launcher, text, System.currentTimeMillis());

			// The PendingIntent to launch our activity if the user selects this
			// notification
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, HomeScreen.class), 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this,
					getText(R.string.service_label), text, contentIntent);

			startForegroundCompat(R.string.foreground_service_started,
					notification);

			openSocket();
		}

		else if (intent.getAction().equals(ACTION_LOGIN)) {

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
			String md5 = BrewHelper.md5(password);
			user.setPassword(md5);

			SocketManager.sendMessage(message);

			BrewDroidUtil.saveUser(BrewDroidService.this, user);

		} else if (intent.getAction().equals(ACTION_SUBSCRIBE)) {

			Bundle bundle = intent.getExtras();

			String channelStr = bundle.getString(BUNDLE_CHANNEL);
			SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);

			BrewMessage message = new BrewMessage();
			message.setMethod(SOCKET_METHOD.SUBSCRIBE);
			message.setChannel(channel);
			message.setGuaranteeId(UUID.randomUUID().toString());

			SocketManager.sendMessage(message);

		} else if (intent.getAction().equals(ACTION_UNSUBSCRIBE)) {

			Bundle bundle = intent.getExtras();

			String channelStr = bundle.getString(BUNDLE_CHANNEL);
			SOCKET_CHANNEL channel = SOCKET_CHANNEL.valueOf(channelStr);

			BrewMessage message = new BrewMessage();
			message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
			message.setChannel(channel);
			message.setGuaranteeId(UUID.randomUUID().toString());

			SocketManager.sendMessage(message);

		}
	}

	private void openSocket() {
		SocketManager.connect(this);
	}

	private void closeSocket() {
		SocketManager.disconnect();
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
		public void onDisconnect() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnect() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnectFailed() {
			// TODO Auto-generated method stub

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

		}

		@Override
		public void onPingReturned(long time) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAuthResult(boolean success) {

			// Toast.makeText(BrewDroidService.this, "Login success:" + success,
			// Toast.LENGTH_SHORT).show();

			Intent intent = new Intent(ACTION_AUTH_RESULT);
			intent.putExtra(BUNDLE_AUTH_RESULT, success);
			sendBroadcast(intent);

		}

		@Override
		public void onLogReceived(LogMessage logMessage) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorSettingsReceived(BrewData brewData) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUsersReceived(BrewData brewData) {
			// TODO Auto-generated method stub

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

}