package com.example.brewdroid;

import java.util.UUID;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;

public class ServerLogActivity extends Activity {

	private TextView logText;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_log);

		handler = new Handler();

		logText = (TextView) findViewById(R.id.logText);
	}

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

			handler.post(new Runnable() {

				@Override
				public void run() {
					String tag = logMessage.getTag();
					String message = logMessage.getMessage();

					String concat = tag + "\t" + message + "\n";

					Spannable spannable = new SpannableString(concat);

					spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0,
							tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					logText.append(spannable);
				}

			});

		}

		@Override
		public void onSensorSettingsReceived(BrewData brewData) {
			// TODO Auto-generated method stub
			
		}

	};

	private void subscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.SUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.LOG);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	private void unsubscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.LOG);
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
