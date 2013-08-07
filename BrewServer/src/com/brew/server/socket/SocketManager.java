package com.brew.server.socket;

import java.io.IOException;
import java.net.ServerSocket;

import com.brew.server.Logger;

public class SocketManager {

	private static boolean listening;
	public static final int PORT_NUMBER = 4444;

	public static void init() {

		new Thread() {

			@Override
			public void run() {

				ServerSocket serverSocket = null;

				try {
					serverSocket = new ServerSocket(PORT_NUMBER);

					Logger.log("SOCKET", "created server socket on "
							+ PORT_NUMBER);

					listening = true;

					while (listening) {

						new SocketConnection(serverSocket.accept());

						Logger.log("SOCKET", "socket connected: " + serverSocket);

					}

					Logger.log("SOCKET", "ending socket");

					serverSocket.close();

				} catch (IOException e) {
					Logger.log("SOCKET", "socket error!");
					Logger.log("ERROR", e.getMessage());
				}

				super.run();
			}

		}.start();

	}

}
