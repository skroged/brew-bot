package com.brew.brewdroid;

import java.text.NumberFormat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.SWITCH_NAME;
import com.brew.lib.model.Sensor;
import com.brew.lib.model.Switch;
import com.brew.lib.model.SwitchTransport;

public class BrewControlFragment extends Fragment {

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

	public static BrewControlFragment instantiate() {
		BrewControlFragment frag = new BrewControlFragment();
		frag.handler = new Handler();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		getAllSensorsCursor();
		getAllSwitchesCursor();

		Intent intent = new Intent(BrewDroidService.ACTION_SUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.BREW_CONTROL.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

	}

	@Override
	public void onDetach() {

		Intent intent = new Intent(BrewDroidService.ACTION_UNSUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.BREW_CONTROL.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

		BrewDroidContentProvider.unregisterContentObserver(getActivity(),
				sensorContentObserver);
		super.onDetach();
	}

	private void getAllSensorsCursor() {
		BrewDroidContentProvider.querySensors(sensorsQueryListener,
				getActivity());
	}

	private QueryListener sensorsQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (cursor != null) {

				while (cursor.moveToNext()) {

					try {
						Sensor sensor = DataObjectTranslator
								.getSensorFromCursor(cursor);

						int sensorId = sensor.getSensorId();
						BrewDroidContentProvider.registerSensorContentObserver(
								getActivity(), sensorContentObserver, sensorId);
						onSensorUpdate(sensor);
					} catch (CursorIndexOutOfBoundsException e) {
						e.printStackTrace();
					}

				}

				cursor.close();
			}
		}

	};

	private ContentObserver sensorContentObserver = new ContentObserver(handler) {

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			BrewDroidContentProvider.querySensor(sensorQueryListener,
					getActivity(), uri);
			super.onChange(selfChange);
		}

	};

	private QueryListener sensorQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {
			try {
				while (cursor.moveToNext()) {
					Sensor sensor = DataObjectTranslator
							.getSensorFromCursor(cursor);
					onSensorUpdate(sensor);
				}
				cursor.close();
			} catch (CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
			}

		}

	};

	private void onSensorUpdate(Sensor sensor) {
		Log.i("JOSH",
				"new value for " + sensor.getSensorName() + ": "
						+ sensor.getValue());

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		switch (sensor.getSensorName()) {

		case HLT_TEMP:
			hltTempText.setText(nf.format(sensor.getValue()) + " °F");
			break;
		case HLT_VOLUME:
			hltVolumeText.setText(nf.format(sensor.getValue()) + " Gal");
			break;
		case MLT_TEMP:
			mltTempText.setText(nf.format(sensor.getValue()) + " °F");
			break;
		case BK_TEMP:
			bkTempText.setText(nf.format(sensor.getValue()) + " °F");
			break;
		case BK_VOLUME:
			bkVolumeText.setText(nf.format(sensor.getValue()) + " Gal");
			break;
		case FERM_TEMP:
			fermTempText.setText(nf.format(sensor.getValue()) + " °F");
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

			if (cursor != null) {

				while (cursor.moveToNext()) {

					try {
						Switch switchh = DataObjectTranslator
								.getSwitchFromCursor(cursor);

						int switchId = switchh.getId();
						BrewDroidContentProvider.registerSwitchContentObserver(
								getActivity(), switchContentObserver, switchId);
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

	private ContentObserver switchContentObserver = new ContentObserver(handler) {

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			BrewDroidContentProvider.querySwitch(switchQueryListener,
					getActivity(), uri);
			super.onChange(selfChange);
		}

	};

	private QueryListener switchQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {
			try {
				while (cursor.moveToNext()) {
					Switch switchh = DataObjectTranslator
							.getSwitchFromCursor(cursor);
					onSwitchUpdate(switchh);
				}
				cursor.close();
			} catch (CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
			}

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
		Log.i("JOSH",
				"new value for " + switchh.getName() + ": "
						+ switchh.getValue());

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);

		switch (switchh.getName()) {

		case HLT_PUMP:
			hltPumpIndicator.setOn(switchh.getValue());
			break;
		case HLT_BURNER:
			hltBurnerIndicator.setOn(switchh.getValue());
			break;
		case HLT_HLT:
			hltHltIndicator.setOn(switchh.getValue());
			break;
		case HLT_MLT:
			hltMltIndicator.setOn(switchh.getValue());
			break;
		case MLT_PUMP:
			mltPumpIndicator.setOn(switchh.getValue());
			break;
		case MLT_BURNER:
			mltBurnerIndicator.setOn(switchh.getValue());
			break;
		case MLT_MLT:
			mltMltIndicator.setOn(switchh.getValue());
			break;
		case MLT_BK:
			mltBkIndicator.setOn(switchh.getValue());
			break;
		case BK_PUMP:
			bkPumpIndicator.setOn(switchh.getValue());
			break;
		case BK_BURNER:
			bkBurnerIndicator.setOn(switchh.getValue());
			break;
		case BK_BK:
			bkBkIndicator.setOn(switchh.getValue());
			break;
		case BK_FERM:
			bkFermIndicator.setOn(switchh.getValue());
			break;
		case IGNITER:
			igniterIndicator.setOn(switchh.getValue());
			break;
		}

	}

	private OnClickListener switchClick = new OnClickListener() {

		@Override
		public void onClick(View v) {

			int switchId = (Integer) v.getTag();
			boolean isOn = false;;

			switch (v.getId()) {

			case R.id.hltPumpButton:
				isOn = !hltPumpIndicator.isOn();
				break;

			case R.id.hltBurnerButton:
				isOn = !hltBurnerIndicator.isOn();
				break;

			case R.id.hltHltButton:
				isOn = !hltHltIndicator.isOn();
				break;

			case R.id.hltMltButton:
				isOn = !hltMltIndicator.isOn();
				break;

			case R.id.mltPumpButton:
				isOn = !mltPumpIndicator.isOn();
				break;

			case R.id.mltBurnerButton:
				isOn = !mltBurnerIndicator.isOn();
				break;

			case R.id.mltMltButton:
				isOn = !mltMltIndicator.isOn();
				break;

			case R.id.mltBkButton:
				isOn = !mltBkIndicator.isOn();
				break;

			case R.id.bkPumpButton:
				isOn = !bkPumpIndicator.isOn();
				break;

			case R.id.bkBurnerButton:
				isOn = !bkBurnerIndicator.isOn();
				break;

			case R.id.bkBkButton:
				isOn = !bkBkIndicator.isOn();
				break;

			case R.id.bkFermButton:
				isOn = !bkFermIndicator.isOn();
				break;

			case R.id.igniterButton:
				isOn = !igniterIndicator.isOn();
				break;
			}

			Intent intent = new Intent(BrewDroidService.ACTION_SWITCH_UPDATE);
			intent.putExtra(BrewDroidService.BUNDLE_SWITCH_ID, switchId);
			intent.putExtra(BrewDroidService.BUNDLE_SWITCH_VALUE, isOn);
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
		hltVolumeText = (TextView) v.findViewById(R.id.hltVolumeText);

		mltTempText = (TextView) v.findViewById(R.id.mltTempText);

		bkTempText = (TextView) v.findViewById(R.id.bkTempText);
		bkVolumeText = (TextView) v.findViewById(R.id.bkVolumeText);

		fermTempText = (TextView) v.findViewById(R.id.fermTempText);

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

		v.findViewById(R.id.progressBar1).setVisibility(View.GONE);

		// submitButton.setOnClickListener(clickListener);

		return v;
	}

}
