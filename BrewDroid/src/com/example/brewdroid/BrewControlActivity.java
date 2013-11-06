package com.example.brewdroid;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.brew.client.socket.SocketManager;
import com.brew.client.socket.SocketManager.SocketManagerListener;
import com.brew.lib.model.ApkPacket;
import com.brew.lib.model.BrewData;
import com.brew.lib.model.BrewMessage;
import com.brew.lib.model.CHANNEL_PERMISSION;
import com.brew.lib.model.LogMessage;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SOCKET_METHOD;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.SensorTransport;
import com.brew.lib.model.ServerInfo;
import com.brew.lib.model.SwitchTransport;

public class BrewControlActivity extends Activity {

	private Handler handler;

	private TextView hltTempText;
	private TextView hltVolumeText;

	private TextView mltTempText;

	private TextView bkTempText;
	private TextView bkVolumeText;

	private TextView fermTempText;

	private View bkPumpButton;
	private View bkBurnerButton;
	private View bkFermButton;
	private View bkBkButton;

	private View mltPumpButton;
	private View mltBurnerButton;
	private View mltBkButton;
	private View mltMltButton;

	private View hltPumpButton;
	private View hltBurnerButton;
	private View hltMltButton;
	private View hltHltButton;

	private View igniterButton;

	private OnOffIndicator bkPumpIndicator;
	private OnOffIndicator bkBurnerIndicator;
	private OnOffIndicator bkFermIndicator;
	private OnOffIndicator bkBkIndicator;

	private OnOffIndicator mltPumpIndicator;
	private OnOffIndicator mltBurnerIndicator;
	private OnOffIndicator mltBkIndicator;
	private OnOffIndicator mltMltIndicator;

	private OnOffIndicator hltPumpIndicator;
	private OnOffIndicator hltBurnerIndicator;
	private OnOffIndicator hltMltIndicator;
	private OnOffIndicator hltHltIndicator;

	private OnOffIndicator igniterIndicator;

