package com.brew.server.socket;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketManager {

	private static boolean listening;
	public static final int PORT_NUMBER = 4444;

	// private static List<SocketConnection> sockets = Collections
	// .synchronizedList(new ArrayList<SocketConnection>());

	// private static Map<SOCKET_CHANNEL, SocketChannel> socketChannels = new
	// Hashtable<SOCKET_CHANNEL, SocketChannel>();

	// private static SocketConnectionListener clientSocketListener = new
	// SocketConnectionListener() {
	//
	// // @Override
	// // public void onSocketIdentified(SocketConnection socket) {
	// // sockets.put(socket.id, socket);
	// //
	// // System.out.println("socket identified: " + socket.id);
	// // }
	//
	// @Override
	// public void onSocketClosed(SocketConnection socket) {
	// for (SOCKET_CHANNEL sc : SOCKET_CHANNEL.values()) {
	// sc.CHANNEL.removeSocketConnection(socket);
	// }
	// // sockets.remove(socket);
	// System.out.println("socket disconnected: " + socket);
	// }
	//
	// @Override
	// public void onConfirmRequested(SocketConnection socket, String confirmId)
	// {
	//
	// BrewMessage message = new BrewMessage();
	// message.setMethod(SOCKET_METHOD.CONFIRM_MESSAGE);
	// message.setConfirmId(confirmId);
	// socket.sendMessage(message);
	//
	// }
	//
	// };

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

						new SocketConnection(serverSocket.accept());

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

	// public static void broadcastMessage(SOCKET_CHANNEL channel, BrewMessage
	// message) {
	//
	// channel.CHANNEL.sendBroadcast(message);
	//
	// // String json = GsonHelper.getGson().toJson(message);
	// //
	// // Iterator<?> it = sockets.entrySet().iterator();
	// // while (it.hasNext()) {
	// //
	// // Map.Entry pairs = (Map.Entry) it.next();
	// //
	// // SocketConnection sc = (SocketConnection) pairs.getValue();
	// //
	// // sc.sendMessage(json);
	// //
	// // }
	//
	// }

	// public static void sendMessage(final BrewMessage message,
	// final String socketId) {
	//
	// new Thread() {
	//
	// @Override
	// public void run() {
	//
	// String json = GsonHelper.getGson().toJson(message);
	//
	// // System.out.println("sending to socket id: " + socketId
	// // + ", message: " + json);
	//
	// try {
	// SocketConnection socket = sockets.get(socketId);
	// socket.sendMessage(json);
	// } catch (NullPointerException e) {
	// e.printStackTrace();
	// }
	//
	// super.run();
	// }
	//
	// }.start();
	//
	// }

}
