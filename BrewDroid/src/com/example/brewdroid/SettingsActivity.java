package com.example.brewdroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	private EditText ipText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		ipText = (EditText) findViewById(R.id.ipText);
	}

	@Override
	protected void onPause() {

		String ip = ipText.getText().toString();
		SharedPreferences sp = this.getSharedPreferences("SETTINGS",
				Context.MODE_PRIVATE);
		sp.edit().putString("BREW_SERVER_IP", ip).commit();

		super.onPause();
	}

	@Override
	protected void onResume() {

		SharedPreferences sp = this.getSharedPreferences("SETTINGS",
				Context.MODE_PRIVATE);

		String ip = sp.getString("BREW_SERVER_IP", "skroged.zapto.org");

		ipText.setText(ip);

		super.onResume();
	}

}
