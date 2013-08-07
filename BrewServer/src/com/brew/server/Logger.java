package com.brew.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.server.socket.SocketChannel;
import com.brew.server.socket.SocketConnection;

public class Logger {

	private static final int QUEUE_SIZE = 1000;

	private static List<BrewMessage> logHistory = Collections
			.synchronizedList(new ArrayList<BrewMessage>());

	public static void log(String tag, String message) {

		// long timestamp = System.currentTimeMillis();

		System.out.println(tag + "\t" + message);

		BrewMessage brewMessage = new BrewMessage();
		brewMessage.setMethod(SOCKET_METHOD.LOG);

		LogMessage logMessage = new LogMessage();
		logMessage.setTag(tag);
		logMessage.setMessage(message);

		brewMessage.setLogMessage(logMessage);

		SocketChannel.get(SOCKET_CHANNEL.LOG).sendBroadcast(brewMessage);

		synchronized (logHistory) {

			if (logHistory.size() == QUEUE_SIZE) {
				logHistory.remove(0);
			}

			logHistory.add(brewMessage);

		}

	}

	public static void requestHistoryDump(SocketConnection socket) {

		synchronized (logHistory) {
			for (BrewMessage message : logHistory) {
				socket.sendMessage(message);
			}
		}

	}
}