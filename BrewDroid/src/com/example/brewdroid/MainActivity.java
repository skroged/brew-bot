package com.example.brewdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.brew.client.socket.SocketManager;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SocketManager.connect(MainActivity.this);

		findViewById(R.id.connectButton).setOnClickListener(
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
