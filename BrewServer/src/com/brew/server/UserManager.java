package com.brew.server;

import java.util.ArrayList;
import java.util.List;

import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SwitchTransport;
import com.brew.lib.model.User;
import com.brew.server.db.MySqlManager;
import com.brew.server.socket.SocketChannel;
import com.brew.server.socket.SocketConnection;

public class UserManager {

	public static void requestUserDump(SocketConnection socketConnection) {
		List<User> users = MySqlManager.getUsers();

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.UPDATE_USERS);
		BrewData data = new BrewData();
		message.setData(data);
		data.setUsers(users);

		socketConnection.sendMessage(message);
	}

	public static void receiveUpdate(BrewMessage message) {

		for (User user : message.getData().getUsers()) {
			MySqlManager.updateUser(user);
			notifyUserChanged(user);
		}
	}

	public static void notifyUserChanged(User user) {

		BrewMessage message = new BrewMessage();

		message.setMethod(SOCKET_METHOD.UPDATE_USERS);

		BrewData data = new BrewData();

		List<User> users = new ArrayList<User>();
		users.add(user);
		data.setUsers(users);

		message.setData(data);

		SocketChannel.get(SOCKET_CHANNEL.USERS).sendBroadcast(message);

	}

}
