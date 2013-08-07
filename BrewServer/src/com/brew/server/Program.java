package com.brew.server;

import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketManager;

public class Program {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("starting server");
		
		SocketManager.init();
		
		SensorManager.init();
		
		MySqlManager.init();
	}

}
