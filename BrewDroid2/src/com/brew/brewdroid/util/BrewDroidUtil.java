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
		editor.putInt("USER_ID", user.getId());
		
		editor.commit();
	}

	public static void setUserId(Context context, int userId) {
		SharedPreferences sp = context.getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);

		Editor editor = sp.edit();

		editor.putInt("USER_ID", userId);
		
		editor.commit();
	}

	public static void deleteUser(Context context) {
		SharedPreferences sp = context.getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);

		Editor editor = sp.edit();

		editor.remove("USER_USERNAME");
		editor.remove("USER_PASSWORD");
		editor.remove("USER_ID");

		editor.commit();
	}

	public static User getSavedUser(Context context) {

		if (context == null) {
			return null;
		}

		SharedPreferences sp = context.getSharedPreferences("BREW_PREFS",
				Context.MODE_PRIVATE);

		if (!sp.contains("USER_USERNAME")) {
			return null;
		}

		String username = sp.getString("USER_USERNAME", "");
		String password = sp.getString("USER_PASSWORD", "");
		int id = sp.getInt("USER_ID", 0);

		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		user.setId(id);

		return user;
	}
}
