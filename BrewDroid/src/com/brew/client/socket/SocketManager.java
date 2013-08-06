package com.brew.client.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
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

import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.ClientIdentifier;
import com.brew.lib.model.GsonHelper;
import com.brew.lib.model.SOCKET_METHOD;
import com.google.gson.reflect.TypeToken;

public class SocketManager {

	public static String SENSOR_UPDATE_ACTION = "sensorUpdateAction";

	public static final int PORT_NUMBER = 4444;
	// private static String serverHost = "10.0.3.2";
	// private static String serverHost = "192.168.0.193";

	// private static String serverHost = "67.162.131.146";

	// private static String serverHost = "skroged.zapto.org";

	private static SocketConnection socketConnection;
	private static Handler handler;
	// private static List<SocketConnection> sockets = Collections
	// .synchronizedList(new ArrayList<SocketConnection>());

	private static SocketConnectionListener clientSocketListener = new SocketConnectionListener() {

		@Override
		public void onSocketReady(SocketConnection socketConnection) {
			identify();

			synchronized (socketManagerListeners) {
				for (SocketManagerListener listener : socketManagerListeners) {
					listener.onConnect();
				}
			}
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

	};

	private static Context context;

	private static void identify() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.IDENTIFY_CLIENT);
		message.setClientIdentifier(new ClientIdentifier(UUID.randomUUID()
				.toString()));
		message.setGuaranteeId(UUID.randomUUID().toString());

		sendMessage(message);

	}

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

	private static boolean looping;

	private static void doConnectionLoop() {

		looping = true;

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doConnectionLoop();

				if (socketConnection == null)
					connect(context);

			}

		}, 3000);
	}

	public static void connect(final Context context) {

		if (!looping)
			doConnectionLoop();

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

					String serverHost = sp.getString("BREW_SERVER_IP",
							"skroged.zapto.org");

					clientSocket = new Socket(serverHost, PORT_NUMBER);

					if (socketConnection != null) {
						// already connected...
						return;
					}
					Log.i("JOSH", "socket connected on " + PORT_NUMBER);

					socketConnection = new SocketConnection(clientSocket,
							clientSocketListener);

				} catch (IOException e) {
					Log.i("JOSH", "socket error!");
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
			synchronized (out) {
				out.println(message);
			}
		}

		private void processInput(String inputLine) {

			Type jsonType = new TypeToken<BrewMessage>() {
			}.getType();

			BrewMessage message = GsonHelper.getGson().fromJson(inputLine,
					jsonType);

			switch (message.getMethod()) {

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

	public static int getPendingConfirmationCount() {
		return brewMessageGuarantees.size();
	}

	private static Map<String, BrewMessageGuarantee> brewMessageGuarantees = new Hashtable<String, BrewMessageGuarantee>();

	private static class BrewMessageGuarantee {

		private BrewMessage brewMessage;
		private boolean confirmed;
		private long start;

		public BrewMessageGuarantee(BrewMessage brewMessage) {

			this.brewMessage = brewMessage;

			brewMessageGuarantees.put(brewMessage.getGuaranteeId(), this);

			start = System.currentTimeMillis();

			if (brewMessage.getMethod() == SOCKET_METHOD.PING) {
				return;
			}

			synchronized (socketManagerListeners) {

				for (SocketManagerListener listener : socketManagerListeners) {

					listener.onConfirmationAction(getPendingConfirmationCount());

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

			Log.i("JOSH", "confirmed in " + confirmTime + " milliseconds");

			brewMessageGuarantees.remove(brewMessage.getGuaranteeId());
			confirmed = true;

			synchronized (socketManagerListeners) {

				for (SocketManagerListener listener : socketManagerListeners) {

					if (brewMessage.getMethod() == SOCKET_METHOD.PING) {

						listener.onPingReturned(confirmTime);

					} else {

						listener.onConfirmationAction(getPendingConfirmationCount());

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

		public void onDisconnect();

		public void onConnect();

		public void onData(BrewData brewData);

		public void onPingReturned(long time);
	}

	public static interface SocketConnectionListener {
		public void onSocketReady(SocketConnection socketConnection);

		public void onSocketDisconnected(SocketConnection socketConnection);
	}

}
