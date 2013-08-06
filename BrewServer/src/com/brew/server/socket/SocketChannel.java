package com.brew.server.socket;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SOCKET_CHANNEL;

public class SocketChannel {

	// public static enum SOCKET_CHANNEL {
	//
	// ALL, BREW_CONTROL, LOG;
	//
	// public SocketChannel CHANNEL;
	//
	// SOCKET_CHANNEL() {
	// CHANNEL = new SocketChannel();
	// }
	//
	// }

	private static Map<SOCKET_CHANNEL, SocketChannel> socketChannels;// = new
																		// Hashtable<SOCKET_CHANNEL,
																		// SocketChannel>();

	static {

		socketChannels = new Hashtable<SOCKET_CHANNEL, SocketChannel>();

		for (SOCKET_CHANNEL sc : SOCKET_CHANNEL.values()) {
			SocketChannel socket = new SocketChannel();
			socketChannels.put(sc, socket);
		}
	}

	public static SocketChannel get(SOCKET_CHANNEL channel) {

		if (socketChannels == null) {
			throw new RuntimeException("must call SocketChannel.build()");
		}

		return socketChannels.get(channel);
	}

	private List<SocketConnection> sockets = new ArrayList<SocketConnection>();

	// public List<SocketConnection> getSockets() {
	// return sockets;
	// }

	// public void setSockets(List<SocketConnection> sockets) {
	// this.sockets = sockets;
	// }

	public void removeSocketConnection(SocketConnection socketConnection) {
		sockets.remove(socketConnection);
	}

	public void addSocketConnection(SocketConnection socketConnection) {
		sockets.add(socketConnection);
	}

	public void sendBroadcast(BrewMessage message) {

		for (SocketConnection sc : sockets) {
			sc.sendMessage(message);
		}

	}

}
