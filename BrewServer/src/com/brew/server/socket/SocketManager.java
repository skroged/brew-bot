package com.brew.server.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.GsonHelper;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.server.SensorManager;
import com.google.gson.reflect.TypeToken;

public class SocketManager {

	private static boolean listening;
	public static final int PORT_NUMBER = 4444;
	// private static List<SocketConnection> sockets = Collections
	// .synchronizedList(new ArrayList<SocketConnection>());

	private static Map<String, SocketConnection> sockets = new Hashtable<String, SocketConnection>();

	private static SocketConnectionListener clientSocketListener = new SocketConnectionListener() {

		@Override
		public void onSocketIdentified(SocketConnection socket) {
			sockets.put(socket.id, socket);

			System.out.println("socket identified: " + socket.id);
		}

		@Override
		public void onSocketClosed(SocketConnection socket) {
			sockets.remove(socket.id);
			System.out.println("socket disconnected: " + socket.id);
		}

		@Override
		public void onConfirmRequested(SocketConnection socket, String confirmId) {

			BrewMessage message = new BrewMessage();
			message.setMethod(SOCKET_METHOD.CONFIRM_MESSAGE);
			message.setConfirmId(confirmId);
			sendMessage(message, socket.id);

		}

	};

	public static void init() {

		new Thread() {

			@Override
			public void run() {

				ServerSocket serverSocket = null;

				try {
					serverSocket = new ServerSocket(PORT_NUMBER);

					System.out.println("created server socket on "
							+ PORT_NUMBER);

					listening = true;

					while (listening) {

						new SocketConnection(serverSocket.accept(),
								clientSocketListener);

						System.out.println("socket connected");

					}

					System.out.println("ending socket");

					serverSocket.close();

				} catch (IOException e) {
					System.out.println("socket error!");
					e.printStackTrace();
				}

				super.run();
			}

		}.start();

	}

	public static void broadcastMessage(BrewMessage message) {

		String json = GsonHelper.getGson().toJson(message);

		Iterator<?> it = sockets.entrySet().iterator();
		while (it.hasNext()) {

			Map.Entry pairs = (Map.Entry) it.next();

			SocketConnection sc = (SocketConnection) pairs.getValue();

			sc.sendMessage(json);

		}

	}

	public static void sendMessage(final BrewMessage message,
			final String socketId) {

		new Thread() {

			@Override
			public void run() {

				String json = GsonHelper.getGson().toJson(message);

				// System.out.println("sending to socket id: " + socketId
				// + ", message: " + json);

				try {
					SocketConnection socket = sockets.get(socketId);
					socket.sendMessage(json);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				

				super.run();
			}

		}.start();

	}

	private static class SocketConnection {

		private SocketConnectionListener clientSocketListener;
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String id;

		public SocketConnection(Socket socket,
				SocketConnectionListener clientSocketListener) {
			this.socket = socket;
			this.clientSocketListener = clientSocketListener;

			startThread();
		}

		public void sendMessage(String message) {
			out.println(message);
		}

		private void processInput(String inputLine) {

			Type jsonType = new TypeToken<BrewMessage>() {
			}.getType();

			BrewMessage message = GsonHelper.getGson().fromJson(inputLine,
					jsonType);

			if (message == null) {
				System.out.println("bad message: " + inputLine);

				return;
			}

			if (message.getMethod() == null) {
				System.out.println("no method: " + inputLine);
			}

			if (message.getGuaranteeId() != null) {
				clientSocketListener.onConfirmRequested(this,
						message.getGuaranteeId());
			}

			switch (message.getMethod()) {

			case IDENTIFY_CLIENT:

				id = message.getClientIdentifier().getId();

				clientSocketListener.onSocketIdentified(this);

				break;

			case REQUEST_DUMP:

				SensorManager.requestDump(id);

				break;

			case SWITCH_UPDATE:

				SensorManager.receiveUpdate(message);

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

					clientSocketListener.onSocketClosed(SocketConnection.this);

				}

				private void setupReadWrite() throws IOException {

					out = new PrintWriter(socket.getOutputStream(), true);

					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
				}

			}.start();

		}

	}

	public static interface SocketConnectionListener {
		void onSocketIdentified(SocketConnection socket);

		void onSocketClosed(SocketConnection socket);

		void onConfirmRequested(SocketConnection socket, String confirmId);
	}

}
