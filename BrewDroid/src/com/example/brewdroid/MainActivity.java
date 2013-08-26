package com.example.brewdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;

public class MainActivity extends Activity {

	private Handler handler;
	private Button connectionButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();

		setContentView(R.layout.activity_main);

		connectionButton = (Button) findViewById(R.id.connectionButton);

		connectionButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!SocketManager.isConnected()) {
					SocketManager.connect(MainActivity.this);
				} else {
					SocketManager.disconnect();
				}

			}

		});

		findViewById(R.id.brewControlButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								BrewControlActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.settingsButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								SettingsActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.sensorSettingsButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								SensorSettingsActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.userSettingsButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								UsersActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.registerButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								RegisterUserActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.loginButton).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {

						Intent i = new Intent(MainActivity.this,
								LoginUserActivity.class);
						startActivity(i);

					}

				});

		findViewById(R.id.logButton).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(MainActivity.this,
						ServerLogActivity.class);
				startActivity(i);

			}

		});

	}

	private void setButtonText() {

		handler.post(new Runnable() {

			@Override
			public void run() {
				String buttonText = SocketManager.isConnected() ? "Disconnect"
						: "Connect";

				connectionButton.setText(buttonText);
			}

		});

	}

	private SocketManagerListener socketListener = new SocketManagerListener() {

		@Override
		public void onConfirmationAction(int pendingConfirmation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect() {
			setButtonText();
		}

		@Override
		public void onConnect() {
			setButtonText();
		}

		@Override
		public void onConnectFailed() {

			handler.post(new Runnable() {

				@Override
				public void run() {

					setButtonText();

					Toast.makeText(MainActivity.this, "Connect socket failed",
							Toast.LENGTH_SHORT).show();
				}

			});

		}

		@Override
		public void onData(BrewData brewData) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPingReturned(long time) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAuthResult(boolean success) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSubscribeResult(SOCKET_CHANNEL channel,
				CHANNEL_PERMISSION permission) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onLogReceived(LogMessage logMessage) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSensorSettingsReceived(BrewData brewData) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUsersReceived(BrewData brewData) {
			// TODO Auto-generated method stub

		}

	};

	@Override
	protected void onPause() {
		SocketManager.unregisterSocketManagerListener(socketListener);
		super.onPause();
	}

	@Override
	protected void onResume() {
		setButtonText();
		SocketManager.registerSocketManagerListener(socketListener);
		super.onResume();
	}

}
