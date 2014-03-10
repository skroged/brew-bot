package com.brew.brewdroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.brew.brewdroid.UserView.UserViewListener;
import com.brew.brewdroid.data.BrewDroidContentProvider;
import com.brew.brewdroid.data.BrewDroidContentProvider.QueryListener;
import com.brew.brewdroid.data.BrewDroidService;
import com.brew.lib.model.SOCKET_CHANNEL;

public class UsersFragment extends Fragment {

	private ListView mUsersList;
	private Handler mHandler;
	private UsersCursorAdapter mUsersCursorAdapter;

	public static UsersFragment instantiate() {
		UsersFragment frag = new UsersFragment();
		frag.mHandler = new Handler();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_users, null);

		mUsersList = (ListView) v.findViewById(R.id.usersList);

		getCursor();

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Intent intent = new Intent(BrewDroidService.ACTION_SUBSCRIBE);
		intent.putExtra(BrewDroidService.BUNDLE_CHANNEL,
				SOCKET_CHANNEL.USERS.toString());
		intent.setClass(getActivity(), BrewDroidService.class);
		getActivity().startService(intent);

		BrewDroidContentProvider.registerUsersContentObserver(activity,
				mUsersContentObserver);

	}

	private ContentObserver mUsersContentObserver = new ContentObserver(
			mHandler) {
		@Override
		public void onChange(boolean selfChange) {
			getCursor();
			super.onChange(selfChange);
		}
	};

	private QueryListener mUsersCursorListener = new QueryListener() {

		@Override
		public void onComplete(Cursor cursor) {

			mUsersCursorAdapter = new UsersCursorAdapter(getActivity(), cursor,
					mUserViewListener);

			mUsersList.setAdapter(mUsersCursorAdapter);

		}

	};

	private UserViewListener mUserViewListener = new UserViewListener() {

	};

	private void getCursor() {

		BrewDroidContentProvider
				.queryUsers(mUsersCursorListener, getActivity());

	}
}
