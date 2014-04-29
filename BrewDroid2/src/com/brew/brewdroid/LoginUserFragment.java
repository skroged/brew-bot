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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brew.brewdroid.data.BrewDroidService;
import com.brew.brewdroid.socket.SocketManager;
import com.brew.brewdroid.util.BrewDroidUtil;
import com.brew.lib.model.User;
import com.brew.lib.util.BrewHelper;

public class LoginUserFragment extends Fragment {

	private EditText usernameText;
	private EditText passwordText;
	private Button submitButton;
	private TextView loggedInText;
	private TextView usernameLabel;
	private TextView passwordLabel;
	private User mUser;

	public static LoginUserFragment instantiate() {

		LoginUserFragment frag = new LoginUserFragment();

		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login_user, null);

		loggedInText = (TextView) v.findViewById(R.id.loggedInText);
		usernameLabel = (TextView) v.findViewById(R.id.usernameLabel);
		passwordLabel = (TextView) v.findViewById(R.id.passwordLabel);
		usernameText = (EditText) v.findViewById(R.id.usernameText);
		passwordText = (EditText) v.findViewById(R.id.passwordText);
		submitButton = (Button) v.findViewById(R.id.submitButton);

		submitButton.setOnClickListener(clickListener);

		getSavedUser();

		return v;
	}

	public BroadcastReceiver mAuthReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			getSavedUser();
		}
	};

	@Override
	public void onAttach(Activity activity) {

		IntentFilter filter = new IntentFilter(BrewDroidService.ACTION_AUTH_RESULT);
		filter.addAction(BrewDroidService.ACTION_LOGOUT);
		activity.registerReceiver(mAuthReceiver, filter);

		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
				mAuthReceiver);
		super.onDetach();
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.submitButton:

				if (mUser == null) {
					// login

					if (SocketManager.isConnected()) {
						Intent intent = new Intent(
								BrewDroidService.ACTION_LOGIN);
						intent.putExtra(BrewDroidService.BUNDLE_USERNAME,
								usernameText.getText().toString());
						intent.putExtra(BrewDroidService.BUNDLE_PASSWORD,
								BrewHelper.md5(passwordText.getText()
										.toString()));
						intent.setClass(getActivity(), BrewDroidService.class);
						getActivity().startService(intent);
					} else {
						Toast.makeText(getActivity(), "Connect Service First!",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					// logout
					Intent intent = new Intent(BrewDroidService.ACTION_LOGOUT);
					intent.putExtra(BrewDroidService.BUNDLE_USERNAME,
							usernameText.getText().toString());
					intent.putExtra(BrewDroidService.BUNDLE_PASSWORD,
							BrewHelper.md5(passwordText.getText().toString()));
					intent.setClass(getActivity(), BrewDroidService.class);
					getActivity().startService(intent);
					passwordText.setText("");
					getSavedUser();
				}
				break;
			}

		}

	};

	private void getSavedUser() {

		mUser = BrewDroidUtil.getSavedUser(getActivity());

		if (mUser != null) {

			loggedInText.setVisibility(View.VISIBLE);
			loggedInText.setText("Logged in as: " + mUser.getUsername());

			usernameText.setText(mUser.getUsername());

			usernameText.setVisibility(View.GONE);
			passwordText.setVisibility(View.GONE);
			usernameLabel.setVisibility(View.GONE);
			passwordLabel.setVisibility(View.GONE);

			submitButton.setText("Logout");
		} else {
			loggedInText.setVisibility(View.GONE);

			usernameText.setVisibility(View.VISIBLE);
			passwordText.setVisibility(View.VISIBLE);
			usernameLabel.setVisibility(View.VISIBLE);
			passwordLabel.setVisibility(View.VISIBLE);

			submitButton.setText("Login");
		}
	}

}
