package com.brew.brewdroid.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.brew.brewdroid.util.BrewDroidUtil;
import com.brew.lib.model.ApkPacket;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.GsonHelper;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.ServerInfo;
import com.brew.lib.model.User;
import com.google.gson.reflect.TypeToken;

public class SocketManager {

	public static String SENSOR_UPDATE_ACTION = "sensorUpdateAction";

	public static final int PORT_NUMBER = 4444;

	private static SocketConnection socketConnection;
	private static Handler handler;

	private static SocketConnectionListener clientSocketListener = new SocketConnectionListener() {

		@Override
		public void onSocketReady(SocketConnection socketConnection) {
			// identify();

			synchronized (socketManagerListeners) {
				for (SocketManagerListener listener : socketManagerListeners) {
					listener.onConnect();
				}
			}

			BrewMessage message = new BrewMessage();
			message.setMethod(SOCKET_METHOD.REQUEST_SERVER_INFO);
			message.setGuaranteeId(UUID.randomUUID().toString());
			sendMessage(message);
		}

		@Override
		public void onSocketDisconnected(SocketConnection socketConnection) {
			SocketManager.socketConnection = null;
			synchronized (socketManagerListeners) {

				for (SocketManagerListener listener : socketManagerListeners) {
					listener.onDisconnect();
				}
			}

		}

		@Override
		public void onSocketError(SocketConnection socketConnection) {
			synchronized (socketManagerListeners) {

				for (SocketManagerListener listener : socketManagerListeners) {
					listener.onConnectFailed();
				}
			}
		}

	};

	private static Context context;

	public static boolean isConnected() {
		return socketConnection != null;
	}

	public static void sendMessage(BrewMessage message) {

		String json = GsonHelper.getGson().toJson(message);

		if (message.getGuaranteeId() != null) {
			new BrewMessageGuarantee(message);
		}

		socketConnection.sendMessage(json);
	}

	// private static boolean looping;

	// private static boolean enableConnectionReconnect;

	// private static void doConnectionLoop() {
	//
	// looping = true;
	//
	// new Handler().postDelayed(new Runnable() {
	//
	// @Override
	// public void run() {
	//
	// Log.i("JOSH", "connection loop executing");
	//
	// if (enableConnectionReconnect) {
	// doConnectionLoop();
	//
	// if (socketConnection == null)
	// connect(context);
	// }
	//
	// }
	//
	// }, 3000);
	// }

