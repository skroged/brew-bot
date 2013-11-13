package com.brew.brewdroid;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.brew.brewdroid.data.BrewDroidService;
import com.brew.lib.model.User;
import com.brew.lib.util.BrewHelper;

public class LoginUserFragment extends Fragment {

	private EditText usernameText;
	private EditText passwordText;
	private Button submitButton;

	public static LoginUserFragment instantiate() {

		LoginUserFragment frag = new LoginUserFragment();

		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login_user, null);

		usernameText = (EditText) v.findViewById(R.id.usernameText);
		passwordText = (EditText) v.findViewById(R.id.passwordText);
		submitButton = (Button) v.findViewById(R.id.submitButton);

		submitButton.setOnClickListener(clickListener);

		return v;
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.submitButton:

				Intent intent = new Intent(BrewDroidService.ACTION_LOGIN);
				intent.putExtra(BrewDroidService.BUNDLE_USERNAME, usernameText
						.getText().toString());
				intent.putExtra(BrewDroidService.BUNDLE_PASSWORD,
						BrewHelper.md5(passwordText.getText().toString()));
				intent.setClass(getActivity(), BrewDroidService.class);
				getActivity().startService(intent);

				break;
			}

		}

	};

}
