package com.brew.brewdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.socket.SocketManager;

public class ServiceControlFragment extends Fragment {

	private TextView mStatusText;
	private Button mServiceButton;
	private EditText mHostText;

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
	private TextWatcher mTextChangedListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			SharedPreferences sp = getActivity().getSharedPreferences(
					"BREW_PREFS", Context.MODE_PRIVATE);
			sp.edit().putString("HOST", s.toString()).commit();

		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_service_control, null);
		mStatusText = (TextView) v.findViewById(R.id.statusText);
		mServiceButton = (Button) v.findViewById(R.id.serviceButton);
		mHostText = (EditText) v.findViewById(R.id.hostText);

		mServiceButton.setOnClickListener(onClickListener);

		SharedPreferences sp = getActivity().getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);
		String host = sp.getString("HOST", "");
		mHostText.setText(host);
		mHostText.addTextChangedListener(mTextChangedListener);

		setConnectedState();

		return v;
	}

	private void setConnectedState() {

		if (SocketManager.isConnected()) {
			mHostText.setEnabled(false);
			mStatusText.setText("Service connected");
			mServiceButton.setText("Disconnect");
		} else {
			mHostText.setEnabled(true);
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
