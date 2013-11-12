package com.brew.brewdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.SOCKET_CHANNEL;
import com.brew.lib.model.Sensor;

public class BrewControlFragment extends Fragment {

	private Handler handler;

	public static BrewControlFragment instantiate() {
		BrewControlFragment frag = new BrewControlFragment();
		frag.handler = new Handler();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		getAllSensorsCursor();

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
				"new value for" + sensor.getSensorName() + ": "
						+ sensor.getValue());
	}

}
