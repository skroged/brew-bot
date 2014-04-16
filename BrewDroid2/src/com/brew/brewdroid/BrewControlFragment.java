package com.brew.brewdroid;

import java.text.NumberFormat;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.brew.brewdroid.OnOffIndicator.SWITCH_STATE;
import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.brewdroid.socket.SocketManager;
import com.brew.brewdroid.util.BrewDroidUtil;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.Switch;
import com.brew.lib.model.User;
import com.brew.lib.model.UserChannelPermission;

public class BrewControlFragment extends Fragment {

	private Handler handler;

	private TextView hltTempText;
	private TextView hltFlameText;
	private TextView hltVolumeText;

	private TextView mltTempText;
	private TextView mltFlameText;

	private TextView bkTempText;
	private TextView bkFlameText;
	private TextView bkVolumeText;

	private TextView fermTempText;

	private TextView boxTempText;

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
	private View fillButton;
	private View chillButton;
	private View airButton;

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
	private OnOffIndicator fillIndicator;
	private OnOffIndicator chillIndicator;
	private OnOffIndicator airIndicator;

	private TextView connectedText;
	private TextView permissionText;
	private TextView pingText;

	private MediaPlayer mMediaPlayerSwitchOn;
	private MediaPlayer mMediaPlayerSwitchOff;

	public static BrewControlFragment instantiate() {
		BrewControlFragment frag = new BrewControlFragment();
		frag.handler = new Handler();
		return frag;
	}

	private void setConnectedState() {

		if (SocketManager.isConnected()) {
			connectedText.setText("Connected");
			connectedText.setTextColor(Color.GREEN);
			permissionText.setVisibility(View.VISIBLE);
			pingText.setVisibility(View.VISIBLE);
		} else {
			connectedText.setText("Disconnected");
			connectedText.setTextColor(Color.RED);
			permissionText.setVisibility(View.GONE);
			pingText.setVisibility(View.GONE);
		}

		getPermissions();

	}

