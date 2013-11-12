package com.brew.brewdroid;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.brew.brewdroid.data.BrewDroidService;

public class ServiceControlFragment extends Fragment {

	public static ServiceControlFragment instantiate() {
		
		ServiceControlFragment frag = new ServiceControlFragment();
		
		return frag;
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.startButton:

				Intent intent = new Intent(BrewDroidService.ACTION_FOREGROUND);
				intent.setClass(getActivity(), BrewDroidService.class);
				getActivity().startService(intent);

				break;
			case R.id.stopButton:
				getActivity().stopService(
						new Intent(getActivity(), BrewDroidService.class));
				break;
			}
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_service_control, null);

		v.findViewById(R.id.startButton).setOnClickListener(onClickListener);
		v.findViewById(R.id.stopButton).setOnClickListener(onClickListener);

		return v;
	}

}