	private TextView connectedText;
	private TextView permissionText;
	private TextView pingText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_brew_control_2);

		handler = new Handler();

		connectedText = (TextView) findViewById(R.id.connectedText);
		permissionText = (TextView) findViewById(R.id.permissionText);
		pingText = (TextView) findViewById(R.id.pingText);

		hltTempText = (TextView) findViewById(R.id.hltTempText);
		hltVolumeText = (TextView) findViewById(R.id.hltVolumeText);

		mltTempText = (TextView) findViewById(R.id.mltTempText);

		bkTempText = (TextView) findViewById(R.id.bkTempText);
		bkVolumeText = (TextView) findViewById(R.id.bkVolumeText);

		fermTempText = (TextView) findViewById(R.id.fermTempText);

		hltPumpButton = findViewById(R.id.hltPumpButton);
		hltBurnerButton = findViewById(R.id.hltBurnerButton);
		hltHltButton = findViewById(R.id.hltHltButton);
		hltMltButton = findViewById(R.id.hltMltButton);

		mltPumpButton = findViewById(R.id.mltPumpButton);
		mltBurnerButton = findViewById(R.id.mltBurnerButton);
		mltMltButton = findViewById(R.id.mltMltButton);
		mltBkButton = findViewById(R.id.mltBkButton);

		bkPumpButton = findViewById(R.id.bkPumpButton);
		bkBurnerButton = findViewById(R.id.bkBurnerButton);
		bkBkButton = findViewById(R.id.bkBkButton);
		bkFermButton = findViewById(R.id.bkFermButton);

		igniterButton = findViewById(R.id.igniterButton);

		hltPumpIndicator = (OnOffIndicator) findViewById(R.id.hltPumpIndicator);
		hltBurnerIndicator = (OnOffIndicator) findViewById(R.id.hltBurnerIndicator);
		hltHltIndicator = (OnOffIndicator) findViewById(R.id.hltHltIndicator);
		hltMltIndicator = (OnOffIndicator) findViewById(R.id.hltMltIndicator);

		mltPumpIndicator = (OnOffIndicator) findViewById(R.id.mltPumpIndicator);
		mltBurnerIndicator = (OnOffIndicator) findViewById(R.id.mltBurnerIndicator);
		mltMltIndicator = (OnOffIndicator) findViewById(R.id.mltMltIndicator);
		mltBkIndicator = (OnOffIndicator) findViewById(R.id.mltBkIndicator);

		bkPumpIndicator = (OnOffIndicator) findViewById(R.id.bkPumpIndicator);
		bkBurnerIndicator = (OnOffIndicator) findViewById(R.id.bkBurnerIndicator);
		bkBkIndicator = (OnOffIndicator) findViewById(R.id.bkBkIndicator);
		bkFermIndicator = (OnOffIndicator) findViewById(R.id.bkFermIndicator);

		igniterIndicator = (OnOffIndicator) findViewById(R.id.igniterIndicator);

		hltPumpButton.setOnClickListener(switchClick);
		hltBurnerButton.setOnClickListener(switchClick);
		hltHltButton.setOnClickListener(switchClick);
		hltMltButton.setOnClickListener(switchClick);

		mltPumpButton.setOnClickListener(switchClick);
		mltBurnerButton.setOnClickListener(switchClick);
		mltMltButton.setOnClickListener(switchClick);
		mltBkButton.setOnClickListener(switchClick);

		bkPumpButton.setOnClickListener(switchClick);
		bkBurnerButton.setOnClickListener(switchClick);
		bkBkButton.setOnClickListener(switchClick);
		bkFermButton.setOnClickListener(switchClick);

		igniterButton.setOnClickListener(switchClick);

		findViewById(R.id.progressBar1).setVisibility(View.GONE);

		setAllUnknown();

	}

	private void setAllUnknown() {

		bkTempText.setText("UNKNOWN");
		bkVolumeText.setText("UNKNOWN");

		mltTempText.setText("UNKNOWN");

		hltTempText.setText("UNKNOWN");
		hltVolumeText.setText("UNKNOWN");

		fermTempText.setText("UNKNOWN");

		pingText.setText("");
		permissionText.setText("");

	}

	private OnClickListener switchClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			BrewMessage message = new BrewMessage();
			message.setGuaranteeId(UUID.randomUUID().toString());
			message.setMethod(SOCKET_METHOD.SWITCH_UPDATE);
			BrewData data = new BrewData();
			message.setData(data);
			List<SwitchTransport> switches = new ArrayList<SwitchTransport>();
			data.setSwitches(switches);
			SwitchTransport st = new SwitchTransport();
			switches.add(st);

			switch (v.getId()) {

			case R.id.hltPumpButton:
				st.setSwitchName(SWITCH_NAME.HLT_PUMP);
				st.setSwitchValue(!hltPumpIndicator.isOn());
				break;

			case R.id.hltBurnerButton:
				st.setSwitchName(SWITCH_NAME.HLT_BURNER);
				st.setSwitchValue(!hltBurnerIndicator.isOn());
				break;

			case R.id.hltHltButton:
				st.setSwitchName(SWITCH_NAME.HLT_HLT);
				st.setSwitchValue(!hltHltIndicator.isOn());
				break;

			case R.id.hltMltButton:
				st.setSwitchName(SWITCH_NAME.HLT_MLT);
				st.setSwitchValue(!hltMltIndicator.isOn());
				break;

			case R.id.mltPumpButton:
				st.setSwitchName(SWITCH_NAME.MLT_PUMP);
				st.setSwitchValue(!mltPumpIndicator.isOn());
				break;

			case R.id.mltBurnerButton:
				st.setSwitchName(SWITCH_NAME.MLT_BURNER);
				st.setSwitchValue(!mltBurnerIndicator.isOn());
				break;

			case R.id.mltMltButton:
				st.setSwitchName(SWITCH_NAME.MLT_MLT);
				st.setSwitchValue(!mltMltIndicator.isOn());
				break;

			case R.id.mltBkButton:
				st.setSwitchName(SWITCH_NAME.MLT_BK);
				st.setSwitchValue(!mltBkIndicator.isOn());
				break;

			case R.id.bkPumpButton:
				st.setSwitchName(SWITCH_NAME.BK_PUMP);
				st.setSwitchValue(!bkPumpIndicator.isOn());
				break;

			case R.id.bkBurnerButton:
				st.setSwitchName(SWITCH_NAME.BK_BURNER);
				st.setSwitchValue(!bkBurnerIndicator.isOn());
				break;

			case R.id.bkBkButton:
				st.setSwitchName(SWITCH_NAME.BK_BK);
				st.setSwitchValue(!bkBkIndicator.isOn());
				break;

			case R.id.bkFermButton:
				st.setSwitchName(SWITCH_NAME.BK_FERM);
				st.setSwitchValue(!bkFermIndicator.isOn());
				break;

			case R.id.igniterButton:
				st.setSwitchName(SWITCH_NAME.IGNITER);
				st.setSwitchValue(!igniterIndicator.isOn());
				break;
			}

			SocketManager.sendMessage(message);

		}

	};

	private SocketManagerListener socketManagerListener = new SocketManagerListener() {

		@Override
		public void onDisconnect() {

			handler.post(new Runnable() {

				@Override
				public void run() {
					connectedText.setText("Diconnected");
					connectedText.setTextColor(Color.RED);
					setAllUnknown();

					stopPingLoop();
				}

			});
		}

		@Override
		public void onConnect() {

			handler.post(new Runnable() {

				@Override
				public void run() {
					connectedText.setText("Connected");
					connectedText.setTextColor(Color.GREEN);

					startPingLoop();
				}

			});

			subscribe();
		}

		@Override
		public void onData(final BrewData brewData) {
			handler.post(new Runnable() {

				@Override
				public void run() {

					if (brewData.getSwitches() != null) {

						for (SwitchTransport st : brewData.getSwitches()) {

							boolean setValue = st.getSwitchValue() != null;

							switch (st.getSwitchName()) {

							case HLT_PUMP:

								if (setValue)
									hltPumpIndicator.setOn(st.getSwitchValue());

								break;

							case HLT_BURNER:

								if (setValue)
									hltBurnerIndicator.setOn(st
											.getSwitchValue());

								break;

							case HLT_HLT:

								if (setValue)
									hltHltIndicator.setOn(st.getSwitchValue());

								break;

							case HLT_MLT:

								if (setValue)
									hltMltIndicator.setOn(st.getSwitchValue());

								break;

							case MLT_PUMP:

								if (setValue)
									mltPumpIndicator.setOn(st.getSwitchValue());

								break;

							case MLT_BURNER:

								if (setValue)
									mltBurnerIndicator.setOn(st
											.getSwitchValue());

								break;

							case MLT_MLT:

								if (setValue)
									mltMltIndicator.setOn(st.getSwitchValue());

								break;

							case MLT_BK:

								if (setValue)
									mltBkIndicator.setOn(st.getSwitchValue());

								break;

							case BK_PUMP:

								if (setValue)
									bkPumpIndicator.setOn(st.getSwitchValue());

								break;

							case BK_BURNER:

								if (setValue)
									bkBurnerIndicator.setOn(st.getSwitchValue());

								break;

							case BK_BK:

								if (setValue)
									bkBkIndicator.setOn(st.getSwitchValue());

								break;

							case BK_FERM:

								if (setValue)
									bkFermIndicator.setOn(st.getSwitchValue());

								break;

							case IGNITER:

								if (setValue)
									igniterIndicator.setOn(st.getSwitchValue());

								break;

							}
						}

					}
					if (brewData.getSensors() != null) {

						NumberFormat nf = NumberFormat.getNumberInstance();
						nf.setMaximumFractionDigits(2);

						for (SensorTransport st : brewData.getSensors()) {

							boolean setValue = st.getValue() != null;

							switch (st.getSensorName()) {

							case HLT_TEMP:

								if (setValue) {
									hltTempText.setText(nf.format(st.getValue()) + " °F");
								}

								break;

							case HLT_VOLUME:

								if (setValue) {
									hltVolumeText.setText(nf.format(st.getValue())
											+ " Gal");
								}

								break;

							case MLT_TEMP:

								if (setValue) {
									mltTempText.setText(nf.format(st.getValue())
											+ " °F");
								}

								break;

							case BK_TEMP:

								if (setValue) {
									bkTempText.setText(nf.format(st.getValue())
											+ " °F");
								}

								break;

							case BK_VOLUME:

								if (setValue) {
									bkVolumeText.setText(nf.format(st.getValue()) + " Gal");
								}

								break;

							case FERM_TEMP:

								if (setValue) {
									fermTempText.setText(nf.format(st.getValue()) + " °F");
								}

								break;
							}
						}

					}

				}

			});

		}

		@Override
		public void onPingReturned(final long time) {

			handler.post(new Runnable() {

				@Override
				public void run() {
					pingText.setText("Ping: " + time + "ms");

				}

			});

		}

		@Override
		public void onConfirmationAction(final int pendingConfirmation) {

			handler.post(new Runnable() {

				@Override
				public void run() {

					int visibility = pendingConfirmation > 0 ? View.VISIBLE
							: View.GONE;

					findViewById(R.id.progressBar1).setVisibility(visibility);
				}

			});

		}

		@Override
		public void onConnectFailed() {

			Toast.makeText(BrewControlActivity.this, "Connect socket failed",
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onAuthResult(boolean success) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSubscribeResult(SOCKET_CHANNEL channel,
				final CHANNEL_PERMISSION permission) {

			if (channel == SOCKET_CHANNEL.BREW_CONTROL) {
				if (permissionText == null) {
					Log.i("JOSH", "permissionText is null!!!??");
				} else {
					handler.post(new Runnable() {

						@Override
						public void run() {
							permissionText.setText("Permission: " + permission);
						}

					});

				}
			}
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

		@Override
		public void onServerInfoReceived(ServerInfo info) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onApkPacketReceived(ApkPacket packet) {
			// TODO Auto-generated method stub
			
		}

	};

	@Override
	protected void onResume() {
		super.onResume();

		SocketManager.registerSocketManagerListener(socketManagerListener);

		if (SocketManager.isConnected()) {

			connectedText.setText("Connected");
			connectedText.setTextColor(Color.GREEN);

			startPingLoop();

			subscribe();

		} else {

			connectedText.setText("Diconnected");
			connectedText.setTextColor(Color.RED);

			stopPingLoop();

			setAllUnknown();

		}

	}

	private void startPingLoop() {
		pingLooping = true;
		loopPing();
	}

	private void stopPingLoop() {
		pingLooping = false;
	}

	private boolean pingLooping;

	private void loopPing() {

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {

				if (pingLooping) {

					BrewMessage message = new BrewMessage();
					message.setGuaranteeId(UUID.randomUUID().toString());
					message.setMethod(SOCKET_METHOD.PING);

					SocketManager.sendMessage(message);

					loopPing();
				}

			}

		}, 3000);

	}

	@Override
	protected void onPause() {
		super.onPause();

		stopPingLoop();

		SocketManager.unregisterSocketManagerListener(socketManagerListener);

		unsubscribe();
	}

	private void subscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.SUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.BREW_CONTROL);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

	private void unsubscribe() {

		BrewMessage message = new BrewMessage();
		message.setMethod(SOCKET_METHOD.UNSUBSCRIBE);
		message.setChannel(SOCKET_CHANNEL.BREW_CONTROL);
		message.setGuaranteeId(UUID.randomUUID().toString());

		SocketManager.sendMessage(message);
	}

}
