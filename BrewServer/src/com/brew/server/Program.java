package com.brew.server;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketManager;

public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Logger.log("SYSTEM", "starting server");

		SocketManager.init();

		SensorManager.init();

		MySqlManager.init();

	}

}
