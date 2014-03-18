package com.brew.brewdroid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.DataObjectTranslator;
import com.brew.lib.model.User;
import com.brew.lib.model.UserChannelPermission;

public class UserView extends RelativeLayout {

	private UserViewListener mUserViewListener;
	private TextView mNameText;
	private TextView mPermissionText;
	private User mUser;

	public UserView(Context context) {
		super(context);
		setStuff();
	}

	public UserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setStuff();
	}

	public UserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setStuff();
	}

	private void setStuff() {
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_user, this);

		mNameText = (TextView) findViewById(R.id.nameText);
		mPermissionText = (TextView) findViewById(R.id.permissionsText);

	}

	public void setUserViewListener(UserViewListener userViewListener) {
		this.mUserViewListener = userViewListener;
	}

	public static interface UserViewListener {
		// public void onEditHighClicked(UserView sender);
		//
		// public void onEditLowClicked(UserView sender);
		//
		// public void onEditAddressClicked(UserView sender);
	}

	private QueryListener permissionsQueryListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			if (cursor != null) {
				List<UserChannelPermission> permissions = new ArrayList<UserChannelPermission>();
				while (cursor.moveToNext()) {
					UserChannelPermission ucp = DataObjectTranslator
							.getPermissionFromCursor(cursor);
					if (mUser.getId() != ucp.getUserId()) {
						cursor.close();
						return;
					}
					permissions.add(ucp);
				}

				cursor.close();

				mUser.setPermissions(permissions);
				setPermissionsText();
			}

		}

	};

	private void setPermissionsText() {
		String permissions = "";
		for (int i = 0; i < mUser.getPermissions().size(); i++) {

			String comma = i < mUser.getPermissions().size() - 1 ? ", " : "";

			permissions += mUser.getPermissions().get(i).getChannel()
					.toString()
					+ comma;
		}

		mPermissionText.setText(permissions);
	}

	private void getPermissionsCursor() {

		BrewDroidContentProvider.queryPermissionsForUserId(
				permissionsQueryListener, getContext(), mUser.getId());
	}

	public void setUser(User user) {
		mUser = user;
		mNameText.setText(mUser.getName());

		getPermissionsCursor();

		if (user.getPermissions() != null) {

			setPermissionsText();

		} else {
			mPermissionText.setText("");
		}
	}

}
