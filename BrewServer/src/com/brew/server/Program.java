package com.brew.server;

import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketManager;

public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Logger.log("SYSTEM", "starting server");

		MySqlManager.init();

		SocketManager.init();

		HardwareManager.init();

	}

}
