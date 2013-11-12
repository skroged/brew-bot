package com.brew.brewdroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.brew.lib.model.User;

public class BrewDroidUtil {

	public static void saveUser(Context context, User user) {

		SharedPreferences sp = context.getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);

		Editor editor = sp.edit();

		editor.putString("USER_USERNAME", user.getUsername());
		editor.putString("USER_PASSWORD", user.getPassword());

		editor.commit();
	}

	public static User getSavedUser(Context context) {

		SharedPreferences sp = context.getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);

		String username = sp.getString("USER_USERNAME", "");
		String password = sp.getString("USER_PASSWORD", "");

		User user = new User();
		user.setUsername(username);
		user.setPassword(password);

		return user;
	}
}
