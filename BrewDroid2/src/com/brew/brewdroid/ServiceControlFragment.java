package com.brew.brewdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.socket.SocketManager;

public class ServiceControlFragment extends Fragment {

	private TextView mStatusText;
	private Button mServiceButton;

	public static ServiceControlFragment instantiate() {

		ServiceControlFragment frag = new ServiceControlFragment();

		return frag;
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.serviceButton:

				if (SocketManager.isConnected()) {
					getActivity().stopService(
							new Intent(getActivity(), BrewDroidService.class));
				} else {

					Intent intent = new Intent(
							BrewDroidService.ACTION_FOREGROUND);
					intent.setClass(getActivity(), BrewDroidService.class);
					getActivity().startService(intent);
				}

				break;
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_service_control, null);
		mServiceButton = (Button) v.findViewById(R.id.serviceButton);
		mServiceButton.setOnClickListener(onClickListener);
		mStatusText = (TextView) v.findViewById(R.id.statusText);

		setConnectedState();

		return v;
	}

	private void setConnectedState() {

		if (SocketManager.isConnected()) {
			mStatusText.setText("Service connected");
			mServiceButton.setText("Disconnect");
		} else {
			mStatusText.setText("Service disconnected");
			mServiceButton.setText("Connect");
		}

	}

	public BroadcastReceiver mConnectReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setConnectedState();
		}
	};

	@Override
	public void onAttach(Activity activity) {

		activity.registerReceiver(mConnectReceiver, new IntentFilter(
				BrewDroidService.ACTION_CONNECT_CHANGED));

		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mConnectReceiver);
		super.onDetach();
	}
}