	private BroadcastReceiver mAuthReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			getPermissions();
		}
	};

	private BroadcastReceiver mPingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			long pingTime = intent.getLongExtra(
					BrewDroidService.BUNDLE_PING_TIME, -1);
			pingText.setText("Ping: " + pingTime + " ms");
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mMediaPlayerSwitchOn = MediaPlayer.create(activity, R.raw.switch1);
		mMediaPlayerSwitchOff = MediaPlayer.create(activity, R.raw.switch2);
		mMediaPlayerSwitchOff.setVolume(.5f, .5f);

		BrewDroidContentProvider.registerSensorsContentObserver(getActivity(),
				sensorObserver);
		BrewDroidContentProvider.registerSwitchesContentObserver(activity,
				switchesContentObserver);
		activity.registerReceiver(mConnectReceiver, new IntentFilter(
				BrewDroidService.ACTION_CONNECT_CHANGED));

		IntentFilter authFilter = new IntentFilter(
				BrewDroidService.ACTION_AUTH_RESULT);
		authFilter.addAction(BrewDroidService.ACTION_LOGOUT);
		activity.registerReceiver(mAuthReceiver, authFilter);

		IntentFilter pingFilter = new IntentFilter(
				BrewDroidService.ACTION_PING_RESULT);
		activity.registerReceiver(mPingReceiver, pingFilter);

		subscribe();

	}

	private void subscribe() {
		if (getActivity() == null) {
			return;
		}
		Intent intent = new Intent(BrewDroidService.ACTION_SUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.BREW_CONTROL.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);
	}

	@Override
	public void onDetach() {

		if (mMediaPlayerSwitchOn != null) {
			mMediaPlayerSwitchOn.release();
		}

		if (mMediaPlayerSwitchOff != null) {
			mMediaPlayerSwitchOff.release();
		}

		Intent intent = new Intent(BrewDroidService.ACTION_UNSUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.BREW_CONTROL.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

		BrewDroidContentProvider.unregisterContentObserver(getActivity(),
				sensorObserver);
		BrewDroidContentProvider.unregisterContentObserver(getActivity(),
				switchesContentObserver);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mConnectReceiver);
		getActivity().unregisterReceiver(mAuthReceiver);
		getActivity().unregisterReceiver(mPingReceiver);

		super.onDetach();
	}

	public BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setConnectedState();
		}
	};

	private QueryListener permissionQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (cursor != null) {
				while (cursor.moveToNext()) {
					UserChannelPermission permission = DataObjectTranslator
							.getPermissionFromCursor(cursor);
					if (permission.getChannel() == SOCKET_CHANNEL.BREW_CONTROL) {
						permissionText.setText("Permission: "
								+ permission.getPermission());
					}
				}

				cursor.close();
			}
		}

	};

	private void getPermissions() {
		User user = BrewDroidUtil.getSavedUser(getActivity());
		if (user != null) {
			BrewDroidContentProvider.queryPermissionsForUserId(
					permissionQueryListener, getActivity(), user.getId());
		} else {
			permissionText.setText("Not logged in!");
		}

		subscribe();
	}

	private void getAllSensorsCursor() {
		BrewDroidContentProvider.querySensors(sensorsQueryListener,
				getActivity());
	}

	private QueryListener sensorsQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (getActivity() == null) {
				return;
			}

			if (cursor != null) {

				while (cursor.moveToNext()) {

					try {
						Sensor sensor = DataObjectTranslator
								.getSensorFromCursor(cursor);

						onSensorUpdate(sensor);
					} catch (CursorIndexOutOfBoundsException e) {
						e.printStackTrace();
					}

				}

				cursor.close();
			}
		}

	};

	private ContentObserver sensorObserver = new ContentObserver(handler) {

		@Override
		public void onChange(boolean selfChange) {
			getAllSensorsCursor();
			super.onChange(selfChange);
		}

	};

	private void onSensorUpdate(Sensor sensor) {

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		switch (sensor.getSensorName()) {

		case HLT_TEMP:
			hltTempText.setText(nf.format(sensor.getValue()) + " \u00B0F");
			break;
		case HLT_VOLUME:
			hltVolumeText.setText(nf.format(sensor.getValue()) + " Gal");
			break;
		case MLT_TEMP:
			mltTempText.setText(nf.format(sensor.getValue()) + " \u00B0F");
			break;
		case BK_TEMP:
			bkTempText.setText(nf.format(sensor.getValue()) + " \u00B0F");
			break;
		case BK_VOLUME:
			bkVolumeText.setText(nf.format(sensor.getValue()) + " Gal");
			break;
		case FERM_TEMP:
			fermTempText.setText(nf.format(sensor.getValue()) + " \u00B0F");
			break;
		case BOX_TEMP:
			boxTempText.setText(nf.format(sensor.getValue()) + " \u00B0F");
			break;
		case HLT_FLAME:
			hltFlameText.setText(nf.format(sensor.getValue()) + " FU");
			break;
		case MLT_FLAME:
			mltFlameText.setText(nf.format(sensor.getValue()) + " FU");
			break;
		case BK_FLAME:
			bkFlameText.setText(nf.format(sensor.getValue()) + " FU");
			break;
		}

	}

	// ///////////////

	private void getAllSwitchesCursor() {
		BrewDroidContentProvider.querySwitches(switchesQueryListener,
				getActivity());
	}

	private QueryListener switchesQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (getActivity() == null) {
				if (cursor != null) {
					cursor.close();
				}
				return;
			}

			if (cursor != null) {

				while (cursor.moveToNext()) {

					try {
						Switch switchh = DataObjectTranslator
								.getSwitchFromCursor(cursor);

						onSwitchUpdate(switchh);

						assignSwitchButtonTag(switchh);

					} catch (CursorIndexOutOfBoundsException e) {
						e.printStackTrace();
					}

				}

				cursor.close();
			}
		}

	};

	private ContentObserver switchesContentObserver = new ContentObserver(
			handler) {

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			getAllSwitchesCursor();
			super.onChange(selfChange);
		}

	};

	private void assignSwitchButtonTag(Switch switchh) {
		switch (switchh.getName()) {
		case BK_BK:
			bkBkButton.setTag(switchh.getId());
			break;
		case BK_BURNER:
			bkBurnerButton.setTag(switchh.getId());
			break;
		case BK_FERM:
			bkFermButton.setTag(switchh.getId());
			break;
		case BK_PUMP:
			bkPumpButton.setTag(switchh.getId());
			break;
		case HLT_BURNER:
			hltBurnerButton.setTag(switchh.getId());
			break;
		case HLT_HLT:
			hltHltButton.setTag(switchh.getId());
			break;
		case HLT_MLT:
			hltMltButton.setTag(switchh.getId());
			break;
		case HLT_PUMP:
			hltPumpButton.setTag(switchh.getId());
			break;
		case IGNITER:
			igniterButton.setTag(switchh.getId());
			break;
		case FILL:
			fillButton.setTag(switchh.getId());
			break;
		case CHILL:
			chillButton.setTag(switchh.getId());
			break;
		case AIR:
			airButton.setTag(switchh.getId());
			break;
		case MLT_BK:
			mltBkButton.setTag(switchh.getId());
			break;
		case MLT_BURNER:
			mltBurnerButton.setTag(switchh.getId());
			break;
		case MLT_MLT:
			mltMltButton.setTag(switchh.getId());
			break;
		case MLT_PUMP:
			mltPumpButton.setTag(switchh.getId());
			break;
		}
	}

	private void onSwitchUpdate(Switch switchh) {

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		switch (switchh.getName()) {

		case HLT_PUMP:
			hltPumpIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case HLT_BURNER:
			hltBurnerIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case HLT_HLT:
			hltHltIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case HLT_MLT:
			hltMltIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case MLT_PUMP:
			mltPumpIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case MLT_BURNER:
			mltBurnerIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case MLT_MLT:
			mltMltIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case MLT_BK:
			mltBkIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case BK_PUMP:
			bkPumpIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case BK_BURNER:
			bkBurnerIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case BK_BK:
			bkBkIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case BK_FERM:
			bkFermIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case IGNITER:
			igniterIndicator
					.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
							: SWITCH_STATE.OFF);
			break;
		case FILL:
			fillIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case CHILL:
			chillIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		case AIR:
			airIndicator.setSwitchState(switchh.getValue() ? SWITCH_STATE.ON
					: SWITCH_STATE.OFF);
			break;
		}
		
		// MediaPlayer mediaPlayer = null;
		//
		// mediaPlayer = switchh.getValue() ? mMediaPlayerSwitchOn
		// : mMediaPlayerSwitchOff;
		//
		// if (mediaPlayer != null) {
		// mediaPlayer.seekTo(0);
		// mediaPlayer.start();
		// }

	}

	private OnClickListener switchClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (false && !SocketManager.isConnected()) {
				return;
			}

			if (v == null || v.getTag() == null) {
				Toast.makeText(getActivity(), "Something went terribly wrong!",
						Toast.LENGTH_SHORT).show();
				return;

			}
			int switchId = (Integer) v.getTag();

			OnOffIndicator indicator = null;

			switch (v.getId()) {

			case R.id.hltPumpButton:
				indicator = hltPumpIndicator;
				break;

			case R.id.hltBurnerButton:
				indicator = hltBurnerIndicator;
				break;

			case R.id.hltHltButton:
				indicator = hltHltIndicator;
				break;

			case R.id.hltMltButton:
				indicator = hltMltIndicator;
				break;

			case R.id.mltPumpButton:
				indicator = mltPumpIndicator;
				break;

			case R.id.mltBurnerButton:
				indicator = mltBurnerIndicator;
				break;

			case R.id.mltMltButton:
				indicator = mltMltIndicator;
				break;

			case R.id.mltBkButton:
				indicator = mltBkIndicator;
				break;

			case R.id.bkPumpButton:
				indicator = bkPumpIndicator;
				break;

			case R.id.bkBurnerButton:
				indicator = bkBurnerIndicator;
				break;

			case R.id.bkBkButton:
				indicator = bkBkIndicator;
				break;

			case R.id.bkFermButton:
				indicator = bkFermIndicator;
				break;

			case R.id.igniterButton:
				indicator = igniterIndicator;
				break;
			case R.id.fillButton:
				indicator = fillIndicator;
				break;
			case R.id.chillButton:
				indicator = chillIndicator;
				break;
			case R.id.airButton:
				indicator = airIndicator;
				break;
			}

			if (indicator == null) {
				return;
			}

			SWITCH_STATE switchState = indicator.getSwitchState();

			if (switchState == null) {
				return;
			}

			boolean sendState = false;

			switch (switchState) {
			case OFF:
				switchState = SWITCH_STATE.PENDING_ON;
				sendState = true;
				break;
			case ON:
				switchState = SWITCH_STATE.PENDING_OFF;
				sendState = false;
				break;
			case PENDING_OFF:
				switchState = SWITCH_STATE.PENDING_ON;
				sendState = true;
				break;
			case PENDING_ON:
				switchState = SWITCH_STATE.PENDING_OFF;
				sendState = false;
				break;
			default:
				break;

			}

			 MediaPlayer mediaPlayer = null;
			
			 mediaPlayer = (switchState == SWITCH_STATE.ON || switchState ==
			 SWITCH_STATE.PENDING_OFF) ? mMediaPlayerSwitchOff
			 : mMediaPlayerSwitchOn;
			
			 if (mediaPlayer != null) {
			 mediaPlayer.seekTo(0);
			 mediaPlayer.start();
			 }

			indicator.setSwitchState(switchState);

			Intent intent = new Intent(BrewDroidService.ACTION_SWITCH_UPDATE);
			intent.putExtra(BrewDroidService.BUNDLE_SWITCH_ID, switchId);
			intent.putExtra(BrewDroidService.BUNDLE_SWITCH_VALUE, sendState);
			intent.setClass(getActivity(), BrewDroidService.class);
			getActivity().startService(intent);

		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_brew_control, null);

		connectedText = (TextView) v.findViewById(R.id.connectedText);
		permissionText = (TextView) v.findViewById(R.id.permissionText);
		pingText = (TextView) v.findViewById(R.id.pingText);

		hltTempText = (TextView) v.findViewById(R.id.hltTempText);
		hltFlameText = (TextView) v.findViewById(R.id.hltFlameText);
		hltVolumeText = (TextView) v.findViewById(R.id.hltVolumeText);

		mltTempText = (TextView) v.findViewById(R.id.mltTempText);
		mltFlameText = (TextView) v.findViewById(R.id.mltFlameText);

		bkTempText = (TextView) v.findViewById(R.id.bkTempText);
		bkFlameText = (TextView) v.findViewById(R.id.bkFlameText);
		bkVolumeText = (TextView) v.findViewById(R.id.bkVolumeText);

		fermTempText = (TextView) v.findViewById(R.id.fermTempText);

		boxTempText = (TextView) v.findViewById(R.id.boxTempText);

		hltPumpButton = v.findViewById(R.id.hltPumpButton);
		hltBurnerButton = v.findViewById(R.id.hltBurnerButton);
		hltHltButton = v.findViewById(R.id.hltHltButton);
		hltMltButton = v.findViewById(R.id.hltMltButton);

		mltPumpButton = v.findViewById(R.id.mltPumpButton);
		mltBurnerButton = v.findViewById(R.id.mltBurnerButton);
		mltMltButton = v.findViewById(R.id.mltMltButton);
		mltBkButton = v.findViewById(R.id.mltBkButton);

		bkPumpButton = v.findViewById(R.id.bkPumpButton);
		bkBurnerButton = v.findViewById(R.id.bkBurnerButton);
		bkBkButton = v.findViewById(R.id.bkBkButton);
		bkFermButton = v.findViewById(R.id.bkFermButton);

		igniterButton = v.findViewById(R.id.igniterButton);
		fillButton = v.findViewById(R.id.fillButton);
		chillButton = v.findViewById(R.id.chillButton);
		airButton = v.findViewById(R.id.airButton);

		hltPumpIndicator = (OnOffIndicator) v
				.findViewById(R.id.hltPumpIndicator);
		hltBurnerIndicator = (OnOffIndicator) v
				.findViewById(R.id.hltBurnerIndicator);
		hltHltIndicator = (OnOffIndicator) v.findViewById(R.id.hltHltIndicator);
		hltMltIndicator = (OnOffIndicator) v.findViewById(R.id.hltMltIndicator);

		mltPumpIndicator = (OnOffIndicator) v
				.findViewById(R.id.mltPumpIndicator);
		mltBurnerIndicator = (OnOffIndicator) v
				.findViewById(R.id.mltBurnerIndicator);
		mltMltIndicator = (OnOffIndicator) v.findViewById(R.id.mltMltIndicator);
		mltBkIndicator = (OnOffIndicator) v.findViewById(R.id.mltBkIndicator);

		bkPumpIndicator = (OnOffIndicator) v.findViewById(R.id.bkPumpIndicator);
		bkBurnerIndicator = (OnOffIndicator) v
				.findViewById(R.id.bkBurnerIndicator);
		bkBkIndicator = (OnOffIndicator) v.findViewById(R.id.bkBkIndicator);
		bkFermIndicator = (OnOffIndicator) v.findViewById(R.id.bkFermIndicator);

		igniterIndicator = (OnOffIndicator) v
				.findViewById(R.id.igniterIndicator);
		fillIndicator = (OnOffIndicator) v.findViewById(R.id.fillIndicator);
		chillIndicator = (OnOffIndicator) v.findViewById(R.id.chillIndicator);
		airIndicator = (OnOffIndicator) v.findViewById(R.id.airIndicator);

		hltPumpButton.setSoundEffectsEnabled(false);
		hltBurnerButton.setSoundEffectsEnabled(false);
		hltHltButton.setSoundEffectsEnabled(false);
		hltMltButton.setSoundEffectsEnabled(false);

		mltPumpButton.setSoundEffectsEnabled(false);
		mltBurnerButton.setSoundEffectsEnabled(false);
		mltMltButton.setSoundEffectsEnabled(false);
		mltBkButton.setSoundEffectsEnabled(false);

		bkPumpButton.setSoundEffectsEnabled(false);
		bkBurnerButton.setSoundEffectsEnabled(false);
		bkBkButton.setSoundEffectsEnabled(false);
		bkFermButton.setSoundEffectsEnabled(false);

		igniterButton.setSoundEffectsEnabled(false);
		fillButton.setSoundEffectsEnabled(false);
		chillButton.setSoundEffectsEnabled(false);
		airButton.setSoundEffectsEnabled(false);

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
		fillButton.setOnClickListener(switchClick);
		chillButton.setOnClickListener(switchClick);
		airButton.setOnClickListener(switchClick);

		v.findViewById(R.id.progressBar1).setVisibility(View.GONE);

		getAllSensorsCursor();
		getAllSwitchesCursor();
		getPermissions();

		setConnectedState();

		return v;
	}

}
