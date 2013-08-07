package com.example.brewdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.User;
import com.brew.lib.util.BrewHelper;

public class RegisterUserActivity extends Activity {

	private Handler handler;
	private EditText usernameText;
	private EditText passwordText;
	private EditText nameText;
	private Button submitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_user);

		handler = new Handler();

		usernameText = (EditText) findViewById(R.id.usernameText);
		passwordText = (EditText) findViewById(R.id.passwordText);
		nameText = (EditText) findViewById(R.id.nameText);
		submitButton = (Button) findViewById(R.id.submitButton);

		submitButton.setOnClickListener(clickListener);
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.submitButton:

				if (!SocketManager.isConnected()) {
					Toast.makeText(RegisterUserActivity.this,
							"Socket not connected!", Toast.LENGTH_SHORT).show();
					return;
				}

				BrewMessage message = new BrewMessage();
				message.setMethod(SOCKET_METHOD.REGISTER_USER);
				BrewData data = new BrewData();
				message.setData(data);
				message.setGuaranteeId(UUID.randomUUID().toString());
				List<User> users = new ArrayList<User>();
				data.setUsers(users);
				User user = new User();
				users.add(user);
				user.setUsername(usernameText.getText().toString());
				String md5 = BrewHelper.md5(passwordText.getText().toString());
				user.setPassword(md5);
				user.setName(nameText.getText().toString());

				SocketManager.sendMessage(message);

				break;
			}

		}

	};

	private SocketManagerListener socketListener = new SocketManagerListener() {

		@Override
		public void onConfirmationAction(int pendingConfirmation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect() {
		}

		@Override
		public void onConnect() {
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
		public void onUserRegisterResult(final boolean success) {

			handler.post(new Runnable() {

				@Override
				public void run() {
					
					if (success) {

						Toast.makeText(RegisterUserActivity.this, "Success!",
								Toast.LENGTH_SHORT).show();
						finish();
						
					} else {

						Toast.makeText(RegisterUserActivity.this,
								"Register failed!", Toast.LENGTH_SHORT).show();

					}
				}

			});

		}

	};

	@Override
	protected void onPause() {
		SocketManager.unregisterSocketManagerListener(socketListener);
		super.onPause();
	}

	@Override
	protected void onResume() {
		SocketManager.registerSocketManagerListener(socketListener);
		super.onResume();
	}

}
