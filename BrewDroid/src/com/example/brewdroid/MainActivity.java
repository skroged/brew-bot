package com.example.brewdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.brew.lib.model.ServerInfo;

public class MainActivity extends Activity {

	private Handler handler;
	private Button connectionButton;
	private ProgressDialog downloadProgressDialog;

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

		// 000002
		// 1377662083000
		// 1377683399000

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

			try {
				PackageManager pm = getPackageManager(); 
				ApplicationInfo appInfo = pm.getApplicationInfo(
						"com.example.brewdroid", 0);
				String appFile = appInfo.sourceDir;
				long installed = new File(appFile).lastModified();

				Log.i("JOSH", "installed build: " + installed);
				Log.i("JOSH", "server build: " + info.getAndroidBuildDate());

				if (info.getAndroidBuildDate() > installed) {

					handler.post(new Runnable() {

						@Override
						public void run() {
							// Toast.makeText(MainActivity.this,
							// "need new version of app",
							// Toast.LENGTH_SHORT).show();

							showNeedNewAppDialog();

						}

					});

				}

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onApkPacketReceived(final ApkPacket packet) {

			lastPacketRecTime = System.currentTimeMillis();

			if (packets == null) {
				packetRecCount = 0;
				packets = new ApkPacket[packet.getTotalPackets()];
			}

			packetRecCount++;
			packets[packet.getPacketNumber()] = packet;

			handler.post(new Runnable() {

				@Override
				public void run() {
					int progress = (int) (100f * ((float) packetRecCount) / ((float) packets.length));
					downloadProgressDialog.setProgress(progress);

					downloadProgressDialog.setMessage((packet.getData()
							.length() * packetRecCount)
							+ "/"
							+ (packet.getData().length() * packets.length));
				}

			});

			if (packetRecCount == packets.length) {

				handler.post(new Runnable() {

					@Override
					public void run() {
						downloadProgressDialog.setIndeterminate(true);
						downloadProgressDialog.setMessage("Preparing package");
					}

				});

				String base64 = "";

				for (ApkPacket p : packets) {
					base64 += p.getData();
				}

				byte[] fileBytes = Base64.decode(base64, Base64.DEFAULT);
				int byteCount = fileBytes.length;
				Log.i("JOSH", "got file: " + byteCount + " bytes");

				String fileName = "BrewBot.apk";
				String directoryPath = Environment
						.getExternalStorageDirectory() + "/BrewBot";

				File directory = new File(directoryPath);
				File file = new File(directoryPath + "/" + fileName);

				if (!directory.exists()) {
					directory.mkdirs();
				}

				if (file.exists()) {
					file.delete();
				}

				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				boolean error = false;
				try {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(fileBytes);
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					error = true;
				} catch (IOException e) {
					e.printStackTrace();
					error = true;
				}

				handler.post(new Runnable() {

					@Override
					public void run() {
						downloadProgressDialog.dismiss();
					}

				});

				if (!error) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					startActivity(intent);
				}

				else {
					handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(MainActivity.this,
									"Error with update", Toast.LENGTH_SHORT)
									.show();
							finish();
						}

					});

				}

			}

		}

	};

	private void showAppDownloadDialog() {

		downloadProgressDialog = new ProgressDialog(this);
		downloadProgressDialog.setTitle("Downloading App Update");
		downloadProgressDialog.setCancelable(false);
		downloadProgressDialog.setIndeterminate(false);
		downloadProgressDialog.setProgress(0);
		downloadProgressDialog.show();

	}

	private void showNeedNewAppDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Your app is out of date. Download new one?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						showAppDownloadDialog();

						BrewMessage bm = new BrewMessage();
						bm.setMethod(SOCKET_METHOD.REQUEST_ANDROID_APK);
						bm.setGuaranteeId(UUID.randomUUID().toString());
						SocketManager.sendMessage(bm);
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private DownloadWatcher downloadWatcher;
	private int packetRecCount;
	private ApkPacket[] packets;
	private long lastPacketRecTime;

	private class DownloadWatcher extends Thread {

		private boolean watching;

		@Override
		public void run() {

			while (watching) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}

				if (!watching)
					return;

				long timeSinceLast = System.currentTimeMillis()
						- lastPacketRecTime;

				if (timeSinceLast > 10000) {

					// reset timeout
					lastPacketRecTime = System.currentTimeMillis();

					// timeout!!!
					List<Integer> missingPackets = new ArrayList<Integer>();

					for (int i = 0; i < packets.length; i++) {

						if (packets[i] == null) {
							missingPackets.add(i);
						}
					}

					BrewMessage message = new BrewMessage();
					message.setMissingPackets(missingPackets);
					message.setMethod(SOCKET_METHOD.REQUEST_ANDROID_APK);
					SocketManager.sendMessage(message);

				}

			}

			super.run();
		}

	}

	@Override
	protected void onPause() {
		SocketManager.unregisterSocketManagerListener(socketListener);
		if (downloadWatcher != null) {
			downloadWatcher.watching = false;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		setButtonText();
		SocketManager.registerSocketManagerListener(socketListener);
		super.onResume();
	}

}
