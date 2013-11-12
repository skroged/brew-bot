package com.brew.brewdroid;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeScreen extends Activity {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] mNavigationItems;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ServiceControlFragment mServiceControlFrag;
	private LoginUserFragment mLoginFragment;
	private BrewControlFragment mBrewControlFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		mNavigationItems = new String[] { "Service Control", "Login",
				"Brew Control" };

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1,
				mNavigationItems));

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		goToServiceControlFragment();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void goToServiceControlFragment() {

		if (mServiceControlFrag == null) {
			mServiceControlFrag = ServiceControlFragment.instantiate();
		}

		if (getFragmentManager().findFragmentById(R.id.content_frame) == mServiceControlFrag) {
			return;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mServiceControlFrag);
		ft.commit();
	}

	private void goToLoginFragment() {

		if (mLoginFragment == null) {
			mLoginFragment = LoginUserFragment.instantiate();
		}

		if (getFragmentManager().findFragmentById(R.id.content_frame) == mLoginFragment) {
			return;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mLoginFragment);
		ft.commit();
	}

	private void gotoBrewControlFragment() {

		if (mBrewControlFragment == null) {
			mBrewControlFragment = BrewControlFragment.instantiate();
		}

		if (getFragmentManager().findFragmentById(R.id.content_frame) == mBrewControlFragment) {
			return;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mBrewControlFragment);
		ft.commit();
	}

	private void selectItem(int position) {

		if (position == 0) {
			goToServiceControlFragment();

		} else if (position == 1) {
			goToLoginFragment();
		} else if (position == 2) {
			gotoBrewControlFragment();
		}
		// // Create a new fragment and specify the planet to show based on
		// // position
		// Fragment fragment = new ServiceControlFragment();
		// Bundle args = new Bundle();
		// // args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		// fragment.setArguments(args);
		//
		// // Insert the fragment by replacing any existing fragment
		// FragmentManager fragmentManager = getFragmentManager();
		// fragmentManager.beginTransaction()
		// .replace(R.id.content_frame, fragment).commit();

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mNavigationItems[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(title);
	}

}
