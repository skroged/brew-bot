package com.brew.brewdroid;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.brew.brewdroid.UserView.UserViewListener;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.User;

public class UsersCursorAdapter extends CursorAdapter {

	private UserViewListener mUserViewListener;

	public UsersCursorAdapter(Context context, Cursor c,
			UserViewListener userViewListener) {
		super(context, c, 0);
		mUserViewListener = userViewListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		UserView uv = (UserView) view;

		User user = DataObjectTranslator.getUserFromCursor(cursor);

		uv.setUser(user);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		UserView ssv = new UserView(context);

		ssv.setUserViewListener(mUserViewListener);

		return ssv;
	}

}
