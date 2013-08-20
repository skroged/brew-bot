package com.brew.server.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;

import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.GsonHelper;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.User;
import com.brew.server.HardwareManager;
import com.brew.server.Logger;
import com.brew.server.db.MySqlManager;
import com.google.gson.reflect.TypeToken;

public class SocketConnection {
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private User user;

	public SocketConnection(Socket socket) {
		this.socket = socket;

		startThread();
	}

	public void sendMessage(final BrewMessage message) {

		new Thread() {

			@Override
			public void run() {

				String json = GsonHelper.getGson().toJson(message);

				try {
					synchronized (out) {
						out.println(json);
					}
				} catch (NullPointerException e) {
					Logger.log("ERROR", e.getMessage());
				}

				super.run();
			}

		}.start();

	}

	private void processInput(String inputLine) {

		Type jsonType = new TypeToken<BrewMessage>() {
		}.getType();

		BrewMessage message = GsonHelper.getGson()
				.fromJson(inputLine, jsonType);

		if (message == null) {
			Logger.log("SOCKET", "bad message: " + inputLine);

			return;
		}

		if (message.getMethod() == null) {
			Logger.log("SOCKET", "no method: " + inputLine);
		}

		if (message.getGuaranteeId() != null) {

			BrewMessage confirmMessage = new BrewMessage();
			confirmMessage.setMethod(SOCKET_METHOD.CONFIRM_MESSAGE);
			confirmMessage.setConfirmId(message.getGuaranteeId());
			sendMessage(confirmMessage);

		}

		switch (message.getMethod()) {

		case SENSOR_SETTINGS_UPDATE:

			CHANNEL_PERMISSION socketSettingsPermission = CHANNEL_PERMISSION.NONE;
			if (user != null) {
				socketSettingsPermission = user
						.getPermissionForChannel(SOCKET_CHANNEL.SENSOR_SETTINGS);
			}

			if (socketSettingsPermission == CHANNEL_PERMISSION.READWRITE) {
				HardwareManager.receiveUpdate(message);
			} else {
				Logger.log("AUTHENTICATION",
						"rejected sensor settings change due to permission level for user "
								+ user.getUsername());
			}

			break;

		case LOGIN_USER:

			if (message.getData() == null
					|| message.getData().getUsers() == null
					|| message.getData().getUsers().size() == 0) {
				Logger.log("SOCKET", "error in login user packet");

				return;
			}

			User loginUser = message.getData().getUsers().get(0);

			user = MySqlManager.loginUser(loginUser);

			BrewMessage loginResultMessage = new BrewMessage();
			loginResultMessage.setMethod(SOCKET_METHOD.LOGIN_RESULT);
			loginResultMessage.setSuccess(user != null);
			sendMessage(loginResultMessage);

			break;

		case REGISTER_USER:

			if (message.getData() == null
					|| message.getData().getUsers() == null
					|| message.getData().getUsers().size() == 0) {
				Logger.log("SOCKET", "error in register user packet");

				return;
			}

			User registerUser = message.getData().getUsers().get(0);

			user = MySqlManager.registerUser(registerUser);

			MySqlManager.setPermissionForUser(user.getId(),
					SOCKET_CHANNEL.BREW_CONTROL, CHANNEL_PERMISSION.READ);

			BrewMessage registerResultMessage = new BrewMessage();
			registerResultMessage.setMethod(SOCKET_METHOD.REGISTER_RESULT);
			registerResultMessage.setSuccess(user != null);
			sendMessage(registerResultMessage);

			break;

		case UNSUBSCRIBE:

			SOCKET_CHANNEL unsubscribeChannel = message.getChannel();

			if (unsubscribeChannel == null) {
				Logger.log("SOCKET",
						"attempted to unsubscribe to null channel!");
				return;
			}

			SocketChannel.get(unsubscribeChannel).removeSocketConnection(this);

			break;

		case SUBSCRIBE:

			SOCKET_CHANNEL subscribeChannel = message.getChannel();

			if (subscribeChannel == null) {
				Logger.log("SOCKET", "attempted to subscribe to null channel!");
				return;
			}

			CHANNEL_PERMISSION channelPermission = CHANNEL_PERMISSION.NONE;
			if (user != null) {
				channelPermission = user
						.getPermissionForChannel(subscribeChannel);
			}

			BrewMessage subscribeResultMessage = new BrewMessage();
			subscribeResultMessage.setMethod(SOCKET_METHOD.SUBSCRIBE_RESULT);
			subscribeResultMessage.setChannelPermission(channelPermission);
			subscribeResultMessage.setChannel(subscribeChannel);
			sendMessage(subscribeResultMessage);

			if (channelPermission == CHANNEL_PERMISSION.NONE) {

				Logger.log("SOCKET",
						"no permission for user " + user.getUsername()
								+ " on channel " + subscribeChannel);

				return;
			}

			Logger.log("SOCKET", "user " + user.getUsername()
					+ " subscribed to channel " + subscribeChannel
					+ " with permission " + channelPermission);

			SocketChannel.get(subscribeChannel).addSocketConnection(this);

			switch (subscribeChannel) {

			case BREW_CONTROL:

				HardwareManager.requestDataDump(this);
				break;

			case LOG:

				Logger.requestHistoryDump(this);
				break;

			case SENSOR_SETTINGS:
				HardwareManager.requestSettingsDump(this);
				break;
			}

			break;

		case SWITCH_UPDATE:

			CHANNEL_PERMISSION switchUpdatePermission = CHANNEL_PERMISSION.NONE;
			if (user != null) {
				switchUpdatePermission = user
						.getPermissionForChannel(SOCKET_CHANNEL.BREW_CONTROL);
			}

			if (switchUpdatePermission == CHANNEL_PERMISSION.READWRITE) {
				HardwareManager.receiveUpdate(message);
			} else {
				Logger.log("AUTHENTICATION",
						"rejected switch change due to permission level for user "
								+ user.getUsername());
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
					Logger.log("ERROR", e.getMessage());
				}

				cleanupSocket();

			}

			private void setupReadWrite() throws IOException {

				out = new PrintWriter(socket.getOutputStream(), true);

				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			}

		}.start();

	}

	private void cleanupSocket() {
		for (SOCKET_CHANNEL sc : SOCKET_CHANNEL.values()) {
			SocketChannel.get(sc).removeSocketConnection(this);
		}
		Logger.log("SOCKET", "socket disconnected: " + socket);
	}
}
