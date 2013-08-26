package com.example.brewdroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.User;

public class UsersActivity extends Activity {

	private Handler handler;
	private ListView userList;
	private List<User> users;
	private UsersPermissionAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		users = Collections.synchronizedList(new ArrayList<User>());
		handler = new Handler();

		setContentView(R.layout.activity_users);

		userList = (ListView) findViewById(R.id.userList);

		adapter = new UsersPermissionAdapter(this, users);
		userList.setAdapter(adapter);
	}

	private SocketManagerListener socketListener = new SocketManagerListener() {

		@Override
		public void onConfirmationAction(int pendingConfirmation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect() {
			users.clear();
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onConnect() {
			subscribe();
		}

		@Override
		public void onConnectFailed() {

		}

		@Override
		public void onData(BrewData brewData) {

		}

		@Override
		public void onPingReturned(long time) {

		}

		@Override
		public void onAuthResult(boolean success) {

		}

		@Override
		public void onSubscribeResult(SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION permission) {

		}

		@Override
		public void onLogReceived(final LogMessage logMessage) {

		}

		@Override
		public void onSensorSettingsReceived(BrewData brewData) {

		}

		@Override
		public void onUsersReceived(BrewData brewData) {

			synchronized (users) {

				for (User u1 : brewData.getUsers()) {

					User user = null;
					for (User u2 : users) {

						if (u1.getId() == u2.getId()) {
							user = u2;
							user.populateNewSettings(u1);
							break;
						}
					}

					if (user == null) {
						user = u1;
						users.add(user);
					}

				}
			}

			handler.post(new Runnable() {

				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}

			});

		}

	};

	private void subscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.SUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.USERS);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	private void unsubscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.USERS);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	@Override
	protected void onPause() {
		super.onPause();

		SocketManager.unregisterSocketManagerListener(socketListener);

		unsubscribe();
	}

	@Override
	protected void onResume() {
		super.onResume();

		SocketManager.registerSocketManagerListener(socketListener);

		if (SocketManager.isConnected()) {

			subscribe();

		}
	}
}