	private static void startConnectTimer(final String serverHost) {

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {

				Log.i("JOSH", "connection loop executing");

				if (attemptingToConnect) {
					startConnectTimer(serverHost);

					long timeElapsed = System.currentTimeMillis()
							- socketStartConnectTime;
					long timeLeft = (TIMEOUT - timeElapsed) / 1000;

					synchronized (socketManagerListeners) {
						for (SocketManagerListener listener : socketManagerListeners) {
							listener.onConnecting(timeLeft, PORT_NUMBER + "'",
									serverHost);
						}
					}
				}

			}

		}, 1000);

	}

	private static boolean attemptingToConnect;

	public static void disconnect() {

		if (socketConnection != null) {
			socketConnection.endThread();
		}
		socketConnection = null;
		attemptingToConnect = false;
		// enableConnectionReconnect = false;

	}

	// private static int socketConnectTimer = 0;
	private static long socketStartConnectTime;
	private static final int TIMEOUT = 10000;

	// private static String serverHost;

	public static void connect(final Context context) {

		if (attemptingToConnect) {
			return;
		}

		socketStartConnectTime = System.currentTimeMillis();

		attemptingToConnect = true;

		// enableConnectionReconnect = true;

		// if (!looping)
		// doConnectionLoop();

		if (handler == null) {
			handler = new Handler();
		}

		SocketManager.context = context;

		new Thread() {

			@Override
			public void run() {

				Socket clientSocket = null;

				try {

					Log.i("JOSH", "attempting to connect socket on "
							+ PORT_NUMBER);

					SharedPreferences sp = context.getSharedPreferences(
							"SETTINGS", Context.MODE_PRIVATE);

					final String serverHost = sp.getString("BREW_SERVER_IP",
							"192.168.0.183");

					handler.post(new Runnable() {

						@Override
						public void run() {
							startConnectTimer(serverHost);
						}

					});

					clientSocket = new Socket();
					SocketAddress remoteAddr = new InetSocketAddress(
							serverHost, PORT_NUMBER);
					clientSocket.connect(remoteAddr, TIMEOUT);

					// clientSocket = new Socket(serverHost, PORT_NUMBER);

					if (socketConnection != null) {
						// connected...

						synchronized (socketManagerListeners) {
							for (SocketManagerListener listener : socketManagerListeners) {
								listener.onConnect();
							}
						}

						return;
					}
					Log.i("JOSH", "socket connected on " + PORT_NUMBER);

					socketConnection = new SocketConnection(clientSocket,
							clientSocketListener);
					
					attemptingToConnect = false;

					// User user = BrewDroidUtil.getSavedUser(context);
					//
					// BrewMessage loginMessage = new BrewMessage();
					// loginMessage.setMethod(SOCKET_METHOD.LOGIN_USER);
					// BrewData data = new BrewData();
					// loginMessage.setData(data);
					// loginMessage.setGuaranteeId(UUID.randomUUID().toString());
					// List<User> users = new ArrayList<User>();
					// data.setUsers(users);
					// users.add(user);
					//
					// SocketManager.sendMessage(loginMessage);

				} catch (IOException e) {
					Log.i("JOSH", "socket error!");
					synchronized (socketManagerListeners) {

						for (SocketManagerListener listener : socketManagerListeners) {
							listener.onConnectFailed();
						}
					}

					disconnect();

					e.printStackTrace();
				}
				super.run();
			}

		}.start();

	}

	public static void sendMessage(String message) {
		socketConnection.sendMessage(message);
	}

	private static class SocketConnection {

		private SocketConnectionListener clientSocketListener;
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;

		public SocketConnection(Socket socket,
				SocketConnectionListener clientSocketListener) {
			this.socket = socket;
			this.clientSocketListener = clientSocketListener;

			startThread();
		}

		public void sendMessage(String message) {
			if (out != null) {
				synchronized (out) {
					out.println(message);
				}
			}
		}

		private void processInput(String inputLine) {

			Type jsonType = new TypeToken<BrewMessage>() {
			}.getType();

			BrewMessage message = GsonHelper.getGson().fromJson(inputLine,
					jsonType);

			switch (message.getMethod()) {

			case SEND_ANDROID_APK:

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onApkPacketReceived(message.getApkPacket());
					}
				}

				break;

			case SEND_SERVER_INFO:

				if (message.getServerInfo() == null) {
					Log.i("JOSH", "bad server info packet");
					return;
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onServerInfoReceived(message.getServerInfo());
					}
				}

				break;

			case UPDATE_USERS:

				if (message.getData() == null) {
					Log.i("JOSH", "bad users packet");
					return;
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onUsersReceived(message.getData());
					}
				}

				break;

			case SENSOR_SETTINGS_UPDATE:

				if (message.getData() == null) {
					Log.i("JOSH", "bad sensor settings packet");
					return;
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onSensorSettingsReceived(message.getData());
					}
				}

				break;

			case LOG:

				if (message.getLogMessage() == null) {
					if (message.getSuccess() == null) {
						Log.i("JOSH", "bad log packet");
						return;
					}
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onLogReceived(message.getLogMessage());
					}
				}

				break;

			case SUBSCRIBE_RESULT:

				SOCKET_CHANNEL channel = message.getChannel();
				CHANNEL_PERMISSION permission = message.getChannelPermission();

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onSubscribeResult(channel, permission);
					}
				}

				break;

			case LOGIN_RESULT:

				if (message.getSuccess() == null) {
					Log.i("JOSH", "bad login result packet");
					return;
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onAuthResult(message.getSuccess());
					}
				}

				break;

			case REGISTER_RESULT:

				if (message.getSuccess() == null) {
					Log.i("JOSH", "bad register result packet");
					return;
				}

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onAuthResult(message.getSuccess());
					}
				}

				break;

			case CONFIRM_MESSAGE:

				String confirmId = message.getConfirmId();

				BrewMessageGuarantee bmg = brewMessageGuarantees.get(confirmId);

				if (bmg != null) {

					bmg.confirm();

				}

				break;

			case DATA_UPDATE:

				synchronized (socketManagerListeners) {

					for (SocketManagerListener listener : socketManagerListeners) {
						listener.onData(message.getData());
					}
				}

				break;
			}

		}

		private void endThread() {
			try {
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void startThread() {

			new Thread() {

				@Override
				public void run() {

					try {
						setupReadWrite();

						String inputLine = null;

						while ((inputLine = in.readLine()) != null) {
							processInput(inputLine);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					Log.i("JOSH", "socket disconnected");

					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out.close();

					clientSocketListener
							.onSocketDisconnected(SocketConnection.this);

				}

				private void setupReadWrite() throws IOException {

					out = new PrintWriter(socket.getOutputStream(), true);

					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));

					clientSocketListener.onSocketReady(SocketConnection.this);
				}

			}.start();

		}

	}

	private static Map<String, BrewMessageGuarantee> brewMessageGuarantees = new Hashtable<String, BrewMessageGuarantee>();

	private static class BrewMessageGuarantee {

		int confirmWaitCount;
		private BrewMessage brewMessage;
		private boolean confirmed;
		private long start;

		public BrewMessageGuarantee(BrewMessage brewMessage) {

			this.brewMessage = brewMessage;

			start = System.currentTimeMillis();

			brewMessageGuarantees.put(brewMessage.getGuaranteeId(), this);

			if (brewMessage.getMethod() == SOCKET_METHOD.PING) {
				return;
			}

			synchronized (socketManagerListeners) {

				for (SocketManagerListener listener : socketManagerListeners) {

					listener.onConfirmationAction(++confirmWaitCount);

				}
			}

			handler.post(new Runnable() {

				@Override
				public void run() {
					scheduleRetry();
				}

			});

		}

		public void confirm() {

			long confirmTime = System.currentTimeMillis() - start;

			Log.i("JOSH", "confirmed " + brewMessage.getMethod() + " in "
					+ confirmTime + " milliseconds");

			confirmed = true;

			synchronized (socketManagerListeners) {

				brewMessageGuarantees.remove(brewMessage.getGuaranteeId());

				for (SocketManagerListener listener : socketManagerListeners) {

					if (brewMessage.getMethod() == SOCKET_METHOD.PING) {

						listener.onPingReturned(confirmTime);

					} else {

						listener.onConfirmationAction(--confirmWaitCount);

					}
				}
			}
		}

		private void scheduleRetry() {

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					if (!confirmed) {
						sendMessage(brewMessage);
					}

				}

			}, 2000);
		}

	}

	public static void registerSocketManagerListener(
			SocketManagerListener listener) {
		synchronized (socketManagerListeners) {
			socketManagerListeners.add(listener);
		}
	}

	public static void unregisterSocketManagerListener(
			SocketManagerListener listener) {
		synchronized (socketManagerListeners) {
			socketManagerListeners.remove(listener);
		}
	}

	public static List<SocketManagerListener> socketManagerListeners = Collections
			.synchronizedList(new ArrayList<SocketManagerListener>());

	public static interface SocketManagerListener {

		public void onConfirmationAction(int pendingConfirmation);

		public void onSubscribeResult(SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION permission);

		public void onDisconnect();

		public void onConnect();

		public void onConnecting(long timer, String port, String host);

		public void onConnectFailed();

		public void onData(BrewData brewData);

		public void onPingReturned(long time);

		public void onAuthResult(boolean success);

		public void onLogReceived(LogMessage logMessage);

		public void onSensorSettingsReceived(BrewData brewData);

		public void onUsersReceived(BrewData brewData);

		public void onServerInfoReceived(ServerInfo info);

		public void onApkPacketReceived(ApkPacket packet);
	}

	public static interface SocketConnectionListener {
		public void onSocketReady(SocketConnection socketConnection);

		public void onSocketDisconnected(SocketConnection socketConnection);

		public void onSocketError(SocketConnection socketConnection);
	}

}
